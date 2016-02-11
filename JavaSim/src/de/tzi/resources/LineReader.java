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
package de.tzi.resources;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @author Michal Markiewicz
 */
public class LineReader {
	
	private static Logger logger = Logger.getLogger(LineReader.class);

    StringBuffer sb = new StringBuffer();
    final LineListener ll;

    public LineReader(LineListener ll) {
        this.ll = ll;
    }

    public int read(InputStream is) {
    	return read(is, -1);
    }    
    
    public int read(InputStream is, int maxToRead) {
        try {
            int c;
            int read = 0;
            int toRead = Math.min(maxToRead, is.available());
            while ((toRead-- > 0 || maxToRead == -1) && (c = is.read()) != -1) {
                if (c == '\r') {
                    continue;
                }
                if (c == '\n') {
                    ll.acceptLine(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append((char) c);
                }
                read++;
            }
            //Last line without <CRLF>
            if (sb.length() > 0) {
            	ll.acceptLine(sb.toString());
            }
            return read;
        } catch (IOException e) {
            logger.error(e);
            return -1;
        } finally {
        	if (maxToRead < 0) {
        		try {
        			is.close();
        		} catch (IOException e) {
                    logger.error(e);
                    return -1;
        		}
        	}
        }
    }
}
