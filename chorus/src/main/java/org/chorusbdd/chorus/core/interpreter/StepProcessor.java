package org.chorusbdd.chorus.core.interpreter;

import org.chorusbdd.chorus.annotations.Step;
import org.chorusbdd.chorus.executionlistener.ExecutionListenerSupport;
import org.chorusbdd.chorus.results.ExecutionToken;
import org.chorusbdd.chorus.results.StepEndState;
import org.chorusbdd.chorus.results.StepToken;
import org.chorusbdd.chorus.util.ChorusRemotingException;
import org.chorusbdd.chorus.util.ExceptionHandling;
import org.chorusbdd.chorus.util.logging.ChorusLog;
import org.chorusbdd.chorus.util.logging.ChorusLogFactory;

import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 24/02/13
 * Time: 11:20
 *
 * Process a scenario step by identifying and calling handler class methods
 * or by processing child steps for step macros.
 */
public class StepProcessor {

    private static ChorusLog log = ChorusLogFactory.getLog(StepProcessor.class);

    private ExecutionListenerSupport executionListenerSupport;
    private boolean dryRun;
    private volatile boolean interruptingOnTimeout;

    public StepProcessor(ExecutionListenerSupport executionListenerSupport) {
        this.executionListenerSupport = executionListenerSupport;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setInterruptingOnTimeout(boolean interruptingOnTimeout) {
        this.interruptingOnTimeout = interruptingOnTimeout;
    }

    /**
     * Process all steps in stepList
     *
     * @param skip, do not actually execute (but mark as skipped if not unimplemented)
     *
     * @return a StepEndState is the StepMacro step's end state, if these steps are executed as part of a StepMacro rather than scenario
     */
    public StepEndState runSteps(ExecutionToken executionToken, List<Object> handlerInstances, List<StepToken> stepList, boolean skip) {
        for (StepToken step : stepList) {
            StepEndState endState = processStep(executionToken, handlerInstances, step, skip);
            switch (endState) {
                case PASSED:
                    break;
                case FAILED:
                    skip = true;//skip (don't execute) the rest of the steps
                    break;
                case UNDEFINED:
                    skip = true;//skip (don't execute) the rest of the steps
                    break;
                case PENDING:
                    skip = true;//skip (don't execute) the rest of the steps
                    break;
                case TIMEOUT:
                    skip = true;//skip (don't execute) the rest of the steps
                    break;
                case SKIPPED:
                case DRYRUN:
                    break;
                default :
                    throw new RuntimeException("Unhandled step state " + endState);

            }
        }

        StepEndState stepMacroEndState = StepMacro.calculateStepMacroEndState(stepList);
        return stepMacroEndState;
    }


    /**
     * @param handlerInstances the objects on which to execute the step (ordered by greatest precidence first)
     * @param step      details of the step to be executed
     * @param skip      is true the step will be skipped if found
     * @return the exit state of the executed step
     */
    private StepEndState processStep(ExecutionToken executionToken, List<Object> handlerInstances, StepToken step, boolean skip) {
        log.trace("Starting to process step " + (step.isStepMacro() ? "macro " : "") + step);
        executionListenerSupport.notifyStepStarted(executionToken, step);

        StepEndState endState;
        if ( step.isStepMacro() ) {
            endState = runSteps(executionToken, handlerInstances, step.getChildSteps(), skip);
        } else {
            endState = processHandlerStep(executionToken, handlerInstances, step, skip);
        }

        step.setEndState(endState);
        executionListenerSupport.notifyStepCompleted(executionToken, step);
        return endState;
    }

    private StepEndState processHandlerStep(ExecutionToken executionToken, List<Object> handlerInstances, StepToken step, boolean skip) {
        //return this at the end
        StepEndState endState = null;

        if (skip) {
            log.debug("Skipping step  " + step);
            //output skipped and don't call the method
            endState = StepEndState.SKIPPED;
            executionToken.incrementStepsSkipped();
        } else {
            log.debug("Processing step " + step);
            //identify what method should be called and its parameters
            StepDefinitionMethodFinder stepDefinitionMethodFinder = new StepDefinitionMethodFinder(handlerInstances, step);
            stepDefinitionMethodFinder.findStepMethod();

            //call the method if found
            if (stepDefinitionMethodFinder.isMethodAvailable()) {
                endState = callStepMethod(executionToken, step, endState, stepDefinitionMethodFinder);
            } else {
                log.debug("Could not find a step method definition for step " + step);
                //no method found yet for this step
                endState = StepEndState.UNDEFINED;
                executionToken.incrementStepsUndefined();
            }
        }
        return endState;
    }

    private StepEndState callStepMethod(ExecutionToken executionToken, StepToken step, StepEndState endState, StepDefinitionMethodFinder stepDefinitionMethodFinder) {
        //setting a pending message in the step annotation implies the step is pending - we don't execute it
        String pendingMessage = stepDefinitionMethodFinder.getMethodToCallPendingMessage();
        if (!pendingMessage.equals(Step.NO_PENDING_MESSAGE)) {
            log.debug("Step has a pending message " + pendingMessage + " skipping step");
            step.setMessage(pendingMessage);
            endState = StepEndState.PENDING;
            executionToken.incrementStepsPending();
        } else {
            if (dryRun) {
                log.debug("Dry Run, so not executing this step");
                step.setMessage("This step is OK");
                endState = StepEndState.DRYRUN;
                executionToken.incrementStepsPassed(); // treat dry run as passed? This state was unsupported in previous results
            } else {
                log.debug("Now executing the step using method " + stepDefinitionMethodFinder.getMethodToCall());
                long startTime = System.currentTimeMillis();
                try {
                    //call the step method using reflection
                    Object result = stepDefinitionMethodFinder.getMethodToCall().invoke(
                            stepDefinitionMethodFinder.getInstanceToCallOn(),
                            stepDefinitionMethodFinder.getMethodCallArgs()
                    );
                    log.debug("Finished executing the step, step passed, result was " + result);
                    if (result != null) {
                        step.setMessage(result.toString());
                    }
                    endState = StepEndState.PASSED;
                    executionToken.incrementStepsPassed();
                } catch (InvocationTargetException e) {
                    log.debug("Step execution failed, we hit an exception invoking the step method");
                    //here if the method called threw an exception
                    if (e.getTargetException() instanceof StepPendingException) {
                        StepPendingException spe = (StepPendingException) e.getTargetException();
                        step.setThrowable(spe);
                        step.setMessage(spe.getMessage());
                        endState = StepEndState.PENDING;
                        executionToken.incrementStepsPending();
                        log.debug("Step Pending Exception prevented execution");
                    } else if (e.getTargetException() instanceof InterruptedException || e.getTargetException() instanceof InterruptedIOException) {
                        if ( interruptingOnTimeout ) {
                            log.warn("Interrupted during step processing, will TIMEOUT remaining steps");
                            interruptingOnTimeout = false;
                            endState = StepEndState.TIMEOUT;
                        } else {
                            log.warn("Interrupted during step processing but this was not due to TIMEOUT, will fail step");
                            endState = StepEndState.FAILED;
                        }
                        executionToken.incrementStepsFailed();
                        step.setMessage(e.getTargetException().getClass().getSimpleName());
                        Thread.currentThread().isInterrupted(); //clear the interrupted status
                    } else if (e.getTargetException() instanceof ThreadDeath ) {
                        //thread has been stopped due to scenario timeout?
                        log.error("ThreadDeath exception during step processing, tests will terminate");
                        throw new ThreadDeath();  //we have to rethrow to actually kill the thread
                    } else {
                        Throwable cause = e.getCause();
                        step.setThrowable(cause);
                        String location = "";
                        if ( ! (cause instanceof ChorusRemotingException) ) {
                            //the remoting exception contains its own location in the message
                            location = ExceptionHandling.getExceptionLocation(cause);
                        }
                        String message = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
                        step.setMessage(location + message);
                        endState = StepEndState.FAILED;
                        executionToken.incrementStepsFailed();
                        log.debug("Exception failed due to exception " + e.getMessage());
                    }
                } catch (Exception e) {
                    log.error(e);
                } finally {
                    step.setTimeTaken(System.currentTimeMillis() - startTime);
                }
            }
        }
        return endState;
    }
}