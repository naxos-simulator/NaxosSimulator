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
 * 2012-05-25
 */
package de.tzi.graphics;

import java.io.Serializable;

import de.tzi.geometry.Section;

/**
 * @author Michal Markiewicz
 * 
 */
public class RenderingSection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6788401958585055372L;

	// x and y interleaved 
	int[] spArr;
	//map position (originals)
	double[] mpArr;

	/**
	 * Used for querying purposes in HashMap and HashSets
	 * 
	 * @param from
	 * @param to
	 */
	public RenderingSection(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public RenderingSection(Section section) {
		this.section = section;
		from = section.getFrom();
		to = section.getTo();
		mpArr = new double[section.getLength() * 2];
		spArr = new int[mpArr.length];
	}

	Section section;

	int from;
	int to;

	public int hashCode() {
		return from * to;
	}

	public boolean equals(Object obj) {
		RenderingSection sw = (RenderingSection) obj;
		return (sw.from == from) && (sw.to == to);
	}

	public double[] getMapPositionsArray() {
		return mpArr;
	}

	
	public int[] getScreenPositionsArray() {
		return spArr;
	}
	
	public Section getSection() {
		return section;
	}
	
	public String toString() {
		if (section == null)
			return "Lookup rendering section for values from: "+from+" to: "+to;
		else
			return "Rendering section "+section.toString();	
	}
	
	public double getMapX(int i) {
		return mpArr[2 * i];
	}

	public double getMapY(int i) {
		return mpArr[2 * i + 1];
	}
	
	public void setMapX(int pos, double val) {
		mpArr[2 * pos] = val;
	}

	public void setMapY(int pos, double val) {
		mpArr[2 * pos + 1] = val;
	}
	
	
	public int getScreenX(int i) {
		return spArr[2 * i];
	}

	public int getScreenY(int i) {
		return spArr[2 * i + 1];
	}
	
	public void setScreenX(int pos, int val) {
		spArr[2 * pos] = val;
	}

	public void setScreenY(int pos, int val) {
		spArr[2 * pos + 1] = val;
	}
	
	public int getSectionLength() {
		return section.getLength();
	}
}
