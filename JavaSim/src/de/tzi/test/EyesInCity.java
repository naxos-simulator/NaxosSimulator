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
 * 2012-07-12
 */
package de.tzi.test;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generates city with roads that intersect with each other at angles  90 degrees 
 * 
 * @author Michal Markiewicz
 *
 */
public class EyesInCity extends NoCollisionCityGenerator {
	
	Set<Integer> holes = new TreeSet<Integer>();
	double holeProbability = 0.15;
	
	protected String getGenDir() {
		return "data/GEN09/";
	}
	
	protected int crossingsCount() {
		return 33;
	}
	
	protected int segmentLength() {
		return 47;
	}
	
	public EyesInCity() {
		int cs = crossingsSquared();
		for (int i = 0; i < cs; i++) {
			if (Math.random() < holeProbability) {
				holes.add(i);
			}
		}
	}
	
	protected int getSibling(int angle, int i) {
		if (holes.contains(i))
			return HAS_NO_SIBLING;
		int res = super.getSibling(angle, i);
		if (holes.contains(res))
			return HAS_NO_SIBLING;
		return res;
	}
	
	public static void main(String[] args) throws IOException {
		new EyesInCity().generate();
	}

}
