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
 * 2012-06-19
 */
package de.tzi.traffic.properties;

import java.io.PrintStream;
import java.util.Collection;

import org.apache.log4j.Logger;

import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 *
 */
public class PropertyManager {

	private static Logger logger = Logger.getLogger(PropertyManager.class);
	
	private final TrafficManager trafficManager;
	
	public final static int NO_DATA = -1;
	
	public enum VehicleProperty {
		ACCELERATION_FROM_ZERO,
		SPEED,
		SPEED_INDEX_NASCH,
		SPEED_INDEX_NASCH_TO_GO,
		SEED,
		SOURCE,
		DESTINATION,
		SMART_R,
		SMART_S,
		TRANSMITTING,
		PLATES_PROPERLY_RECOGNIZED,
		//When a vehicle entered passing possibility
		SGM_ENTER_TIME,
		SGM_ENTER_ID,
		PP_ENTER_ID,
		PP_PREV_ID,
		DEADLOCK_COUNTER,
		DESTINATION_REACHED_LAST_TIME,
		CO2_EMITTED,
		DESTINATION_REACHED_JUST_RIGHT_NOW,
		DESTINATION_REACH_COUNTER,
		CELLS_DRIVEN_IN_TRIP,
		TOTAL_CELLS_DRIVEN,
	}
	
	public enum RoadProperty {
		GREEN_WAVE,	
	}
	
	public enum SegmentProperty {
		//Passing time of all vehicles
		REF_PASSING_TIME,
		REF_PASSING_UPDATE,
		//Passing time of vehicles with properly recognized license plates 
		II_PASSING_TIME,
		II_PASSING_UPDATE,
		//Floating car data estimations
		FCD_ESTIMATION_TIME,
		FCD_ESTIMATION_UPDATE,
		//Floating car data at crossings
		FCD_ENDING_TIME,
		FCD_ENDING_UPDATE,
		//Floating car data estimations (all vehicles)
		FCD_REF_ESTIMATION_TIME,
		FCD_REF_ESTIMATION_UPDATE,
		//Historical time from the past
		HISTORICAL_TIME,
		DISTANCE_TO_THE_NEAREST_CAR_IN_GREEN,		
	}
	
	public enum CrossingProperty {
		CURRENT_CYCLE_LENGTH,
		RANDOM_CYCLE_LENGTH
	}

	public enum PassingPossibilityProperty {
		REF_PASSING_TIME,
		REF_PASSING_UPDATE,
		II_PASSING_TIME,
		II_PASSING_UPDATE,
		FCD_ESTIMATION_TIME,
		FCD_ESTIMATION_UPDATE,
		FCD_REF_ESTIMATION_TIME,
		FCD_REF_ESTIMATION_UPDATE,
		FCD_ENDING_TIME,
		FCD_ENDING_UPDATE,
		HISTORICAL_TIME,
		SOTL_D,
		SOTL_R,
		SOTL_E,
	}
	           
	public PropertyManager(TrafficManager trafficManager) {
		this.trafficManager = trafficManager;
		//Creates properties for all segments
		Collection<Segment> segments = trafficManager.getSegments();
		for (RoadProperty r : RoadProperty.values()) {
			int prop = r.ordinal();
			for (Segment segment : segments) {
				roadProperties[prop] = new int[segment.getId() - 1][segment.getSectionLength()];
			}
		}
		//Creates properties for all vehicles
		int vehicleIdsCount = trafficManager.getVehiclesCount();
		vehicleProperties = new int[VehicleProperty.values().length][vehicleIdsCount];
		//Creates properties for crossings vehicles
		int crossingsIdsCount = trafficManager.getCrossingsCount();
		crossingProperties = new int[CrossingProperty.values().length][crossingsIdsCount];
		int passingPossibitiesCount =trafficManager.getPassingPossibitiesCount();
		passingPossibilityProperties = new int[PassingPossibilityProperty.values().length][passingPossibitiesCount];
		int segmentPropertiesCount = trafficManager.getSegmentsCount();
		segmentProperties = new int[SegmentProperty.values().length][segmentPropertiesCount]; 
	}
	
	//Property, vehicle id
	private final int[][] vehicleProperties;

	//Property, vehicle id
	private final int[][] crossingProperties;

	//Property, road id, cell id
	private final int[][][] roadProperties = new int[RoadProperty.values().length][][];

	//Property, passing possibility id
	private final int[][] passingPossibilityProperties;

	//Property, segment id
	private final int[][] segmentProperties;
	
	public int getPassingPossibilityProperty(PassingPossibilityProperty ppp, int id) {
		return passingPossibilityProperties[ppp.ordinal()][id - 1];
	}
	
	public void setPassingPossibilityProperty(PassingPossibilityProperty ppp, int id, int val) {
		passingPossibilityProperties[ppp.ordinal()][id - 1] = val;
	}
	
