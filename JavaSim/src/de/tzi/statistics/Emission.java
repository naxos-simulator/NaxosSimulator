/**
 * 2014-03-14
 */
package de.tzi.statistics;

import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class Emission extends AbstractStatistics {
	
	public Emission(TrafficManager trafficManager) {
		super(trafficManager);
	}

	int callCounter;
	double averageSpeed = 0;
	
	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		for (Segment sgm : trafficManager.getSegments()) {
			
			for (int pos = 0; pos < sgm.getSectionLength(); pos++) {
				int vehicleId = sgm.getVehicleAtIndex(pos);
				if (vehicleId > 0) {
					int v = propertyManager.getVehicleProperty(VehicleProperty.SPEED, vehicleId);	
					int a = propertyManager.getVehicleProperty(VehicleProperty.ACCELERATION_FROM_ZERO, vehicleId);
					System.out.println(sgm.getId()+"\t"+pos+"\t"+ time+"\t"+v+"\t"+a);
				}
					
			}
		}			
		return true;
	}
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 ? 0 : averageSpeed / callCounter; 
	}
	
	public String getName() {
		return "CO_2 emission of each vehicle";
	}
}
