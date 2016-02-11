/*
 * Copyright (c) 2015 Michal Markiewicz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */ 


/**
 * 2009-11-02
 */
package de.tzi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Markiewicz
 */
public class TextUtils {

	public final static String TAB = "\t";
	
    public static String readToEnd(String resourceName) throws IOException {
        InputStream is = new Object().getClass().getResourceAsStream(resourceName);
        if (is == null)
            return "";
        int c;
        StringBuffer sb = new StringBuffer();
        while ((c = is.read()) != -1) {
            sb.append((char)c);
        }
        is.close();
        return sb.toString();
    }

    public static boolean isEmpty(String str) {
    	return str == null || str.length() == 0;
    }

    public static String[] expandString(String line, String delim) {
    	return readFields(line, delim, 0);
	}

	private static String[] readFields(final String str, final String delim, int from) {
		if (str == null)
			return new String[0];
		List<String> v = new ArrayList<String>();
		int to;
		do {
			to = str.indexOf(delim, from);
			if (to == -1) {
				v.add(str.substring(from, str.length()));
			} else {
				v.add(str.substring(from, to));
				from = to + 1;
			}
		} while (to != -1);
        String[] res = new String[v.size()];
        int i = 0;
        for (String s : v) {
			res[i++] = s;
		}
		return res;
	}


}
