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
 * 2012-05-13
 */

package de.tzi.traffic;

import de.tzi.traffic.navigation.Edge;

/**
 * @author Michal Markiewicz
 *
 */

public class PassingPossibility implements Edge {
	
	final int id;
	
	final Segment input;
	final Segment output;
	
	public PassingPossibility(int id, Segment input, Segment output) {
		this.id = id;
		this.input = input;
		this.output = output;
	}

	public boolean excludes(PassingPossibility p2) {		
		int in = input.getSection().getFrom();
		int out  = output.getSection().getTo();
		int in2 = p2.input.getSection().getFrom();
		int out2  = p2.output.getSection().getTo();
		boolean opposite = (in == out2) && (out == in2);
		if (opposite)
			return false;
		boolean sameStart = (in == in2);
		if (sameStart)
			return false;
		//not opposite, not same start
		boolean sameEnd = (out == out2);
		if (sameEnd)
			return true;
		//not opposite, not same start, different endings
		float i1 = input.getSection().getAngle();
		float o1 = output.getSection().getAngle();
		float i2 = p2.input.getSection().getAngle();
		float o2 = p2.output.getSection().getAngle();
		return areInOrder(i1, i2, o1) == areInOrder(i1, o2, o2);
	}
	
	/**
	 * Proper configurations are:
	 * <ul>
	 * <li>0 A B C</li>
	 * <li>A 0 B C</li>
	 * <li>A B 0 C</li>
	 * <li>A B C 0</li>
	 * </ul>
	 * 
	 * which leads to:
	 * 
	 * <ul>
	 * <li>0 A B C</li>
	 * <li>0 B C A</li>
	 * <li>0 C A B</li>
	 * </ul>
	 * 
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return whenever a, b and c are placed in that order on a cricle
	 */
	private boolean areInOrder(float a, float b, float c) {
		return (((a < b) && (b < c)) ||
				((b < c) && (c < a)) ||
				((c < a) && (a < b)));
	}
	
	public String toString() {
		int in = input.getSection().getFrom();
		int by  = input.getSection().getTo();
		int out  = output.getSection().getTo();
		return in+"("+by+")"+out;
	}
	
	public Segment getInput() {
		return input;
	}
	
	public Segment getOutput() {
		return output;
	}
	
	public int getId() {
		return id;
	}

	public int hashCode() {
		return id;
	}
	
	public boolean equals(Object obj) {
		return id == ((PassingPossibility)obj).id;
	}
}