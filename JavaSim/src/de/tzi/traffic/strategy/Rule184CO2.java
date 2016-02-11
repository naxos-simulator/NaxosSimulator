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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.traffic.Crossing;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.PassingPossibilityProperty;
import de.tzi.traffic.properties.PropertyManager.SegmentProperty;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */

public class Rule184CO2 extends Strategy {

	private static Logger logger = Logger.getLogger(Rule184CO2.class);
	
	public Rule184CO2(TrafficManager trafficManager) {
		super(trafficManager);
	}
	
	public static char toCharacter(boolean b) {
		return b ? 'o' : '.';
	}
	
	protected char charRepresentation(PassingPossibility p, int pos) {
		Segment input = p.getInput();
		Segment output = p.getOutput();
		if (pos == 0) {
			int lastIdx = input.getSectionLength() - 1;
			return input == null ? '?' : toCharacter(input.isVehicleThere(lastIdx));
		} else if (pos == 1) {
			return output == null ? '?' : toCharacter(output.isVehicleThere(0));
		}
		return '?';
	}

	/**
	 * Usable for more advanced models like NaSch
	 * 
	 * @param time
	 * @param vehicleId
	 * @return
	 */
	protected boolean canVehicleMoveInThisStep(int time, int vehicleId) {
		return true;
	}
	
