/**
 * 2012-07-02
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 *
 */
public class DistanceOverheads extends AbstractStatistics {
	
	public DistanceOverheads(TrafficManager trafficManager) {
		super(trafficManager);	
	}

	int callCounter;
	
	
	/**
	 * Counts differences in travel distance by shortest and quickest routes
	 */
	public boolean update(int time) {
		callCounter++;
		int max = trafficManager.getCrossingsCount();
		double travelDistanceSmart = 0;
		double travelDistanceNormal = 0;
		for (int u = 1; u <= max; u++) {
			for (int v = 1; v <= max; v++) {
				if (u == v)
					continue;
				double ds = trafficManager.getNavigator().travelDistance(u, v, true);//smart
				double dn = trafficManager.getNavigator().travelDistance(u, v, false);//normal
				
				if (ds == Double.POSITIVE_INFINITY || dn == Double.POSITIVE_INFINITY)
					continue;
				
				travelDistanceSmart += ds;
				travelDistanceNormal += dn;
			}
		}
		double ratio = (travelDistanceSmart - travelDistanceNormal) / travelDistanceNormal;
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
		return "Distance overheads";
	}
}
