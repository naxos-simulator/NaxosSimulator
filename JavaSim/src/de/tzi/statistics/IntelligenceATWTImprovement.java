/**
 * 2012-06-27
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class IntelligenceATWTImprovement extends AbstractStatistics {
	
	public IntelligenceATWTImprovement(TrafficManager trafficManager) {
		super(trafficManager);	
	}

	int callCounter;
	
	double sum;
	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		boolean atLeastOne = false;
		callCounter++;
		
		int countStopped = 0;
		int countStoppedIntelligent = 0;

		int countAllVehicles = 0;
		int countIntelligentVehicles = 0;
		
		
		for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
			int intelligence = propertyManager.getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (intelligence == PropertyManager.NO_DATA)
				continue;
			atLeastOne = true;
			countAllVehicles++;
			if (intelligence > 0)
				countIntelligentVehicles++;
			int speed = propertyManager.getVehicleProperty(VehicleProperty.SPEED, vehicleId);
			if (speed == 0) {
				countStopped++;
				if (intelligence > 0) {
					countStoppedIntelligent++;
				}
			}
		}
		double atwt_int = (double)countStoppedIntelligent/countIntelligentVehicles;
		double atwt_all = (double)countStopped/countAllVehicles;
		
		atwt_int_total+= atwt_int;
		atwt_all_total+= atwt_all;
		return atLeastOne;
	}
	
	double atwt_int_total;
	double atwt_all_total;
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 ? 0 : atwt_int_total / atwt_all_total; 
	}
	
	public String getName() {
		return "Intelligence ATWT Improvement";
	}
}
