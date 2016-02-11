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
 * 2012-06-18
 */
package de.tzi.traffic.strategy;

import de.tzi.traffic.Crossing;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public abstract class Strategy {

	protected TrafficManager trafficManager;
	
	public Strategy(TrafficManager trafficManager) {
		this.trafficManager = trafficManager;
	}
	
	public abstract void tick(int time, Segment segment);

	public abstract void tick(int time, Crossing crs);	
	
	protected int getSpeed(int vehicleId) {
		return trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED, vehicleId);
	}

	protected void setSpeed(int vehicleId, int speed) {
		if (speed > 15) {
			throw new RuntimeException("Bad speed: "+speed);
		}
		int oldSpeed = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED, vehicleId);
		if (oldSpeed < speed)
			trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.ACCELERATION_FROM_ZERO, vehicleId, 1);
		else
			trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.ACCELERATION_FROM_ZERO, vehicleId, 0);
		trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.SPEED, vehicleId, speed);
	}
	
	protected void removeVehicleFromStatistics(int vehicleId) {
		trafficManager.getPropertyManager().removeAllProperties(vehicleId);	
	}

	public int getTimeResolution() {
		return 1;
	}
	
	public void postProcessing(int time) {
		
	}
	
}
