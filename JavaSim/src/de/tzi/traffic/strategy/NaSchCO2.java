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
package de.tzi.traffic.strategy;

import org.apache.log4j.Logger;

import de.tzi.graphics.Display;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.SegmentProperty;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class NaSchCO2 extends Rule184 {
	
	private static Logger logger = Logger.getLogger(NaSchCO2.class);
	
	public NaSchCO2(TrafficManager trafficManager) {
		super(trafficManager);
	}
	
	final static byte MIN_SPEED = 1;
	final static byte MAX_SPEED = 12;

	
	public int getTimeResolution() {
		return advances[MAX_SPEED];
	}
	
	
//	static int[] cellNaSch = { 0, 1, 2, 3, 4, 5 };
	
	static int[] cellR184 = { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	static int[] cell07 = { 0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 19, 21 };
	static int[] cell29 = { 0, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5 };
	
	
	static int[] advances = cell07;
//	static int[] advances = cell29;
	
	public static int getMaxSpeedInCells() {
		return advances[advances.length - 1]; 
	}

	
	static int[] advancesReversed;
	
	static {
		advancesReversed = new int[advances[MAX_SPEED] + 1];
		for (int i = 0; i < advancesReversed.length; i++) {
			int m = 0;
			for (int j = 0; j < MAX_SPEED; j++) {
				if (advances[j] <= i)
					m = j;
			}
			advancesReversed[i] = m;
		}
	}
	
	protected boolean canVehicleMoveInThisStep(int time, int vehicleId, Segment segment) {
		int toGo = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH_TO_GO, vehicleId);
		if (toGo > 0) {
			trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH_TO_GO, vehicleId, toGo - 1);
			return true;
		}
		return segment.getVehicleAtIndex(segment.getSectionLength() - 1) == vehicleId;
	}
	
	//In zero step 
	public void tick(int time, Segment segment) {
		//CELLS_TO_GO
		final int max = segment.getSectionLength() - 1;
		if (segment.getOutput() == null && segment.isVehicleThere(max)) {
			removeVehicleFromStatistics(segment.getVehicleAtIndex(max));
			segment.removeVehicleAtIndex(max);
		}  else {
			segment.setVehicleAtIndex(max, segment.getVehicleAtIndex(max));
		}
		//FIXME: The case when there is a road after crossing
		if (time % getTimeResolution() == 0) {
			//For all green lights check what is the distance to the next car after green lights
			for (PassingPossibility p : segment.getOutput().getAllPossibilities()) {
				if (p.getInput() != segment) {
					continue;
				}
				trafficManager.getPropertyManager().setSegmentProperty(SegmentProperty.DISTANCE_TO_THE_NEAREST_CAR_IN_GREEN,
						p.getOutput().getId(), 0);
			}
			for (PassingPossibility p : segment.getOutput().getGroupWithGreenLight()) {
				if (p.getId() == 20) {
					logger.getAdditivity();
				}
				if (p.getInput() != segment) {
					continue;
				}
				int min = Math.min(p.getOutput().getSectionLength(), advances[MAX_SPEED]);
				Segment outputSegment = p.getOutput();
				int distanceToTheNextInGreen = 0;
				for (int i = 0; i < min && !outputSegment.isVehicleThere(i); i++) {
					distanceToTheNextInGreen++;
				}
				trafficManager.getPropertyManager().setSegmentProperty(SegmentProperty.DISTANCE_TO_THE_NEAREST_CAR_IN_GREEN, 
						outputSegment.getId(), distanceToTheNextInGreen);
			}
			
			//Do calculations only here
			int lastVehicleIdx = max + 1;
			for (int i = max; i >= 0 ; i--) {
				if (segment.isVehicleThere(i)) {
					int vehicleId = segment.getVehicleAtIndex(i);
					int currentSpeed = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH, vehicleId);
					if (currentSpeed == PropertyManager.NO_DATA) {
						logger.error("Vehicle with wrong speed");
					}
					int newSpeed = Math.min(MAX_SPEED, currentSpeed + 1);
					
					int distanceInTheNextSegment = 0;
					int lookup = advances[newSpeed];
					if (lookup + i > max) { 
						PassingPossibility nextMove = getNextMove(vehicleId, segment, time, segment.getOutput());
						if (nextMove == null) {
							boolean destinationReached = trafficManager.getNavigator().destinationReached(vehicleId, segment);
							if (true) {
								PassingPossibility[] all = segment.getOutput().getAllPossibilities();
								nextMove = all[trafficManager.getRandom().nextInt(all.length)];
							}
							if (nextMove == null) {
								logger.warn("Removing vehicle: "+vehicleId+(destinationReached ? " OK " : " ")+(++removed));
								nextMove = trafficManager.getNavigator().findNextPassingPossibility(vehicleId, segment.getOutput(), segment, time);
								logger.debug("Next move (once again) "+nextMove);
								segment.removeVehicleAtIndex(i);
								removeVehicleFromStatistics(vehicleId);
								continue;
							}
						}
						distanceInTheNextSegment = trafficManager.getPropertyManager().getSegmentProperty(SegmentProperty.DISTANCE_TO_THE_NEAREST_CAR_IN_GREEN,
										nextMove.getOutput().getId());
					}
					int distanceInThisSegment = lastVehicleIdx - i - 1;
					int distanceToTheNextCar = distanceInThisSegment;
					//if there is no vehicles between current vehicle position and the end of the segment, then add distanceInTheNextSegment  
					if (lastVehicleIdx == max + 1) {
						distanceToTheNextCar += distanceInTheNextSegment;
					}
					
					if (distanceToTheNextCar <= advances[newSpeed]) {
						newSpeed = advancesReversed[distanceToTheNextCar];
						/*
						if (lastVehicleIdx <= max) {
							int otherVehicleId = segment.getVehicleAtIndex(lastVehicleIdx);
							if (otherVehicleId > 0) {
								int otherCarSpeed = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH, otherVehicleId);
								if (otherCarSpeed != PropertyManager.NO_DATA) {
									newSpeed = Math.min(newSpeed, Math.max(otherCarSpeed - 1, 0));
								}
							}
						} else {
							newSpeed = advancesReversed[distanceToTheNextCar];
						}
						*/
					} else {
						//newSpeed = 0; 
					}
					if (distanceToTheNextCar == 0) {
//						newSpeed = 0;
					}

					trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH, vehicleId, newSpeed);
					trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH_TO_GO, vehicleId, advances[newSpeed]);
					if (currentSpeed == -1 || newSpeed == -1) {
						logger.error("Wrong speed!");
					}
					computeCO2ForVehicle(vehicleId, currentSpeed, newSpeed);
					lastVehicleIdx = i;
				} else {
					segment.removeVehicleAtIndex(i);
				}
			}
		}
		super.tick(time, segment);
	}
	
	//CO2
	static int[] singleAcc = { 1011, 2089, 3627, 5541, 7575, 9403, 10792, 11687, 12160, 12332, 12313, 12181, 11986 };
	static int[] zeroAcc =   {  553,  952, 1410, 1881, 2303, 2614,  2775,  2778,  2637,  2376,  2016,  1574, 1060 };
	
	private int getCO2(int vp, int vn) {
		if (vn - vp == 1) {
			return singleAcc[vp];
		} else if (vp == vn) {
			return zeroAcc[vp];
		}
		return 0;
	}
	long totalCO2;
	private void computeCO2ForVehicle(int vehicleId, int currentSpeed, int newSpeed) {
		int n = (int)(getCO2(currentSpeed, newSpeed));
		//Current amount of CO2 emission
		PropertyManager pm = trafficManager.getPropertyManager();
		int a = pm.getVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId);
		if (Display.resetCO2)
			a = -n;
		pm.setVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId, n + a);
		totalCO2 += n;
	}

}
