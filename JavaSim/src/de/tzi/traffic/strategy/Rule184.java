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

public class Rule184 extends Strategy {

	private static Logger logger = Logger.getLogger(Rule184.class);
	
	public Rule184(TrafficManager trafficManager) {
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

	protected void setSpeed(boolean moved, int vehicleId) {
		setSpeed(vehicleId, moved ? 1 : 0);
	}
	/**
	 * Usable for more advanced models like NaSch
	 * 
	 * @param time
	 * @param vehicleId
	 * @return
	 */
	protected boolean canVehicleMoveInThisStep(int time, int vehicleId, Segment segment) {
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
			if (!canVehicleMoveInThisStep(time, vehicleId, input))
				continue;
			
			boolean zombie = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId) == PropertyManager.NO_DATA;
			if (zombie) {
				logger.error("Vehicle without destination");
			}
			logger.debug(Arrays.toString(group));
			
			PassingPossibility nextMove = getNextMove(vehicleId, input, time, crossing);

			if (nextMove == null) {
				boolean destinationReached = trafficManager.getNavigator().destinationReached(vehicleId, input);
				
				logger.warn("Removing vehicle: "+vehicleId+(destinationReached ? " OK " : " No way back! ")+(++removed));
				nextMove = trafficManager.getNavigator().findNextPassingPossibility(vehicleId, crossing, input, time);
				logger.debug("Next move (once again) "+nextMove);
				input.removeVehicleAtIndex(lastIdx);
				removeVehicleFromStatistics(vehicleId);
				servedInputs.add(input.getId());
			} else if (nextMove == pp) {
				//Two must be empty
				if (crossing.getGroups().length > 1) {
					//logger.info("AS");
				}
				if (!pp.getOutput().isVehicleThere(0) && !pp.getOutput().isVehicleThere(1) &&
						!pp.getOutput().isVehicleThere(2)) {
					pp.getOutput().setVehicleAtIndex(0, vehicleId);
					input.removeVehicleAtIndex(lastIdx);
//					int speed = getSpeed(vehicleId);
//					if (speed == 15)
//						setSpeed(vehicleId, 1);
//					else if (speed < 14 && !pp.getOutput().isVehicleThere(1)) 
//						setSpeed(vehicleId, speed + 1)	; 
//					else if (speed <= 14 && pp.getOutput().isVehicleThere(1)) {
//						int inc = (speed < 14) ? 1 : 0;
//						setSpeed(vehicleId, Math.min(speed + inc, getSpeed(pp.getOutput().getVehicleAtIndex(1))));
//					}
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
	
	protected PassingPossibility getNextMove(int vehicleId, Segment input, int time, Crossing crossing) {
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
				//End of this trip
				updateMovementStats(vehicleId);
				//Beginning of the another
				updateMovementStats(vehicleId);
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
			if (trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId) != PropertyManager.NO_DATA) {
				nextMove = trafficManager.getNavigator().findNextPassingPossibility(vehicleId, crossing, input, time);
				logger.debug("Next move (once again) "+nextMove);
			}
		}
		return nextMove;
	}

	public void tick(int time, Segment segment) {
		final int max = segment.getSectionLength() - 2;
		if (segment.getOutput() == null && segment.isVehicleThere(max + 1)) {
			removeVehicleFromStatistics(segment.getVehicleAtIndex(max + 1));
			segment.removeVehicleAtIndex(max + 1);
		}  else {
			segment.setVehicleAtIndex(max + 1, segment.getVehicleAtIndex(max + 1));
		}
		PropertyManager pm = trafficManager.getPropertyManager();
		for (int i = max; i >= 0 ; i--) {
			if (segment.isVehicleThere(i)) {
				if (!canVehicleMoveInThisStep(time, segment.getVehicleAtIndex(i), segment)) {
					segment.moveVehicle(i, i);
					continue;
				}
				if (segment.isVehicleThere(i + 1)) {
					segment.moveVehicle(i, i);
					setSpeed(false, segment.getVehicleAtIndex(i));
				} else {
					segment.moveVehicle(i, i + 1);
					segment.removeVehicleAtIndex(i);
					setSpeed(true, segment.getVehicleAtIndex(i));
					updateMovementStats(segment.getVehicleAtIndex(i));
				}
			} else {
				segment.removeVehicleAtIndex(i);
			}
		}
		boolean foundTransmitting = false;
		final int len = segment.getSectionLength() - 1;
		for (int i = len - 1; !foundTransmitting && i > len / 2; i--) {
			int vehicleId = segment.getVehicleAtIndex(i);
			foundTransmitting = vehicleId > 0 && pm.getVehicleProperty(VehicleProperty.TRANSMITTING, vehicleId) > 0;
			if (!foundTransmitting)
				continue;
			if (vehicleId == 1) {
//				logger.info("HERE");
			}
			int sgmEnterId = pm.getVehicleProperty(VehicleProperty.SGM_ENTER_ID, vehicleId);
			int sgmEnterTime = pm.getVehicleProperty(VehicleProperty.SGM_ENTER_TIME, vehicleId);
			if (sgmEnterId != segment.getId() || sgmEnterTime <= 0)
				continue;
			int ppEnterId = pm.getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
			if (!(ppEnterId > 0 && sgmEnterId > 0 && sgmEnterTime > 0))
				continue;
			if (trafficManager.getPassingPossibilitiesById()[ppEnterId - 1].getOutput() != segment) {
//				logger.info("HERE");
			}
			//trafficManager.getPropertyManager().setVehiclePropertye(VehicleProperty.TRANSMITTING, 1, 1);
			pm.setSegmentProperty(SegmentProperty.FCD_ESTIMATION_UPDATE, segment.getId(), time);
			pm.setPassingPossibilityProperty(PassingPossibilityProperty.FCD_ESTIMATION_UPDATE, ppEnterId, time);
			int estimation = (int) Math.round((double) (time - sgmEnterTime) * ((double) segment.getSectionLength() / i));
			//logger.debug(estimation);
			if (estimation == 0) {
//				logger.info("HERE");
			}
			pm.setSegmentProperty(SegmentProperty.FCD_ESTIMATION_TIME, sgmEnterId, estimation);
			pm.setPassingPossibilityProperty(PassingPossibilityProperty.FCD_ESTIMATION_TIME, ppEnterId, estimation);
		}
	}
	
	protected void updateMovementStats(int vehicleId) {
		int cellsDriven = trafficManager.getPropertyManager()
				.getVehicleProperty(VehicleProperty.CELLS_DRIVEN_IN_TRIP,
						vehicleId);
		trafficManager.getPropertyManager().setVehicleProperty(
				VehicleProperty.CELLS_DRIVEN_IN_TRIP, vehicleId,
				cellsDriven + 1);
		int totalCellsDriven = trafficManager.getPropertyManager()
				.getVehicleProperty(VehicleProperty.TOTAL_CELLS_DRIVEN,
						vehicleId);
		trafficManager.getPropertyManager().setVehicleProperty(
				VehicleProperty.TOTAL_CELLS_DRIVEN, vehicleId,
				totalCellsDriven + 1);
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
		updateMovementStats(vehicleId);
	}
}
