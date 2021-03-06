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
package org.chorusbdd.chorus.results;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * User: nick
 * Date: 17/01/13
 * Time: 18:22
 */
public class TestTokenVisitor extends Assert {

    private TestSuite testSuite;
    private ScenarioToken scenarioToken;
    private ScenarioToken scenarioTwo;
    private StepToken stepOne;
    private StepToken stepTwo;
    private StepToken stepThree;
    private FeatureToken featureToken;
    private FeatureToken featureTwo;
    private ExecutionToken executionToken;

    @Before
    public void doBefore() {
        scenarioToken = new ScenarioToken();
        scenarioToken.setName("Test Scenario");
        stepOne = scenarioToken.addStep(StepToken.createStep("If", "I create a step"));
        stepTwo = scenarioToken.addStep(StepToken.createStep("If", "I create a second step"));

        scenarioTwo = new ScenarioToken();
        stepThree = scenarioTwo.addStep(StepToken.createStep("If", "I add a step to scenario 2"));
        featureToken = new FeatureToken();
        featureToken.addScenario(scenarioToken);
        featureToken.addScenario(scenarioTwo);

        featureTwo = new FeatureToken();

        executionToken = new ExecutionToken("My test suite name");
        testSuite = new TestSuite(executionToken, Arrays.asList(featureToken, featureTwo));
    }

    @Test
    public void testVisitation() {
        MockVisitor m = new MockVisitor();
        testSuite.accept(m);
        assertEquals("Expected visit count", 9, m.visitations);
    }

    private class MockVisitor extends Assert implements TokenVisitor {

        private int visitations;

        private List<Token> expectedTokenOrder = new LinkedList<>(
                Arrays.asList(
                        (Token) executionToken, executionToken.getResultsSummary(), featureToken, scenarioToken, stepOne, stepTwo,
                        scenarioTwo, stepThree, featureTwo
                ));

        public void visit(ExecutionToken executionToken) {
            checkExpectedToken(executionToken);
        }

        private void checkExpectedToken(Token token) {
            Token t = expectedTokenOrder.remove(0);
            assertSame(token, t);
            visitations++;
        }

        public void visit(ResultsSummary resultsSummary) {
            checkExpectedToken(resultsSummary);
        }

        public void visit(FeatureToken featureToken) {
            checkExpectedToken(featureToken);
        }

        public void visit(ScenarioToken scenarioToken) {
            checkExpectedToken(scenarioToken);
        }

        public void visit(StepToken stepToken) {
            checkExpectedToken(stepToken);
        }
    }
}
