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
public class TargetsCount extends AbstractStatistics {
	
	public TargetsCount(TrafficManager trafficManager) {
		super(trafficManager);	
	}

	int callCounter;
	
	int targetsSmart;
	int targetsAll;
	
	double targetsSmartWeighted;
	
	/**
	 * Counts differences in time of travel by shortest and quickest routes
	 */
	public boolean update(int time) {
		callCounter++;
		int allVehicles = 0;;
		int smartVehicles = 0;
		int targetsLocalAll = 0;
		int targetsLocalSmart = 0;
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
			int targetNow = propertyManager.getVehicleProperty(
					VehicleProperty.DESTINATION_REACHED_LAST_TIME, vehicleId) > time
					- trafficManager.getTimeResolution() ? 1 : 0;
			targetsAll += targetNow;
			targetsLocalAll += targetNow;
			int intelligence = propertyManager.getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (intelligence == PropertyManager.NO_DATA)
				continue;
			allVehicles++;
			if (intelligence == 0) 
				continue;
			smartVehicles++;
			targetsSmart += targetNow;
			targetsLocalSmart += targetNow;
		}
		if (targetsLocalAll > 0) {
			targetsSmartWeighted += ((double) targetsLocalSmart *  allVehicles) / ((double) targetsLocalAll * smartVehicles);
		}
		return true;
	}
	
	double cumulativeRatio;
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 && targetsAll > 0 ? 0 : // targetsSmart / (double)targetsAll;
				targetsSmartWeighted / callCounter;
	}
	
	public String getName() {
		return "Time savings";
	}
}
