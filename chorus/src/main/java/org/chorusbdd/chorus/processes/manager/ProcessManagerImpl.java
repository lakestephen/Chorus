/**
 *  Copyright (C) 2000-2014 The Software Conservancy and Original Authors.
 *  All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to
 *  deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 *  sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 *
 *  Nothing in this notice shall be deemed to grant any rights to trademarks,
 *  copyrights, patents, trade secrets or any other intellectual property of the
 *  licensor or any contributor except as expressly stated herein. No patent
 *  license is granted separate from the Software, for code that you delete from
 *  the Software, or for combinations of the Software with other software or
 *  hardware.
 */
package org.chorusbdd.chorus.processes.manager;

import org.chorusbdd.chorus.annotations.Scope;
import org.chorusbdd.chorus.core.interpreter.subsystem.processes.ProcessManager;
import org.chorusbdd.chorus.core.interpreter.subsystem.processes.ProcessManagerConfig;
import org.chorusbdd.chorus.executionlistener.ExecutionListener;
import org.chorusbdd.chorus.executionlistener.ExecutionListenerAdapter;
import org.chorusbdd.chorus.logging.ChorusLog;
import org.chorusbdd.chorus.logging.ChorusLogFactory;
import org.chorusbdd.chorus.results.ExecutionToken;
import org.chorusbdd.chorus.results.FeatureToken;
import org.chorusbdd.chorus.util.ChorusException;
import org.chorusbdd.chorus.util.NamedExecutors;
import org.chorusbdd.chorus.util.assertion.ChorusAssert;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The default implementation of Chorus' ProcessManager subsystem
 * 
 * Accessible by annotating a Handler field with 
 * @ChrousResource("process.manager")
 * ProcessManager processManager;
 */
public class ProcessManagerImpl implements ProcessManager {

    private static ScheduledExecutorService processesHandlerExecutor = NamedExecutors.newSingleThreadScheduledExecutor("ProcessesHandlerScheduler");

    private static ChorusLog log = ChorusLogFactory.getLog(ProcessManager.class);

    private final Map<String, ProcessInfo> processes = new ConcurrentHashMap<String, ProcessInfo>();

    private final CleanupShutdownHook cleanupShutdownHook = new CleanupShutdownHook();
    private final ProcessManagerConfigValidator processesConfigValidator = new ProcessManagerConfigValidator();

    private ExecutionListener processManagerExecutionListener = new ProcessManagerExecutionListener();
    private ChorusProcessFactory chorusProcessFactory = new ChorusProcessFactory();

    private volatile File featureDir;
    private volatile File featureFile;
    private volatile FeatureToken featureToken;

    public ProcessManagerImpl() {
        addShutdownHook();
    }


    // ----------------------------------------------------- Start/Stop Process

    /**
     * Starts a record Java process using properties defined in a properties file alongside the feature file
     *
     * @throws Exception
     */
    public synchronized void startProcess(String processName, ProcessManagerConfig processManagerConfig) throws Exception {

        ProcessInfo processInfo = new ProcessInfo(processName, processManagerConfig);
        checkConfigAndNotAlreadyStarted(processInfo);

        //get the log output containing logging configuration for this process
        ProcessLogOutput logOutput = new ProcessLogOutput(featureToken, featureDir, featureFile, processInfo);
        String logFileBaseName = logOutput.getLogFileBaseName();

        AbstractCommandLineBuilder b = processInfo.isJavaProcess() ?
                new JavaProcessCommandLineBuilder(featureDir, processInfo, logFileBaseName) :
                new NativeProcessCommandLineBuilder(processInfo, featureDir);

        List<String> commandLineTokens = b.buildCommandLine();
        startProcess(commandLineTokens, logOutput, processInfo);
    }

    private void checkConfigAndNotAlreadyStarted(ProcessInfo processInfo) {
        String processName = processInfo.getProcessName();
        ChorusAssert.assertFalse("There is already a process with the processName " + processName, processes.containsKey(processName));
        ChorusAssert.assertTrue("The config for " + processName + " must be valid", processesConfigValidator.checkValid(processInfo));
    }


    private void startProcess(List<String> commandLineTokens, ProcessLogOutput logOutput,  ProcessInfo processInfo) throws Exception {
        String name = processInfo.getProcessName();
        ChorusAssert.assertFalse("There is already a process with the processName " + name, processes.containsKey(name));
        processes.put(name, processInfo);

        ChorusProcess chorusProcess = chorusProcessFactory.createChorusProcess(name, commandLineTokens, logOutput);
        processInfo.setProcess(chorusProcess);
        chorusProcess.checkProcess(processInfo.getProcessCheckDelay());
    }

    public synchronized void stopProcess(String processName) {
        ChorusProcess p = processes.get(processName).getProcess();
        if (p != null) {
            try {
                p.destroy();
                log.debug("Stopped process: " + processName);
            } catch (Exception e) {
                log.warn("Failed to closeAllConnections process", e);
            } finally {
                processes.remove(processName);
            }
        } else {
            throw new ChorusException("There is no process named '" + processName + "' to stop");
        }
    }

    public synchronized void stopProcessesRunningWithinScope(Scope scope) {
        for (final ProcessInfo pInfo : processes.values()) {
            if (pInfo.getProcessScope() == scope ) {
                log.debug("Stopping process named " + pInfo.getProcessName() + " scoped to " + scope);
                try {
                    stopProcess(pInfo.getProcessName());
                } catch (Exception e) {
                    log.warn("Error when stopping process named " + pInfo.getProcessName(), e);
                }
            }
        }
    }

