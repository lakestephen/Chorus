/**
 *  Copyright (C) 2000-2015 The Software Conservancy and Original Authors.
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

import org.chorusbdd.chorus.logging.ChorusLog;
import org.chorusbdd.chorus.logging.ChorusLogFactory;
import org.chorusbdd.chorus.processes.manager.commandlinebuilder.AbstractCommandLineBuilder;
import org.chorusbdd.chorus.processes.manager.commandlinebuilder.JavaProcessCommandLineBuilder;
import org.chorusbdd.chorus.processes.manager.commandlinebuilder.NativeProcessCommandLineBuilder;
import org.chorusbdd.chorus.processes.manager.process.ChorusProcess;
import org.chorusbdd.chorus.processes.manager.process.NamedProcessConfig;
import org.chorusbdd.chorus.results.FeatureToken;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 17/07/13
 * Time: 22:15
 * To change this template use File | Settings | File Templates.
 */
public class ChorusProcessFactory {

    private ChorusLog log = ChorusLogFactory.getLog(ChorusProcessFactory.class);

    public ChorusProcess startChorusProcess(NamedProcessConfig namedProcessConfig, FeatureToken featureToken) throws Exception {

        String name = namedProcessConfig.getProcessName();

        //work out where the process std out and err should go
        ProcessOutputConfiguration outputConfig = new ProcessOutputConfiguration(featureToken, namedProcessConfig);

        List<String> commandLineTokens = buildCommandLine(namedProcessConfig, featureToken, outputConfig.getLogFileBaseName());

        ProcessManagerProcess processManagerProcess = new ProcessManagerProcess(namedProcessConfig, commandLineTokens, outputConfig);
        processManagerProcess.start();

        return processManagerProcess;
    }

    private List<String> buildCommandLine(NamedProcessConfig namedProcessConfig, FeatureToken featureToken, String logFileBaseName) {
        File featureDir = featureToken.getFeatureDir();
        AbstractCommandLineBuilder b = namedProcessConfig.isJavaProcess() ?
                new JavaProcessCommandLineBuilder(featureDir, namedProcessConfig, logFileBaseName) :
                new NativeProcessCommandLineBuilder(namedProcessConfig, featureDir);

        List<String> commandTokens = b.buildCommandLine();
        logCommandLine(commandTokens);
        return commandTokens;
    }

    private void logCommandLine(List<String> commandTokens) {
        StringBuilder commandBuilder = new StringBuilder();
        for ( String s : commandTokens) {
            commandBuilder.append(s).append(" ");
        }
        commandBuilder.deleteCharAt(commandBuilder.length() - 1);
        log.info("About to run process: " + commandBuilder);
    }
}
