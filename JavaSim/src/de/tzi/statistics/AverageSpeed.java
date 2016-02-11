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
public class AverageSpeed extends AbstractStatistics {
	
	public AverageSpeed(TrafficManager trafficManager) {
		super(trafficManager);	
	}

	int callCounter;
	double averageSpeed = 0;
	
	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		boolean atLeastOne = false;
		callCounter++;
		int sum = 0;
		int vehicleCount = 0;
		for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
			int speed = propertyManager.getVehicleProperty(VehicleProperty.SPEED, vehicleId);
			if (speed == PropertyManager.NO_DATA)
				continue;
			vehicleCount++;
			atLeastOne = true;
			sum += speed;
		}
		averageSpeed += (double)sum / vehicleCount;
		return atLeastOne;
	}
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 ? 0 : averageSpeed / callCounter; 
	}
	
	public String getName() {
		return "Average speed of all vehicles";
	}
}