    public synchronized void stopAllProcesses() {
        final Set<String> processNames = new HashSet<String>(processes.keySet());
        for (final String name : processNames) {
            stopProcess(name);
        }
    }

    // ----------------------------------------------------- Process Properties

    public synchronized ProcessManagerConfig getProcessConfig(final String processName) {
        final ProcessInfo processInfo = processes.get(processName);
        return processInfo;
    }

    // --------------------------------------------------------- Process Status

    public synchronized void checkProcessHasStopped(String processName) {
        ProcessInfo p = processes.get(processName);
        if ( p == null ) {
            throw new ChorusException("There is no process named '" + processName + "' to check is stopped");
        }
        ChorusAssert.assertTrue("The process " + processName + " was not stopped", p.getProcess().isStopped());
    }

    public synchronized void checkProcessIsRunning(String processName) {
        ProcessInfo p = processes.get(processName);
        if ( p == null ) {
            throw new ChorusException("There is no process named '" + processName + "' to check is running");
        }
        ChorusAssert.assertTrue("Check the process " + processName + " is running", ! p.getProcess().isStopped());
    }

    public synchronized void waitForProcessToTerminate(String processName) {
        ProcessInfo p = processes.get(processName);
        if ( p == null ) {
            throw new ChorusException("There is no process named '" + processName + "' to wait to terminate");
        }
        int waitTime = p.getTerminateWaitTime();
        waitForProcessToTerminate(processName, waitTime);
    }

    // ---------------------------------------------------------- Process Comms

    public synchronized void readFromProcess(String pattern, String processName, boolean searchWithinLines) {
        ChorusProcess p = getAndCheckProcessByName(processName);
        p.waitForMatchInStdOut(pattern, searchWithinLines);
    }

    public synchronized void readFromProcessWithinNSeconds(String pattern, String processName, boolean searchWithinLines, int seconds) {
        ChorusProcess p = getAndCheckProcessByName(processName);
        p.waitForMatchInStdOut(pattern, searchWithinLines, TimeUnit.SECONDS, seconds);
    }

    public synchronized void readFromProcessStdError(String pattern, String processName, boolean searchWithinLines) {
        ChorusProcess p = getAndCheckProcessByName(processName);
        p.waitForMatchInStdErr(pattern, true);
    }

    public synchronized void readFromProcessStdErrorWithinNSeconds(String pattern, String processName, boolean searchWithinLines, int seconds) {
        ChorusProcess p = getAndCheckProcessByName(processName);
        p.waitForMatchInStdErr(pattern, true, TimeUnit.SECONDS, seconds);
    }

    public synchronized void writeToProcess(String line, String processName, boolean newLine) {
        ChorusProcess p = getAndCheckProcessByName(processName);
        p.writeToStdIn(line, newLine);
    }

    // -------------------------------------------------------- Private Methods


    private synchronized ChorusProcess getAndCheckProcessByName(String processName) {
        ProcessInfo p = processes.get(processName);
        if ( p == null ) {
            ChorusAssert.fail("Could not find the process " + processName);
        }
        return p.getProcess();
    }

    public synchronized void waitForProcessToTerminate(String processName, int waitTimeSeconds) {
        ProcessInfo p = processes.get(processName);
        if ( p == null ) {
            throw new ChorusException("There is no process named '" + processName + "' to wait for");
        }

        InterruptWaitTask t = new InterruptWaitTask(Thread.currentThread(), processName);
        processesHandlerExecutor.schedule(t, waitTimeSeconds, TimeUnit.SECONDS);

        try {
            p.getProcess().waitFor();
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for process " + processName + " to terminate");
            throw new ChorusException("Process " + processName + " failed to terminate after " + waitTimeSeconds + " seconds");
        }
        t.setWaitFinished(); //prevent the interrupt, process finished naturally
    }

    class InterruptWaitTask implements Runnable {
        private Thread waitingThread;
        private String processName;
        private volatile boolean isWaitFinished;

        InterruptWaitTask(Thread waitingThread, String processName) {
            this.waitingThread = waitingThread;
            this.processName = processName;
        }

        public void setWaitFinished() {
            isWaitFinished = true;
        }

        public void run() {
            if ( ! isWaitFinished) {
                log.warn("The process named " + processName + " appears not to have terminated, will stop waiting");
                waitingThread.interrupt();
            }
        }
    }

    private void addShutdownHook() {
        log.trace("Adding shutdown hook for ProcessHandler " + this);
        Runtime.getRuntime().addShutdownHook(cleanupShutdownHook);
    }

    /**
     * If shut down before a scenario completes, try as hard as we can to
     * stop any processes under test which Chorus started, since these do not appear
     * to die automatically with the parent in all environments
     */
    private class CleanupShutdownHook extends Thread {
        public void run() {
            log.debug("Running Cleanup on shutdown for ProcessHandler " + this);
            stopAllProcesses();
        }
    }

    public ExecutionListener getProcessManagerExecutionListener() {
        return processManagerExecutionListener;
    }

    private class ProcessManagerExecutionListener extends ExecutionListenerAdapter {
        public void featureStarted(ExecutionToken testExecutionToken, FeatureToken feature) {
            ProcessManagerImpl.this.featureToken = feature;
            ProcessManagerImpl.this.featureFile = feature.getFeatureFile();
            ProcessManagerImpl.this.featureDir = featureToken.getFeatureFile().getParentFile();
        }
    }}