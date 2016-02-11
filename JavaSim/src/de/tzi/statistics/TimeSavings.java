/**
 * 2012-07-02
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 *
 */
public class TimeSavings extends AbstractStatistics {
	
	public TimeSavings(TrafficManager trafficManager) {
		super(trafficManager);	
	}

	int callCounter;
	
	
	/**
	 * Counts differences in time of travel by shortest and quickest routes
	 */
	public boolean update(int time) {
		callCounter++;
		int max = trafficManager.getCrossingsCount();
		double travelTimeSmart = 0;
		double travelTimeNormal = 0;
		for (int u = 1; u <= max; u++) {
			for (int v = 1; v <= max; v++) {
				if (u == v)
					continue;
				double ts = trafficManager.getNavigator().travelTime(u, v, true);
				double tn = trafficManager.getNavigator().travelTime(u, v, false);
				
				if (ts == Double.POSITIVE_INFINITY || tn == Double.POSITIVE_INFINITY)
					continue;
				
				travelTimeSmart += ts;
				travelTimeNormal += tn;

			}
		}
		double ratio = (travelTimeNormal - travelTimeSmart) / travelTimeNormal;
		if (!Double.isNaN(ratio))
			cumulativeRatio += ratio;
		return true;
	}
	
	double cumulativeRatio;
	
	protected boolean singleValue() {
		return true;
	}

	public double getValue() {
		return callCounter == 0 ? 0 : cumulativeRatio / callCounter; 
	}
	
	public String getName() {
		return "Time savings";
	}
}
