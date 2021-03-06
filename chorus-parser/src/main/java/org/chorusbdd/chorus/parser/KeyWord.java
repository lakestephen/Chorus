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
package org.chorusbdd.chorus.parser;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 23/02/13
 * Time: 22:55
 *
 * Key words from Chorus' extended gherkin syntax
 */
public enum KeyWord {
    
    Uses("Uses:", false),
    Configurations("Configurations:", false),
    Feature("Feature:", false),
    Background("Background:", true),
    Scenario("Scenario:", true),
    ScenarioOutline("Scenario-Outline:", true),
    Examples("Examples:", false),
    StepMacro("Step-Macro:", true),
    FeatureStart("Feature-Start:", true),
    FeatureEnd("Feature-End:", true);

    public static final String FEATURE_START_SCENARIO_NAME = "Feature-Start";
    public static final String FEATURE_END_SCENARIO_NAME = "Feature-End";

    private String keyWord;
    private boolean supportsDirectives;

    KeyWord(String keyWord, boolean supportsDirectives) {
        this.keyWord = keyWord;
        this.supportsDirectives = supportsDirectives;
    }

    public String stringVal() {
        return keyWord;
    }

    public boolean matchesLine(String line) {
        return line.startsWith(keyWord);
    }

    public boolean is(KeyWord k) {
        return this == k;
    }

    public boolean isSupportsDirectives() {
        return supportsDirectives;
    }

    public static KeyWord getKeyWord(String line) {
        KeyWord result = null;
        for ( KeyWord k : values() ) {
            if ( line.startsWith(k.keyWord)) {
                result = k;
                break;
            }
        }
        return result;
    }

    public String toString() {
        return keyWord;
    }
}
