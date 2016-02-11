/**
 * 2012-06-19
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class AccelerationEmission extends AbstractStatistics {
	
	public AccelerationEmission(TrafficManager trafficManager) {
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
			int acc = propertyManager.getVehicleProperty(VehicleProperty.ACCELERATION_FROM_ZERO, vehicleId);
			count += acc > 0 ? 1 : 0;
			vehicleCount += acc == PropertyManager.NO_DATA ? 0 : 1;
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
		return "Acceleration from zero Emission";
	}
}
