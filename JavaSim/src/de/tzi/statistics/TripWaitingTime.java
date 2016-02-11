/**
 * 2012-06-29
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class TripWaitingTime extends AbstractStatistics {
	
	public TripWaitingTime(TrafficManager trafficManager) {
		super(trafficManager);		
	}

	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		callCounter++;
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		int count = 0;
		int vehicleCount = 0;
		for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
			int speed = propertyManager.getVehicleProperty(VehicleProperty.SPEED, vehicleId);
			count += speed == 0 ? 1 : 0;
			vehicleCount += speed == PropertyManager.NO_DATA ? 0 : 1;
		}
		double ratio = (double)count / vehicleCount;
		if (!Double.isNaN(ratio))
			cumulativeRatio += ratio;
		return count > 0;
	}
	
	int callCounter;
	
	double cumulativeRatio;
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 ? 0 : cumulativeRatio / callCounter; 
	}
	
	public String getName() {
		return "Trip Waiting Time";
	}
}
