/**
 * 2012-06-18
 */
package de.tzi.statistics;

import de.tzi.traffic.TrafficManager;

/**
 * 
 * Holds abstract statistics per object with a given id
 * 
 * @author Michal Markiewicz
 *
 */
public abstract class AbstractStatistics {

	protected final TrafficManager trafficManager;
	protected final double[] stats;

	public AbstractStatistics(int count, TrafficManager trafficManager) {
		this.trafficManager = trafficManager;
		stats = new double[count];		
	}
	
//	public boolean hasMin() {
//		return true;
//	}
//	public boolean hasMax() {
//		return true;
//	}
//	public boolean hasSum() {
//		return true;
//	}
//	public boolean hasAvg() {
//		return true;
//	}
//	public boolean hasStdDev() {
//		return true;
//	}
	
	public AbstractStatistics(TrafficManager trafficManager) {
		this(0, trafficManager);
	}

	/** 
	 * Returns true if value is non equal to zero;
	 * 
	 * @param no
	 * @param val
	 * @return
	 */
	public boolean addStat(int no, double val) {
		stats[no] += val;
		return val != 0;
	}
	
	public double getMin() {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < stats.length; i++) {
			min = Math.min(min, stats[i]);
		}
		return min;
	}

	public double getMax() {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < stats.length; i++) {
			max = Math.max(max, stats[i]);
		}
		return max;
	}
	
	public double getSum() {
		double sum = 0;
		for (int i = 0; i < stats.length; i++) {
			sum += stats[i];
		}
		return sum;
	}
	
	public double getAverage() {
		return getSum() / stats.length;
	}
	
	public double getStandardDeviation() {
		double avg = getAverage();
		double sum = 0;
		for (int i = 0; i < stats.length; i++) {
			double diff = avg - stats[i]; 
			double toAdd = diff * diff;
			sum += toAdd;
		}
		return Math.sqrt(sum / stats.length);
	}
	
	/**
	 * Returns true if statistics have been changed
	 * @return
	 */
	public abstract boolean update(int time);
	
	public abstract String getName();
	
	protected boolean singleValue() {
		return false;
	}
	
	public double getValue() {
		return 0;
	}
}
