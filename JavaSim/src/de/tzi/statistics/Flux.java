/**
 * 2012-07-02
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
public class Flux extends AbstractStatistics {
	
	final int allCellsCount;
	
	public Flux(TrafficManager trafficManager) {
		super(trafficManager);
		int tmp = 0;
		for (Segment sgm : trafficManager.getSegments()) {
			tmp += sgm.getSectionLength();
		}
		allCellsCount = tmp;
	}

	int callCounter;
	
	
	/**
	 * Counts differences in time of travel by shortest and quickest routes
	 */
	public boolean update(int time) {
		callCounter++;
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		int max = trafficManager.getVehiclesCount();
		int count = 0;
		for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
			int speed = propertyManager.getVehicleProperty(VehicleProperty.SPEED, vehicleId);
			count += speed > 0 ? 1 : 0;
		}
		double ratio = (double)count / allCellsCount;
		if (!Double.isNaN(ratio))
			cumulativeRatio += ratio;
		return count > 0;
	}
	
	double cumulativeRatio;
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 ? 0 : cumulativeRatio / callCounter; 
	}
	
	public String getName() {
		return "Flux";
	}
}
