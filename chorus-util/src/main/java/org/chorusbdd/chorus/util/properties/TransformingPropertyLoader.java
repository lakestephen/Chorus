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
package org.chorusbdd.chorus.util.properties;

import org.chorusbdd.chorus.util.function.BiFunction;
import org.chorusbdd.chorus.util.function.Tuple2;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by GA2EBBU on 09/01/2015.
 *
 * Apply a function to convert the map entries from a source Properties object into a Properties instance with modified values
 */
class TransformingPropertyLoader implements PropertyLoader {

    private PropertyLoader wrappedLoader;
    private BiFunction<String, String, Tuple2<String, String>> mappingFunction;

    /**
     * @param mappingFunction a function which takes key and value from source map entry and returns a Tuple2 containing a new key and value
     */
    public TransformingPropertyLoader(PropertyLoader wrappedLoader,
                                      BiFunction<String, String, Tuple2<String,String>> mappingFunction) {
        this.wrappedLoader = wrappedLoader;
        this.mappingFunction = mappingFunction;
    }

    public Properties loadProperties()  {
        Properties p = wrappedLoader.loadProperties();
        Properties dest = new Properties();
        for ( Map.Entry<Object,Object> mapEntry : p.entrySet()) {
            Tuple2<String,String> newEntry = mappingFunction.apply(
                mapEntry.getKey().toString(),
                mapEntry.getValue().toString()
            );
            dest.put(newEntry.getOne(), newEntry.getTwo());
        }
        return dest;
    }

}
