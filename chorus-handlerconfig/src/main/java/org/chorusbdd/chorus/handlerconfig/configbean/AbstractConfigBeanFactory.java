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
package org.chorusbdd.chorus.handlerconfig.configbean;

import org.chorusbdd.chorus.annotations.Scope;
import org.chorusbdd.chorus.util.ChorusException;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: GA2EBBU
 * Date: 21/09/12
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
public class AbstractConfigBeanFactory {

    protected int parseIntProperty(String value, String propertyName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ChorusException("Could not parse property '" + propertyName + "' with value '" + value + "' as an integer");
        }
    }

    protected boolean parseBooleanProperty(String value, String propertyKey) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            throw new ChorusException("Failed to parse property " + propertyKey + " with value '" + value + "' as boolean");
        }
    }

    protected static Scope parseScope(String value) {
        Scope processScope = Scope.valueOf(value.toUpperCase().trim());
        if ( processScope == null) {
            throw new ChorusException(
                    "Failed to parse property processScope with value '" + value + "', should be one of: "
                            + Arrays.asList(Scope.values())
            );
        }
        return processScope;
    }
}
