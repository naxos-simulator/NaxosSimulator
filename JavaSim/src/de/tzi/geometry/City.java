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
 * 2012-05-02
 */
package de.tzi.geometry;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Michal Markiewicz
 *
 */
public class City implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8268549847073977492L;
	
	Passage[] crossings;
	Section[] sections;
	
	public City(Passage[] crossings, Section[] segments) {
		this.crossings = crossings;
		this.sections = segments;
	}

	public Passage[] getCrossings() {
		return crossings;
	}

	public Section[] getSections() {
		return sections;
	}
	
	public long getTotalNetworkLength() {
		long res = 0;
		for (Section section : sections) {
			res += section.getLength();
		}
		return res;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nCrossings: \n");
		sb.append(Arrays.toString(crossings));
		sb.append("\nSections: \n");
		sb.append(Arrays.toString(sections));
		sb.append("\n");
		return sb.toString();
	}
	
}
