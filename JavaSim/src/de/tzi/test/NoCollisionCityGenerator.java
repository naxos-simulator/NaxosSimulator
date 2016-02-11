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
import java.io.PrintStream;
import java.util.Locale;

/**
 * Generates city with roads that intersect with each other at angles  90 degrees 
 * 
 * @author Michal Markiewicz
 *
 */
public class NoCollisionCityGenerator  {

	static {
		Locale.setDefault(Locale.US);
	}
	
	
	protected int crossingsCount() {
		return 6;
	}
	
	protected int crossingsSquared() {
		return crossingsCount()*crossingsCount();
	}
	
	protected int segmentLength() {
		return 20;//200;
	}
	
	protected String getGenDir() {
		return "data/GEN18/";
	}
	
	private String getCrossingsFileName() {
		return getGenDir()+"crossings.txt";
	}

	private String getSegmentsFileName() {
		return getGenDir()+"segments.txt";
	}

	private String getMapSegmentsFileName() {
		return getGenDir()+"map_segments.txt";
	}

	final static char FIELDS_SEPARATOR = '\t';
	
	final static int DIR_E = 0;
	final static int DIR_W = 180;
	final static int DIR_N = 90;
	final static int DIR_S = 270;
	
	protected final static int HAS_NO_SIBLING = -1; 
	
	final static int[][][] possibilities = new int[][][] {
		// (0) (1)
		// (2) (3)
 		new int[][] { new int[] {DIR_N, DIR_W}, new int[] {DIR_N, DIR_N}, new int[] {DIR_E, DIR_E}, new int[] {DIR_E, DIR_S} },
		new int[][] { new int[] {DIR_E, DIR_E}, new int[] {DIR_S, DIR_W}, new int[] {DIR_E, DIR_N}, new int[] {DIR_S, DIR_S} },
		new int[][] { new int[] {DIR_W, DIR_W}, new int[] {DIR_W, DIR_S}, new int[] {DIR_N, DIR_N}, new int[] {DIR_N, DIR_E} },
		new int[][] { new int[] {DIR_W, DIR_W}, new int[] {DIR_W, DIR_N}, new int[] {DIR_S, DIR_S}, new int[] {DIR_S, DIR_E} },
	};
	
	final static int[][] segments = new int[][] {
		// (0) (1)
		// (2) (3)
		new int[] {DIR_E, DIR_N},
		new int[] {DIR_E, DIR_S},
		new int[] {DIR_W, DIR_N},
		new int[] {DIR_W, DIR_S},
	};

	private int getType(int crossingNo) {
		return 2 * ((crossingNo / crossingsCount()) % 2) + ((crossingNo % crossingsCount()) % 2);
	}
	
	public static void main(String[] args) throws IOException {
		new NoCollisionCityGenerator().generate();
	}
	
	public void generate() throws IOException {
		generateSegments();
		generatePassingPossibilities();
	}
	
	private void generatePassingPossibilities() throws IOException {
		PrintStream fw = new PrintStream(getCrossingsFileName());
		int cs = crossingsSquared();
		for (int i = 0; i < cs; i++) {
			int type = getType(i);
			for (int k = 0; k < possibilities[type].length; k++) {
				int j;
				addPassingPossibility(fw, i, j = getSibling(possibilities[type][k][0], i), getSibling(possibilities[type][k][1], j));
			}
		}
		fw.close();
	}

	private void addPassingPossibility(PrintStream fw, int from, int by, int to) 
			throws IOException {
		if (from == HAS_NO_SIBLING || by == HAS_NO_SIBLING || to == HAS_NO_SIBLING)
			return;
		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(by)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(to)).append('\n');
	}
	
	private void generateSegments() throws IOException  {
		PrintStream fw = new PrintStream(getSegmentsFileName());
		PrintStream f2 = new PrintStream(getMapSegmentsFileName());
		int cs = crossingsSquared();
		for (int i = 0; i < cs; i++) {
			int type = getType(i);
			for (int j = 0; j < segments[type].length; j++) {
				addSegment(fw, i, getSibling(segments[type][j], i), segments[type][j], segmentLength());
				addSegmMap(f2, i, getSibling(segments[type][j], i), segments[type][j], segmentLength());
			}
		}
		fw.close();
		f2.close();
	}

	//r - checking
	//> x <- read.csv("/tmp/map_segments.txt", header=F, sep="\t")
	//> plot(x)
	
	final double LANE_SHIFT = 0;//0.05;
	private void addSegmMap(PrintStream fw, int from, int to,
			int angle, int segmentLength) throws IOException {
		if (from == HAS_NO_SIBLING || to == HAS_NO_SIBLING)
			return;
		int cs = crossingsCount();
		int rowF = from / cs;
		int colF = from % cs;
		int rowT = to / cs;
		int colT = to % cs;
		int xDiff = colT - colF;
		int yDiff = rowT - rowF;
		double x = colF;
		double y = rowF;
		double xStep = (double)xDiff / segmentLength;
		double yStep = (double)yDiff / segmentLength;
		if (xStep == 0) {
			if (yDiff > 0)
				x += LANE_SHIFT;
			else
				x -= LANE_SHIFT;
		} else if (yStep == 0) {
			if (xDiff < 0)
				y += LANE_SHIFT;
			else
				y -= LANE_SHIFT;
		}
		for (int i = 0; i < segmentLength; i++) {
	 		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
			fw.append(String.valueOf(to)).append(FIELDS_SEPARATOR);
			fw.append(String.valueOf(i)).append(FIELDS_SEPARATOR);
			fw.printf("%3.4f", (x+=xStep)).append(FIELDS_SEPARATOR);
			fw.printf("%3.4f", (y+=yStep)).append('\n');
		}
	}

	protected int getSibling(int angle, int i) {
		if (i == HAS_NO_SIBLING)
			return HAS_NO_SIBLING;
		int cs = crossingsSquared();
		int c = crossingsCount();
		if (angle == DIR_E && i % c  + 1 < c)
			return i + 1;
		if (angle == DIR_W  && i % c > 0)
			return i - 1;
		if (angle == DIR_S &&  i / c  < c && i + c < cs)
			return i + c;
		if (angle == DIR_N &&  i / c > 0)
			return i - c;
		return HAS_NO_SIBLING;
	}

	private void addSegment(PrintStream fw, int from, int to, int angle,
			int segmentLength) throws IOException {
		if (from == HAS_NO_SIBLING || to == HAS_NO_SIBLING)
			return;
		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(to)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(segmentLength)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(angle)).append('\n');
	}	

}
