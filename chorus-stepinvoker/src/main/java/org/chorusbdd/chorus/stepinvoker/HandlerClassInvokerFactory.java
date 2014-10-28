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
package org.chorusbdd.chorus.stepinvoker;

import org.chorusbdd.chorus.annotations.PassesWithin;
import org.chorusbdd.chorus.annotations.Step;
import org.chorusbdd.chorus.logging.ChorusLog;
import org.chorusbdd.chorus.logging.ChorusLogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
* User: nick
* Date: 24/09/13
* Time: 18:46
*/
public class HandlerClassInvokerFactory implements InvokerFactory {

    private static ChorusLog log = ChorusLogFactory.getLog(HandlerClassInvokerFactory.class);
    private Object handlerInstance;

    public HandlerClassInvokerFactory(Object handlerInstance) {
        this.handlerInstance = handlerInstance;
    }

    public List<StepInvoker> createStepInvokers() {
        List<StepInvoker> stepInvokers = new ArrayList<StepInvoker>();

        //if the handler implements StepInvokerProvider then get the step invokers directly
        addStepProviderInvokers(stepInvokers);

        //add step invokers for any annotated step methods
        addMethodStepInvokers(stepInvokers);

        return stepInvokers;
    }

    private void addStepProviderInvokers(List<StepInvoker> stepInvokers) {
        if (handlerInstance instanceof StepInvokerProvider) {
            List<StepInvoker> invokersByInterface = ((StepInvokerProvider) handlerInstance).getStepInvokers();
            stepInvokers.addAll(invokersByInterface);
            log.debug("Added " + invokersByInterface.size() + " step invokers for handler class " +
                    handlerInstance.getClass().getSimpleName() + " step provider");
        }
    }

    private void addMethodStepInvokers(List<StepInvoker> stepInvokers) {
        List<StepInvoker> invokersForMethods = getStepInvokersForAnnotatedMethods();
        stepInvokers.addAll(invokersForMethods);
        log.debug("Added " + invokersForMethods.size() + " step invokers for handler class " +
                handlerInstance.getClass().getSimpleName() + " annotated methods");
    }

    private List<StepInvoker> getStepInvokersForAnnotatedMethods() {
        List<StepInvoker> stepInvokers = new ArrayList<StepInvoker>();
        Method[] methods = handlerInstance.getClass().getMethods();
        for (Method method : methods) {
            //only check methods with Step annotation
            Step stepAnnotationInstance = method.getAnnotation(Step.class);
            if (stepAnnotationInstance != null) {
                log.debug("Found @Step annotated method " + method + " on handler " + handlerInstance);
                StepInvoker invoker = createInvoker(handlerInstance, method);
                stepInvokers.add(invoker);
            }
        }
        return stepInvokers;
    }

    public StepInvoker createInvoker(Object handlerInstance, Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();

        Step stepAnnotation = method.getAnnotation(Step.class);
        Pattern stepPattern = Pattern.compile(stepAnnotation.value());

        String pendingText = stepAnnotation.pending();
        pendingText = ( Step.NO_PENDING_MESSAGE.equals(pendingText)) ? null : pendingText;

        StepInvoker simpleMethodInvoker = new SimpleMethodInvoker(handlerInstance, method, stepPattern, pendingText);

        //if the step is annotated with @PassesWithin then we wrap the simple invoker with the appropriate
        //PolledInvoker
        for ( Annotation a : annotations) {
            if ( a.annotationType() == PassesWithin.class) {
                PassesWithin passesWithin = (PassesWithin) a;
                switch(passesWithin.pollMode()) {
                    case UNTIL_FIRST_PASS:
                        simpleMethodInvoker = new UntilFirstPassInvoker(simpleMethodInvoker, passesWithin);
                        break;
                    case PASS_THROUGHOUT_PERIOD:
                        simpleMethodInvoker =  new PassesThroughoutInvoker(simpleMethodInvoker, passesWithin);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown mode " + passesWithin.pollMode());
                }
            }
        }
        return simpleMethodInvoker;
    }

}
