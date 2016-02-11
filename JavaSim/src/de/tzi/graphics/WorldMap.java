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
import java.util.Map;


/**
 * @author Michal Markiewicz
 *
 */
public class WorldMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1325376689393155415L;

	/**
	 * minX, minY, maxX, maxY
	 */
	double[] boundaries;
	
	/**
	 * Contains reference to section and array with x and y coordinates which refers to positions of cells in a map 
	 */
	Map<RenderingSection, RenderingSection> sectionMapping;
	
	public WorldMap(Map<RenderingSection, RenderingSection> sectionMapping) {
		this.sectionMapping = sectionMapping;
		double minX = Double.MAX_VALUE;        
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		for (RenderingSection rs : sectionMapping.keySet()) {
			int size = rs.getSectionLength();
			for (int i = 0; i < size; i ++) {
				double x = rs.getMapX(i);
				double y = rs.getMapY(i);
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
			}
		}
		int i=0;
		boundaries = new double[4];
		boundaries[i++] = minX;
		boundaries[i++] = minY;
		boundaries[i++] = maxX;
		boundaries[i++] = maxY;
	}
	
	public Map<RenderingSection, RenderingSection> getSectionMapping() {
		return sectionMapping;
	}
}
