/**
 * 2012-09-26
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class TargetsCountAbs extends AbstractStatistics {
	
	public TargetsCountAbs(TrafficManager trafficManager) {
		super(trafficManager);	
	}

	int callCounter;
	
	int targetsAll;
	
	/**
	 * Counts differences in time of travel by shortest and quickest routes
	 */
	public boolean update(int time) {
		callCounter++;
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
			int targetNow = propertyManager.getVehicleProperty(
					VehicleProperty.DESTINATION_REACHED_LAST_TIME, vehicleId) > time
					- trafficManager.getTimeResolution() ? 1 : 0;
			targetsAll += targetNow;
		}
		return true;
	}
	
	double cumulativeRatio;
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 && targetsAll > 0 ? 0 : // targetsSmart / (double)targetsAll;
				(double)targetsAll / callCounter;
	}
	
	public String getName() {
		return "Time savings";
	}
}
