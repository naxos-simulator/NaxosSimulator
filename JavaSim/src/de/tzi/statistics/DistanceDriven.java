/**
 * 2012-06-18
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class DistanceDriven extends AbstractStatistics {
	
	public DistanceDriven(TrafficManager trafficManager) {
		super(trafficManager.getVehiclesCount(), trafficManager);	
	}

	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		boolean affected = false;
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		for (int vehicleId = 1; vehicleId < max; vehicleId++) {
			affected |= addStat(vehicleId - 1, propertyManager.getVehicleProperty(VehicleProperty.SPEED, vehicleId) == 1 ? 1 : 0);
		}
		return affected;
	}
	
	public String getName() {
		return "Distance Driven";
	}
}
