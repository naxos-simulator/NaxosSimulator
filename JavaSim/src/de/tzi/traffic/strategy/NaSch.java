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

import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class NaSch extends Rule184CO2 {
	
	private static Logger logger = Logger.getLogger(NaSch.class);
	
	public NaSch(TrafficManager trafficManager) {
		super(trafficManager);
	}
	
	final static byte MIN_SPEED = 1;
	final static byte MAX_SPEED = 5;

	protected boolean canVehicleMoveInThisStep(int time, int vehicleId) {
		return time % MAX_SPEED < trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED, vehicleId);
	}
	
	protected void setSpeed(boolean moved, int vehicleId) {
		//Disallowed!
	}
	
	public void tick(int time, Segment segment) {
//		int badSgm = 0;
		if (time % MAX_SPEED == 0) {
			int length = segment.getSectionLength();
			for (int i = 0; i < length; i++) {
				int vehicleId = segment.getVehicleAtIndex(i);
				if (vehicleId == 0)
					continue;
				int distanceToTheNext = 0;
				//Lookup within segment
				int lookup = Math.min(i + MAX_SPEED + 1, length);
				for (int j = i + 1; j < lookup; j++) {
					if (segment.isVehicleThere(j)) {
						break;
					}
					distanceToTheNext++;
				}
				if (i >= length - MAX_SPEED && segment.getOutput() != null) {
					PassingPossibility[] groupWithGreen = segment.getOutput().getGroupWithGreenLight();
					PassingPossibility whereToGoNext = trafficManager.getNavigator().lookupForNextPassingPossibility(vehicleId, segment.getOutput(), segment);
					if (whereToGoNext == null) {
						//Allow the vehicle to reach its destination
						distanceToTheNext++;
					} else {
						boolean foundInGreenLightGroup = false;
						for (int j = 0; !foundInGreenLightGroup && j < groupWithGreen.length; j++) {
							foundInGreenLightGroup = groupWithGreen[j] == whereToGoNext;
						}
						if (foundInGreenLightGroup) {
							//We know that it could pass the lights in this turn
							Segment outputSegment = whereToGoNext.getOutput();
							int max = Math.min(outputSegment.getSectionLength(), MAX_SPEED - distanceToTheNext);
							for (int j = 0; j < max; j++) {
								if (outputSegment.isVehicleThere(j)) {
									break;
								}
								distanceToTheNext++;
							}
						}
					}
				}
				//Set the speed
				int currentNaSchSpeed = getSpeed(vehicleId);
				int newSpeed = Math.min(MAX_SPEED, Math.min(currentNaSchSpeed + 1, distanceToTheNext));
				int passingPossibilityId = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
				if (newSpeed > 1 && passingPossibilityId > 0 && trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_S, vehicleId) > 0) {
					//Slow down accordingly to traffic lights suggestions
					PassingPossibility nextPP = trafficManager.getNavigator().lookupForNextPassingPossibility(vehicleId, segment.getOutput(), segment); 
					if (nextPP != null && nextPP.getInput() == segment) {
						byte[] delayTunnel = segment.getDelayForPassingPossibility(nextPP);
						int delay = delayTunnel[delayTunnel.length - i - 1];
						if (delay > 0) {
							if (newSpeed != Math.max(1, newSpeed - delay)) {
								logger.info("Altering speed from : "+newSpeed +" to: "+ Math.max(1, newSpeed - delay));
							}
							newSpeed = Math.max(1, newSpeed - delay);
						}
					}
				}
//				logger.debug("Vehicle: " + vehicleId + " distance to the next: " + distanceToTheNext + 
//						" (" + distanceToTheNextB + " / " +distanceToTheNextA + ") new speed: " + newSpeed);
				setSpeed(vehicleId, newSpeed);
			}
		}
		super.tick(time, segment);
	}
	
	public int getTimeResolution() {
		return MAX_SPEED;
	}
}
