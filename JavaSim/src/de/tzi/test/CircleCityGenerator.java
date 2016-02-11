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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.tzi.geometry.Passage;

/**
 * Generates city with roads that intersect with each other at angles  90 degrees 
 * 
 * @author Michal Markiewicz
 *
 */
public class CircleCityGenerator {

	
	//     /-------7------\
	//    |      /-5-\     |
	//    1  -  2  -  3  - 4
	//    |      \-6-/     | 
	//     \-------8------/
	
	static int[] pairs = new int[] {
		1,7,	7,4,	4,8,	8,1,
		1,2,	2,3,	3,4,
		2,5,	5,3,	3,6,	6,2,
		4,3,	3,2,	2,1,
	};
	static Passage[] pps;
	static {
		int MAX_ID = 0;
		for (int i = 0; i < pairs.length; i++) {
			MAX_ID = Math.max(MAX_ID, pairs[i]);
		}
		MAX_ID++;
		Set<Passage> s = new HashSet<Passage>();
		for (int i = 0; i < pairs.length / 2; i++) {
			int from = pairs[2*i]; 
			int by = pairs[2*i + 1]; 
			for (int j = 0; j < pairs.length / 2; j++) {
				int by2 = pairs[2*j];
				int to = pairs[2*j + 1];
				if (by != by2) {
					continue;
				}
				s.add(new Passage(from, by, to));
			}
		}
		pps = s.toArray(new Passage[0]);
	}
	
//	static Passage[] pps =  new Passage[] { //(int from, int by, int to)
//			new Passage(1, 7, 4),
//			new Passage(7, 4, 8),
//			new Passage(4, 8, 1),
//			new Passage(8, 1, 7),
//			new Passage(1, 2, 5),
//			new Passage(2, 5, 3),
//			new Passage(5, 3, 4),
//			new Passage(1, 2, 6),
//			new Passage(2, 6, 3),
//			new Passage(6, 3, 4),
//			new Passage(1, 2, 3),
//			new Passage(2, 3, 4),
//			new Passage(4, 3, 2),
//			new Passage(3, 2, 1),
//			new Passage(3, 4, 8),
//	};
	
	static class Coords {
		public int id;
		public int x;
		public int y;
		public Coords(int id, int x, int y) {
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}
	static Coords[] coords = new Coords[] {
		new Coords(1, 0, 2),
		new Coords(2, 2, 2),
		new Coords(3, 4, 2),
		new Coords(4, 6, 2),
		new Coords(5, 3, 3),
		new Coords(7, 3, 4),
		new Coords(6, 3, 1),
		new Coords(8, 3, 0),
	};
	
	static {
		Locale.setDefault(Locale.US);
	}
	
	final static int UNIT_LENGTH = 15;//15;//100
	
	final static String DIR = "data/GEN14/";

	final static String CROSSING_FILE_NAME = DIR + "crossings.txt";
	final static String SEGMENTS_FILE_NAME = DIR + "segments.txt";
	final static String MAP_SEGMENTS_FILE_NAME = DIR + "map_segments.txt";

	final static char FIELDS_SEPARATOR = '\t';
	
		public static void main(String[] args) throws IOException {
		(new File(DIR)).mkdirs();
		generateSegments();
		generatePassingPossibilities();
	}
	
	private static void generatePassingPossibilities() throws IOException {
		PrintStream fw = new PrintStream(CROSSING_FILE_NAME);
		for (int i = 0; i < pps.length; i++) {
			addPassingPossibility(fw, pps[i].getFrom(), pps[i].getBy(), pps[i].getTo());
		}
		fw.close();
	}

	private static void addPassingPossibility(PrintStream fw, int from, int by, int to) 
			throws IOException {
		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(by)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(to)).append('\n');
	}
	
	private static void generateSegments() throws IOException  {
		PrintStream fw = new PrintStream(SEGMENTS_FILE_NAME);
		PrintStream f2 = new PrintStream(MAP_SEGMENTS_FILE_NAME);
		int MAX_ID = 0;
		for (int i = 0; i < coords.length; i++) {
			MAX_ID = Math.max(MAX_ID, coords[i].id);
		}
		MAX_ID++;
		Map<Integer, Coords> ids = new TreeMap<Integer, Coords>();
		for (int i = 0; i < coords.length; i++) {
				ids.put(coords[i].id, coords[i]);
		}
		Set<Integer> pairsServed = new TreeSet<Integer>();
		for (int i = 0; i < pps.length; i++) {
			int start = pps[i].getFrom();
			int stop = pps[i].getBy();
			for (int t = 0; t <= 1; t++) {
				if (!pairsServed.contains(MAX_ID * start + stop)) {
					pairsServed.add(MAX_ID * start + stop);
					int angle =(int)Math.round(180 * Math.atan2(
							(ids.get(stop).x - ids.get(start).x),
							(ids.get(stop).y - ids.get(start).y))/ Math.PI);
					angle += 360;
					angle %= 360;
					int segmentLength = (int)Math.round(UNIT_LENGTH * Math.sqrt(
							Math.pow(ids.get(start).x - ids.get(stop).x, 2) +
							Math.pow(ids.get(start).y - ids.get(stop).y, 2)));
					addSegment(fw, start, stop, angle, segmentLength);
					addSegmMap(f2, start, stop, angle, segmentLength, ids);
				}
				start = stop;
				stop = pps[i].getTo();
			}
		}
		fw.close();
		f2.close();
	}

	//r - checking
	//> x <- read.csv("/tmp/map_segments.txt", header=F, sep="\t")
	//> plot(x)
	
	final static double LANE_SHIFT = 0.05;
	private static void addSegmMap(PrintStream fw, int from, int to,
			int angle, int segmentLength, Map<Integer, Coords> ids) 
					throws IOException {
		int rowF = ids.get(from).y;
		int colF = ids.get(from).x;
		int rowT = ids.get(to).y;
		int colT = ids.get(to).x;
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

	private static void addSegment(PrintStream fw, int from, int to, int angle,
			int segmentLength) throws IOException {
		fw.append(String.valueOf(from)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(to)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(segmentLength)).append(FIELDS_SEPARATOR);
		fw.append(String.valueOf(angle)).append('\n');
	}	

}