	public int getRoadProperty(RoadProperty rp, int id, int pos) {
		return roadProperties[rp.ordinal()][id - 1][pos];
	}
	
	public void setRoadProperty(RoadProperty rp, int id, int pos, int val) {
		roadProperties[rp.ordinal()][id - 1][pos] = val;
	}

	public int getVehicleProperty(VehicleProperty vp, int vehicleId) {
		return vehicleProperties[vp.ordinal()][vehicleId - 1];
	}
	
	public void setVehicleProperty(VehicleProperty vp, int vehicleId, int value) {
		vehicleProperties[vp.ordinal()][vehicleId - 1] = value;
	}
	
	public int getCrossingProperty(CrossingProperty cp, int crossingId) {
		return crossingProperties[cp.ordinal()][crossingId - 1];
	}
	
	public void setCrossingProperty(CrossingProperty cp, int crossingId, int value) {
		crossingProperties[cp.ordinal()][crossingId - 1] = value;
	}
	
	public int getSegmentProperty(SegmentProperty sp, int segmentId) {
		return segmentProperties[sp.ordinal()][segmentId - 1];
	}
	
	public void setSegmentProperty(SegmentProperty sp, int segmentId, int value) {
		segmentProperties[sp.ordinal()][segmentId - 1] = value;
	}
	
	public String vehiclePropertiesToString(int vehicleId) {
		StringBuffer sb = new StringBuffer();
		for (VehicleProperty v : VehicleProperty.values()) {
			sb.append(v.toString()).append('\t');
			sb.append(String.valueOf(vehicleProperties[v.ordinal()][vehicleId - 1]));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void print(PrintStream ps) {
		for (VehicleProperty v : VehicleProperty.values()) {
			ps.append(v.toString()).append('\t');
			int prop = v.ordinal();
			for (int i = 0; i < vehicleProperties[prop].length; i++) {
				ps.append(' ');
				ps.append(String.valueOf(vehicleProperties[prop][i]));
			}
			ps.append("\n");
		}
		
		for (PassingPossibilityProperty v : PassingPossibilityProperty.values()) {
			ps.append(v.toString()).append('\t');
			int prop = v.ordinal();
			for (int i = 0; i < passingPossibilityProperties[prop].length; i++) {
				ps.append(' ');
				ps.append(String.valueOf(passingPossibilityProperties[prop][i]));
			}
			ps.append("\n");
		}
		
		for (SegmentProperty v : SegmentProperty.values()) {
			ps.append(v.toString()).append('\t');
			int prop = v.ordinal();
			for (int i = 0; i < segmentProperties[prop].length; i++) {
				ps.append(' ');
				ps.append(String.valueOf(segmentProperties[prop][i]));
			}
			ps.append("\n");
		}
	}

	public void removeAllProperties(int vehicleId) {
		logger.info("Removing vehicle: "+vehicleId);
		for (VehicleProperty v : VehicleProperty.values()) {
			vehicleProperties[v.ordinal()][vehicleId - 1] = NO_DATA;
		}
		trafficManager.setRemovedVehiclesCount(trafficManager.getRemovedVehiclesCount() + 1);
	}
	
	public void importHistoricalTravelTimes(PropertyManager otherPM) {
		importSegmentPassingTimes(otherPM, 1);
		importPassingPossibilitiesTimes(otherPM, 1);
	}
	
	public void importHistoricalTravelTimes(Collection<PropertyManager> otherPMs) {
		double weight = 1.0 / otherPMs.size();
		for (PropertyManager pm : otherPMs) {
			importSegmentPassingTimes(pm, weight);
			importPassingPossibilitiesTimes(pm, weight);
		}
	}

	private void importPassingPossibilitiesTimes(PropertyManager otherPM, double weight) {
		int pFrom = PassingPossibilityProperty.II_PASSING_TIME.ordinal();
		int pTo = PassingPossibilityProperty.HISTORICAL_TIME.ordinal();
		if (passingPossibilityProperties[pTo].length != otherPM.passingPossibilityProperties[pFrom].length) {
			String msg = "Cannot import passing possibilities passing times: number of passing possibilities is different!";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		for (int i = 0; i < otherPM.passingPossibilityProperties[pFrom].length; i++) {
			passingPossibilityProperties[pTo][i] += (int)Math.round(otherPM.passingPossibilityProperties[pFrom][i] * weight);
		}
	}

	private void importSegmentPassingTimes(PropertyManager otherPM, double weight) {
		int pFrom = SegmentProperty.II_PASSING_TIME.ordinal();
		int pTo = SegmentProperty.HISTORICAL_TIME.ordinal();
		if (segmentProperties[pTo].length != otherPM.segmentProperties[pFrom].length) {
			String msg = "Cannot import segment passing times: number of segments is different!";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		for (int i = 0; i < otherPM.segmentProperties[pFrom].length; i++) {
			segmentProperties[pTo][i] += (int)Math.round(otherPM.segmentProperties[pFrom][i] * weight);
		}
	}
	
}
