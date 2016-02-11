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
 * 2012-05-06
 */
package de.tzi.traffic;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Random;

import de.tzi.geometry.Section;
import de.tzi.traffic.navigation.Edge;
import de.tzi.traffic.navigation.Verticle;

/**
 * @author Michal Markiewicz
 *
 */
public class Segment implements Verticle, Edge {

	//private final static Logger logger = Logger.getLogger(Segment.class); 
	
	/**
	 * Segment id enumeration is guaranteed to be continuous (starts from 1) 
	 */
	int id;
	
	//now/future vehicleData (vehicle ID)
	protected final int[][] vehicleIds;
	
	protected final Section section;
	
	Crossing input;
	Crossing output;
	
	protected boolean dissapearAtTheEnd;
	
	public void setDissapearAtTheEnd(boolean dissapearAtTheEnd) {
		this.dissapearAtTheEnd = dissapearAtTheEnd;
	}
	
	final TrafficManager trafficManager;
	
	public Segment(TrafficManager trafficManager, Section section) {
		this.id = trafficManager.nextSegmentId();
		this.section = section;
		vehicleIds = new int[2][];
		vehicleIds[0] = new int[section.getLength()];
		vehicleIds[1] = new int[section.getLength()];
		now = 0;
		future = 1;
		this.trafficManager=trafficManager;
	}
	
	protected int now;
	protected int future;
	
	public void tick() {
		now = 1 - now;
		future = 1 - now;
	}
	
	public double getDensity() {
		int vehicleCount = 0;
		for (int i = 0; i < vehicleIds[now].length; i++) {
			vehicleCount += vehicleIds[now][i] > 0 ? 1 : 0;
		}
		return (double)vehicleCount/vehicleIds[now].length;
	}
	
	public boolean isVehicleThere(int pos) {
		return vehicleIds[now][pos] != 0;
	}

	public int getVehicleAtIndex(int pos) {
		return vehicleIds[now][pos];
	}

	public void destroyVehicleNowAtIndex(int pos) {
		vehicleIds[now][pos] = 0;
	}
	
	public void removeVehicleAtIndex(int pos) {
		vehicleIds[future][pos] = 0;
	}

	public void setVehicleAtIndex(int pos, int id) {
		vehicleIds[future][pos] = id;
	}
	
	public void moveVehicle(int oldPos, int newPos) {
		vehicleIds[future][newPos] = vehicleIds[now][oldPos];
	}
	
	protected char charRepresentation(int pos) {
		return isVehicleThere(pos) ? 'o' : '.';
	}
	
	public void printVehiclePositions(int time, Writer w) throws IOException {
		int length = getSectionLength();
		for (int i = 0; i < length; i++) {
			if (isVehicleThere(i)) {
				w.append(String.valueOf(time));
				w.append('\t');
				w.append(String.valueOf(section.getFrom()));
				w.append('\t');
				w.append(String.valueOf(section.getTo()));
				w.append('\t');
				w.append(String.valueOf(i / (float)length));
				w.append('\n');
			}
		}
	}
	
	public int getSectionLength() {
		return section.getLength();
	}
	
	void randomizeVehiclePositions(Random random, double treshold) {
		for (int i = 0; i < vehicleIds[now].length; i++) {
			if (random.nextFloat() < treshold) {
				//FIXED: ONLY ONE VEHICLE FOR TESTS
				//if (trafficManager.getVehiclesCount() <= 1) {
					vehicleIds[now][i] = trafficManager.nextVehicleId();
				//}
			} else {
				vehicleIds[now][i] = 0;
			}
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append(section.toString());
		sb.append(" [");
		sb.append((input == null ? 0 : input.getId()));
		sb.append('-');
		sb.append((output == null ? 0 : output.getId()));
		sb.append("] :\t");
		int length = getSectionLength();		
		for (int i = 0; i < length; i++) {
			//sb.append(charRepresentation(i));
			//sb.append(' ');
			sb.append(vehicleIds[now][i]);
			sb.append(' ');
		}
		return sb.toString();
	}
	
	
	public void debug() {
		StringBuffer sb = new StringBuffer();
		int length = getSectionLength();
		sb.append('\n');
		for (int i = 0; i < length; i++) {
			sb.append(vehicleIds[now][i]);
			sb.append(' ');
		}
		sb.append('\n');
		for (int i = 0; i < length; i++) {
			sb.append(vehicleIds[now][i]);
			sb.append(' ');
		}
		System.out.println(sb.toString());
	}
	
	public Section getSection() {
		return section;
	}
	
	public int getId() {
		return id;
	}
	
	public Crossing getInput() {
		return input;
	}
	
	public Crossing getOutput() {
		return output;
	}
	
	public int hashCode() {
		return id;
	}
	
	public boolean equals(Object obj) {
		return id == ((Segment)obj).id;
	};

	//PassingPossibility and delay which have to be applied to fit into green wave
	Map<PassingPossibility, byte[]> greenWaveDelayForPassingPossibility;
	
	/**
	 * Returns a collection containing PassingPossibilities in which this segment is an input segment
	 * The first element of the array represents the cell which is the closest to the output
	 * @return
	 */
	public byte[] getDelayForPassingPossibility(PassingPossibility p) {
		return greenWaveDelayForPassingPossibility.get(p);
	}
}
