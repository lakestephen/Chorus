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
package org.chorusbdd.chorus.util;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 19/06/12
 * Time: 08:05
 */
public class ChorusConstants {

    public static final String DEFAULT_SUITE_NAME = "Test Suite";

    public static final String[] BUILT_IN_HANDLER_PACKAGE_PREFIXES = new String[] {
        "org.chorusbdd.chorus.handlers",
        "org.chorusbdd.chorus.selftest"
    };

    public static final String JMX_EXPORTER_NAME = "org.chorusbdd.chorus:name=chorus_exporter";

    public static final String JMX_EXPORTER_ENABLED_PROPERTY = "org.chorusbdd.chorus.jmxexporter.enabled";

    public static final String CHORUS_ROOT_PACKAGE = "org.chorusbdd.chorus";
    public static final String[] ANY_PACKAGE = new String[] {".*"};
    /**
     * A special config name which is used to define database connection properties to
     * load configs from the database
     */
    public static final String DATABASE_CONFIGS_PROPERTY_GROUP  = "dbproperties";
    /**
     * The name of the properties group which may be defined to supply default settings for other configs
     */
    public static final String DEFAULT_PROPERTIES_GROUP = "default";
}
