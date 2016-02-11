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
public class Passage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6361956423938329154L;
	
	private int from;
	private int by;
	private int to;
	
	public Passage(int from, int by, int to) {
		this.from = from;
		this.by = by;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getBy() {
		return by;
	}

	public int getTo() {
		return to;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(from).append('-').append(by).append('-').append(to);
		return sb.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof Passage){
			Passage p2 = (Passage)obj;
			return p2.from == from  && p2.by == by && to == p2.to;
		}
		return super.equals(obj);
	}
	public int hashCode() {
		return from ^ by ^ to;
	}
	
}
