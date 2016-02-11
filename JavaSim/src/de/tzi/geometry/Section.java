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


/**
 * @author Michal Markiewicz
 *
 */
public class Section implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 245007601201811096L;
	
	private int from;
	private int to;
	private int length;
	private float angle;
	
	public Section(int from, int to, int length, float angle) {
		this.from = from;
		this.to = to;
		this.length = length;
		this.angle = angle;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public int getLength() {
		return length;
	} 
	
	public float getAngle() {
		return angle;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(from).append('-').append(to).append(' ').append(length).append('m').append(' ').append(angle).append('\u00B0');
		return sb.toString();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Section))
			return false;
		Section s2 = (Section)obj;
		return (from == s2.from) && (to == s2.to);
	}
	
	public int hashCode() {
		return from * to;
	}
	
	
}
