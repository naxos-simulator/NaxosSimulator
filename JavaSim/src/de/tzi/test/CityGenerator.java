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
 * 2012-06-26
 */
package de.tzi.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Generates city with roads that intersect with each other at angles  90 degrees 
 * 
 * @author Michal Markiewicz
 *
 */
public class CityGenerator {

	static {
		Locale.setDefault(Locale.US);
	}
	
	final static int CROSSINGS = 3;//6
	
	final static int SEGMENT_LENGTH = 10;//400;//100;//15;//50
	final static int CROSSINGS_SQUARED = CROSSINGS * CROSSINGS;
	
	final static String DIR = "data/GEN17/";

	final static String CROSSING_FILE_NAME = DIR + "crossings.txt";
	final static String SEGMENTS_FILE_NAME = DIR + "segments.txt";
	final static String MAP_SEGMENTS_FILE_NAME = DIR + "map_segments.txt";

	final static char FIELDS_SEPARATOR = '\t';
	
	final static int DIRECTION_E = 0;
	final static int DIRECTION_W = 180;
	final static int DIRECTION_N = 90;
	final static int DIRECTION_S = 270;
	
	final static int HAS_NO_SIBLING = -1; 
	
	final static boolean CAN_TURN_AROUND = true;//change in code below!
	
	//a single entry means that it is possible to go east and then east (first entry)
	final static int[][] allPossibilities = new int[][]{
			{DIRECTION_E, DIRECTION_E},
			{DIRECTION_E, DIRECTION_N},
			{DIRECTION_E, DIRECTION_S},

			{DIRECTION_E, DIRECTION_W},
			
			{DIRECTION_W, DIRECTION_W},
			{DIRECTION_W, DIRECTION_N},
			{DIRECTION_W, DIRECTION_S},
			
			{DIRECTION_W, DIRECTION_E},

			{DIRECTION_N, DIRECTION_N},
			{DIRECTION_N, DIRECTION_W},
			{DIRECTION_N, DIRECTION_E},
			
			{DIRECTION_N, DIRECTION_S},

			{DIRECTION_S, DIRECTION_S},
			{DIRECTION_S, DIRECTION_E},
			{DIRECTION_S, DIRECTION_W},
			
			{DIRECTION_S, DIRECTION_N},
			
			};
	
	public static void main(String[] args) throws IOException {
		(new File(DIR)).mkdirs();
		generateSegments();
		generatePassingPossibilities();
	}
	
	private static void generatePassingPossibilities() throws IOException {
		PrintStream fw = new PrintStream(CROSSING_FILE_NAME);
		int j;
		for (int i = 0; i < CROSSINGS_SQUARED; i++) {
			for (int k = 0; k < allPossibilities.length; k++) {
				int a = allPossibilities[k][0];
				int b = allPossibilities[k][1];
				addPassingPossibility(fw, i, j = getSibling(a, i), getSibling(b, j));
			}
		}
		fw.close();
	}

	private static void addPassingPossibility(PrintStream fw, int from, int by, int to) 
			throws IOException {
		if (from == HAS_NO_SIBLING || by == HAS_NO_SIBLING || to == HAS_NO_SIBLING)
			return;
		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(by)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(to)).append('\n');
	}
	
	private static void generateSegments() throws IOException  {
		PrintStream fw = new PrintStream(SEGMENTS_FILE_NAME);
		PrintStream f2 = new PrintStream(MAP_SEGMENTS_FILE_NAME);
		for (int i = 0; i < CROSSINGS_SQUARED; i++) {
			addSegment(fw, i, getSibling(DIRECTION_E, i), DIRECTION_E, SEGMENT_LENGTH);
			addSegment(fw, i, getSibling(DIRECTION_W, i), DIRECTION_W, SEGMENT_LENGTH);
			addSegment(fw, i, getSibling(DIRECTION_S, i), DIRECTION_S, SEGMENT_LENGTH);
			addSegment(fw, i, getSibling(DIRECTION_N, i), DIRECTION_N, SEGMENT_LENGTH);
			addSegmMap(f2, i, getSibling(DIRECTION_E, i), DIRECTION_E, SEGMENT_LENGTH);
			addSegmMap(f2, i, getSibling(DIRECTION_W, i), DIRECTION_W, SEGMENT_LENGTH);
			addSegmMap(f2, i, getSibling(DIRECTION_S, i), DIRECTION_S, SEGMENT_LENGTH);
			addSegmMap(f2, i, getSibling(DIRECTION_N, i), DIRECTION_N, SEGMENT_LENGTH);
		}
		fw.close();
		f2.close();
	}

	//r - checking
	//> x <- read.csv("/tmp/map_segments.txt", header=F, sep="\t")
	//> plot(x)
	
	final static double LANE_SHIFT = 0.05;
	private static void addSegmMap(PrintStream fw, int from, int to,
			int angle, int segmentLength) throws IOException {
		if (from == HAS_NO_SIBLING || to == HAS_NO_SIBLING)
			return;
		int rowF = from / CROSSINGS;
		int colF = from % CROSSINGS;
		int rowT = to / CROSSINGS;
		int colT = to % CROSSINGS;
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

	private static int getSibling(int angle, int i) {
		if (i == HAS_NO_SIBLING)
			return HAS_NO_SIBLING;
		if (angle == DIRECTION_E && i % CROSSINGS  + 1 < CROSSINGS)
			return i + 1;
		if (angle == DIRECTION_W  && i % CROSSINGS > 0)
			return i - 1;
		if (angle == DIRECTION_S &&  i / CROSSINGS  < CROSSINGS && i + CROSSINGS < CROSSINGS_SQUARED)
			return i + CROSSINGS;
		if (angle == DIRECTION_N &&  i / CROSSINGS > 0)
			return i - CROSSINGS;
		return HAS_NO_SIBLING;
	}

	private static void addSegment(PrintStream fw, int from, int to, int angle,
			int segmentLength) throws IOException {
		if (from == HAS_NO_SIBLING || to == HAS_NO_SIBLING)
			return;
		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(to)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(segmentLength)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(angle)).append('\n');
	}	

}