	int anihilated = 0;
	int removed = 0;
	int wayChanged = 0;
	public void tick(int time, Crossing crossing) {
		PassingPossibility[] group = crossing.getGroupWithGreenLight();
		Set<Integer> servedInputs = new TreeSet<Integer>();
		//Ask Vehicle if it is good direction for it
		for (PassingPossibility pp : group) {
			//Get a car which is at the last cell of input segment
			Segment input = pp.getInput();
			if (servedInputs.contains(input.getId()))
				continue;
			int lastIdx = input.getSectionLength() - 1;
			int vehicleId = input.getVehicleAtIndex(lastIdx);
			if (vehicleId == 0)
				continue;
			if (!canVehicleMoveInThisStep(time, vehicleId))
				continue;
			logger.debug(Arrays.toString(group));
			
			
			logger.debug("Processing vehicle: "+vehicleId+" at segment: "+input.getId()+" before crossing: "+crossing.getId());
			PassingPossibility nextMove = trafficManager.getNavigator().getVehiclePassingPossibility(vehicleId);
			logger.debug("Next move "+nextMove);
			
			boolean destinationReached = trafficManager.getNavigator().destinationReached(vehicleId, input);
			if (destinationReached) {
				logger.debug("Vehicle destination: "+trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId));
				if (GlobalConfiguration.getInstance().getBoolean(SettingsKeys.TRAFFIC_INFINITE_TRIP)) {
					trafficManager.getNavigator().flipSourceDestination(time, vehicleId);
					nextMove = trafficManager.getNavigator().findNextPassingPossibility(vehicleId, crossing, input, time);
					if (nextMove == null) {
						logger.info("No way back!");
					}
					
				} else {
					nextMove = null;
				}
				trafficManager.getPropertyManager().setVehicleProperty(
						VehicleProperty.DESTINATION_REACH_COUNTER,
						vehicleId, 1 + trafficManager.getPropertyManager().getVehicleProperty(
								VehicleProperty.DESTINATION_REACH_COUNTER,
								vehicleId));
								
			}
			
			trafficManager.getPropertyManager().setVehicleProperty(
					VehicleProperty.DESTINATION_REACHED_JUST_RIGHT_NOW,
					vehicleId, destinationReached ? 1 : 0);

			if (nextMove == null || !trafficManager.getNavigator().isPassingPossibilityValid(nextMove, crossing)) {
				nextMove = trafficManager.getNavigator().findNextPassingPossibility(vehicleId, crossing, input, time);
				logger.debug("Next move (once again) "+nextMove);
			}

			if (nextMove == null) {
				logger.warn("Removing vehicle: "+vehicleId+(destinationReached ? " OK " : " ")+(++removed));
				nextMove = trafficManager.getNavigator().findNextPassingPossibility(vehicleId, crossing, input, time);
				logger.debug("Next move (once again) "+nextMove);
				input.removeVehicleAtIndex(lastIdx);
				removeVehicleFromStatistics(vehicleId);
				servedInputs.add(input.getId());
			} else if (nextMove == pp) {
				//Two must be empty
				if (!pp.getOutput().isVehicleThere(0) && !pp.getOutput().isVehicleThere(1) &&
						!pp.getOutput().isVehicleThere(2)) {
					pp.getOutput().setVehicleAtIndex(0, vehicleId);
					input.removeVehicleAtIndex(lastIdx);
					int speed = getSpeed(vehicleId);
					if (speed == 15)
						setSpeed(vehicleId, 1);
					else if (speed < 14 && !pp.getOutput().isVehicleThere(1)) 
						setSpeed(vehicleId, speed + 1); 
					else if (speed <= 14 && pp.getOutput().isVehicleThere(1)) {
						int inc = (speed < 14) ? 1 : 0;
						setSpeed(vehicleId, Math.min(speed + inc, getSpeed(pp.getOutput().getVehicleAtIndex(1))));
					}
					moveAlong(vehicleId, pp, time);
					trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.DEADLOCK_COUNTER, vehicleId, 0);
					logger.debug("Moved successfully, speed: "+getSpeed(vehicleId));
					servedInputs.add(input.getId());
				} else {
					logger.debug("Car ahead, cannot move");
					int dc = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DEADLOCK_COUNTER, vehicleId);
					int dcTreshold = GlobalConfiguration.getInstance().getInt(SettingsKeys.TRAFFIC_DEADLOCK_WAIT);
					if (dcTreshold > 0 && dc > dcTreshold) {
						//Killing
//						logger.warn("Vehicle anihilated! " + (++anihilated));
//						input.removeVehicleAtIndex(lastIdx);
//						removeVehicleFromStatistics(vehicleId);
//						servedInputs.add(input.getId());						
						logger.warn("Changing the way! "+(++wayChanged));
						trafficManager.getNavigator().resetVehiclePassingPossibility(vehicleId, crossing);
					}
					trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.DEADLOCK_COUNTER, vehicleId, ++dc);
				}
			}
		}
	}
	
	public void tick(int time, Segment segment) {
		final int max = segment.getSectionLength() - 2;
		if (segment.getOutput() == null && segment.isVehicleThere(max + 1)) {
			removeVehicleFromStatistics(segment.getVehicleAtIndex(max + 1));
			segment.removeVehicleAtIndex(max + 1);
		}  else {
			segment.setVehicleAtIndex(max + 1, segment.getVehicleAtIndex(max + 1));
		}
		for (int i = max; i >= 0 ; i--) {
			if (segment.isVehicleThere(i)) {
				if (!canVehicleMoveInThisStep(time, segment.getVehicleAtIndex(i))) {
					segment.moveVehicle(i, i);
					continue;
				}
				if (segment.isVehicleThere(i + 1)) {
					segment.moveVehicle(i, i);
					setSpeed(segment.getVehicleAtIndex(i), 15);
//					setSpeed(false, segment.getVehicleAtIndex(i));
				} else {
					segment.moveVehicle(i, i + 1);
					segment.removeVehicleAtIndex(i);
					int maxSpeed = 14;
					if (i + 2 < segment.getSectionLength() && segment.isVehicleThere(i+2)) {
						maxSpeed = getSpeed(segment.getVehicleAtIndex(i+2));
					}
					int currSpeed = getSpeed(segment.getVehicleAtIndex(i));
					if (currSpeed == 15) {
						setSpeed(segment.getVehicleAtIndex(i), 1);
					} else {
						setSpeed(segment.getVehicleAtIndex(i), Math.min(maxSpeed, currSpeed + 1));
					}
				}
			} else {
				segment.removeVehicleAtIndex(i);
			}
		}
		boolean foundTransmitting = false;
		PropertyManager pm = trafficManager.getPropertyManager();
		final int len = segment.getSectionLength() - 1;
		for (int i = len - 1; !foundTransmitting && i > len / 2; i--) {
			int vehicleId = segment.getVehicleAtIndex(i);
			foundTransmitting = vehicleId > 0 && pm.getVehicleProperty(VehicleProperty.TRANSMITTING, vehicleId) > 0;
			if (!foundTransmitting)
				continue;
			if (vehicleId == 1) {
				//logger.info("HERE");
			}
			int sgmEnterId = pm.getVehicleProperty(VehicleProperty.SGM_ENTER_ID, vehicleId);
			int sgmEnterTime = pm.getVehicleProperty(VehicleProperty.SGM_ENTER_TIME, vehicleId);
			if (sgmEnterId != segment.getId() || sgmEnterTime <= 0)
				continue;
			int ppEnterId = pm.getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
			if (!(ppEnterId > 0 && sgmEnterId > 0 && sgmEnterTime > 0))
				continue;
			if (trafficManager.getPassingPossibilitiesById()[ppEnterId - 1].getOutput() != segment) {
				//logger.info("HERE");
			}
			//trafficManager.getPropertyManager().setVehiclePropertye(VehicleProperty.TRANSMITTING, 1, 1);
			pm.setSegmentProperty(SegmentProperty.FCD_ESTIMATION_UPDATE, segment.getId(), time);
			pm.setPassingPossibilityProperty(PassingPossibilityProperty.FCD_ESTIMATION_UPDATE, ppEnterId, time);
			int estimation = (int) Math.round((double) (time - sgmEnterTime) * ((double) segment.getSectionLength() / i));
			//logger.debug(estimation);
			pm.setSegmentProperty(SegmentProperty.FCD_ESTIMATION_TIME, sgmEnterId, estimation);
			pm.setPassingPossibilityProperty(PassingPossibilityProperty.FCD_ESTIMATION_TIME, ppEnterId, estimation);
		}
	}
	
	protected void moveAlong(int vehicleId, PassingPossibility pp, int time) {
		PropertyManager pm = trafficManager.getPropertyManager();
		int sgmEnterId = pm.getVehicleProperty(VehicleProperty.SGM_ENTER_ID, vehicleId);
		int sgmEnterTime = pm.getVehicleProperty(VehicleProperty.SGM_ENTER_TIME, vehicleId);
		int ppEnterId = pm.getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
		if (ppEnterId > 0 && sgmEnterId > 0 && sgmEnterTime > 0) {
			int travelTime = time - sgmEnterTime;
			pm.setSegmentProperty(SegmentProperty.REF_PASSING_TIME, sgmEnterId, travelTime);
			pm.setSegmentProperty(SegmentProperty.REF_PASSING_UPDATE, sgmEnterId, time);
			pm.setPassingPossibilityProperty(PassingPossibilityProperty.REF_PASSING_TIME, ppEnterId, travelTime);
			pm.setPassingPossibilityProperty(PassingPossibilityProperty.REF_PASSING_UPDATE, ppEnterId, time);
			double vlpr = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_LICENSE_PLATE_RATIO);
			boolean platesRecognized = trafficManager.getRandom().nextFloat() < vlpr; 
			if (platesRecognized && pm.getVehicleProperty(VehicleProperty.PLATES_PROPERLY_RECOGNIZED, vehicleId) > 0) {
				pm.setSegmentProperty(SegmentProperty.II_PASSING_TIME, sgmEnterId, travelTime);
				pm.setSegmentProperty(SegmentProperty.II_PASSING_UPDATE, sgmEnterId, time);
				pm.setPassingPossibilityProperty(PassingPossibilityProperty.II_PASSING_TIME, ppEnterId, travelTime);
				pm.setPassingPossibilityProperty(PassingPossibilityProperty.II_PASSING_UPDATE, ppEnterId, time);
			}
			pm.setVehicleProperty(VehicleProperty.PLATES_PROPERLY_RECOGNIZED, vehicleId, platesRecognized ? 1 : 0);
			if (pm.getVehicleProperty(VehicleProperty.TRANSMITTING, vehicleId) > 0) {
				pm.setSegmentProperty(SegmentProperty.FCD_ENDING_TIME, sgmEnterId, travelTime);
				pm.setSegmentProperty(SegmentProperty.FCD_ENDING_UPDATE, sgmEnterId, time);
				pm.setPassingPossibilityProperty(PassingPossibilityProperty.FCD_ENDING_TIME, ppEnterId, travelTime);
				pm.setPassingPossibilityProperty(PassingPossibilityProperty.FCD_ENDING_UPDATE, ppEnterId, time);
			}
		}
		pm.setVehicleProperty(VehicleProperty.SGM_ENTER_ID, vehicleId, pp.getOutput().getId());
		pm.setVehicleProperty(VehicleProperty.SGM_ENTER_TIME, vehicleId, time);
	}
	
	public void postProcessing(int time) {
		PropertyManager pm = trafficManager.getPropertyManager();
		for (int vehicleId = 1; vehicleId <= trafficManager.getVehiclesCount(); vehicleId++) {
			int speed = getSpeed(vehicleId);
			if (speed == -1)
				continue;
			int n = (int)(CO2Amounts[speed] / 500.0);
			//Current amount of CO2 emission
			int a = pm.getVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId);
			pm.setVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId, n + a);
			//pm.setVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId, n);
		}
	}
	
	final static int[] CO2Amounts = {0, 1011, 2089, 3627, 5541, 7575, 9403, 10792, 11687, 12160, 12332, 12313, 12181, 11986, 1378, 553};
	
	final static int[] CO2AmountsGuard = {0, 0, 1011, 2089, 3627, 5541, 7575, 9403, 10792, 11687, 12160, 12332, 12313, 12181, 11986, 1378, 553};
	
	int getCO2EmissionForState(int state) {
		return CO2Amounts[state];
	}
	
}
