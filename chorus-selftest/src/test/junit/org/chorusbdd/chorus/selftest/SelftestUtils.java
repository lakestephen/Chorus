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
package org.chorusbdd.chorus.selftest;

import org.chorusbdd.chorus.util.assertion.ChorusAssert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 27/09/12
 * Time: 08:35
 */
public class SelftestUtils {

    private SelftestUtils() {
    }

    public static void checkFileContainsLine(String line, String path) {
           BufferedReader r = null;
           try {
               r = new BufferedReader(new FileReader(new File(path)));
               line = line.trim();

               String l = r.readLine();
               boolean result = false;
               while(l != null) {
                   if ( line.equals(l.trim())) {
                       result = true;
                       break;
                   }
                   l = r.readLine();
               }
               ChorusAssert.assertTrue("check contains line", result);
           } catch ( Exception e) {
               ChorusAssert.fail("File at path " + path + " did not exist or did not contain the line " + line);
           } finally {
               if ( r != null) {
                   try {
                       r.close();
                   } catch (Exception e) {
                   }
               }
           }
       }
}
