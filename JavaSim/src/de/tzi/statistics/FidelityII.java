/**
 * 2012-07-17
 */
package de.tzi.statistics;

import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.PassingPossibilityProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class FidelityII extends AbstractStatistics {
	
	public FidelityII(TrafficManager trafficManager) {
		super(1, trafficManager);	
	}

	int callCounter;
	
	/**
	 * Counts vehicles which speed is equal to zero
	 */
	public boolean update(int time) {
		boolean affected = false;
		PropertyManager propertyManager = trafficManager.getPropertyManager();
		PassingPossibility[] pps = trafficManager.getPassingPossibilitiesById();
		callCounter++;
		int appliedPP = 0;
		double localSum = 0;
		for (PassingPossibility pp : pps) {
			int measureTime_t = propertyManager.getPassingPossibilityProperty(
							PassingPossibilityProperty.II_PASSING_UPDATE, pp.getId());
			int measureTime_all = propertyManager.getPassingPossibilityProperty(
							PassingPossibilityProperty.REF_PASSING_UPDATE, pp.getId());
			if (measureTime_t > 0 && measureTime_all > 0) {
				int t_t = propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.II_PASSING_TIME, pp.getId());
				int t_all = propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.REF_PASSING_TIME, pp.getId());
				int diff = Math.abs(t_t - t_all);
				localSum += diff;// / (double)t_all; 
				appliedPP++;
			}
		}
		if (appliedPP > 0)
			affected |= addStat(0, localSum / appliedPP);
		return affected;
	}
	
	public double getValue() {
		return callCounter == 0 ? 0 : stats[0] / callCounter;
	}
	
	
	
	public String getName() {
		return "Fidelity of II";
	}
}
