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
public class VehiclesCount extends AbstractStatistics {
	
	public VehiclesCount(TrafficManager trafficManager) {
		super(1, trafficManager);	
	}

	int callCounter;
	
	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		int count = 0;
		for (int vehicleId = 1; vehicleId < max; vehicleId++) {
			count += propertyManager.getVehicleProperty(VehicleProperty.DESTINATION, vehicleId) > 0 ? 1 : 0;
		}
		boolean affected = count != stats[0];
		stats[0] = count;
		return affected;
	}
	
	protected boolean singleValue() {
		return true;
	}
	
	public double getValue() {
		return stats[0];
	}
	
	public String getName() {
		return "Vehicles Count";
	}
}
