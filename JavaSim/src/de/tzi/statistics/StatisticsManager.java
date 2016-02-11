/**
 * 2012-06-18
 */
package de.tzi.statistics;

import java.text.NumberFormat;
import java.util.Locale;

import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 *
 */
public class StatisticsManager {

	final TrafficManager trafficManager;
	
	AbstractStatistics[] statistics;
	
	public StatisticsManager(TrafficManager trafficManager,
			AbstractStatistics[] statistics) {
		this.trafficManager = trafficManager;
		this.statistics = statistics;
	}
	
	public StatisticsManager(TrafficManager trafficManager) {
		this(trafficManager, new AbstractStatistics[] {
				new TripWaitingTime(trafficManager),
				new TripTotalTime(trafficManager),
				new DistanceDriven(trafficManager),
				new AccelerationEmission(trafficManager),
				new VehiclesCount(trafficManager),
				new IntelligenceATWTImprovement(trafficManager),
				new AverageInteligenceSpeed(trafficManager),
				new AverageSpeed(trafficManager),
				new TimeSavings(trafficManager),
				new DistanceOverheads(trafficManager),
				new TargetsCountAbs(trafficManager),
				new FidelityII(trafficManager),
				new FidelityFCDEnding(trafficManager),
				new TargetsCount(trafficManager),
		});
	}
	
	public AbstractStatistics getStatisticsByClassName(Class<? extends AbstractStatistics> clazz) {
		for (int i = 0; i < statistics.length; i++) {
			if (clazz == statistics[i].getClass())
				return statistics[i];
		}
		return null;
	}
	
	public boolean update() {
		return update(trafficManager.getTime());
	}
	
	public boolean update(int time) {
		boolean affected = false;
		for (AbstractStatistics s : statistics) {
			affected |= s.update(time);
		}
		return affected;
	}
	
	public boolean updateIfNeeded(int time) {
		if (time % trafficManager.getTimeResolution() == 0)
			return update();
		return false;
	}
	
	private final String TEXT_DELIM = "\t"; 
	private final String TEXT_NEWLINE = "\n"; 
	
	NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(TEXT_NEWLINE);
		sb.append("Name");
		sb.append(TEXT_DELIM).append(TEXT_DELIM).append(TEXT_DELIM);
		sb.append("Sum");
		sb.append(TEXT_DELIM);
		sb.append("Min");
		sb.append(TEXT_DELIM);
		sb.append("Max");
		sb.append(TEXT_DELIM);
		sb.append("Average");
		sb.append(TEXT_DELIM);
		sb.append("StdDev");
		sb.append(TEXT_NEWLINE);
		for (AbstractStatistics s : statistics) {
			sb.append(s.getName());
			if (s.getName().length() < 16)
				sb.append(TEXT_DELIM);
			sb.append(TEXT_DELIM);
			if (s.singleValue()) {
				sb.append(s.getValue());
			} else {
				sb.append(formatter.format(s.getSum()));
				sb.append(TEXT_DELIM);
				sb.append(formatter.format(s.getMin()));
				sb.append(TEXT_DELIM);
				sb.append(formatter.format(s.getMax()));
				sb.append(TEXT_DELIM);
				sb.append(formatter.format(s.getAverage()));
				sb.append(TEXT_DELIM);
				sb.append(formatter.format(s.getStandardDeviation()));
			}
			sb.append(TEXT_NEWLINE);
		}
		return sb.toString();
	}
	
}
