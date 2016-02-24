/*
 * Copyright (c) 2015 Michal Markiewicz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */ 

/**
 * 2016-02-12
 */
package de.tzi.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.tzi.graphics.Display;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author m
 *
 */
public class R {

	private static Logger logger = Logger.getLogger(R.class);

	final TrafficManager trafficManager;

	public R(TrafficManager trafficManager) {
		this.trafficManager=trafficManager;
	}
	

	double totalCO2Smart = 0;
	double totalCO2Normal = 0;
	
	public void showQuickStatistics() {
		int totalSmart = 0;
		int totalNormal = 0;
		int co2Smart = 0;
		int co2Normal = 0;
		double targetSmart = 0;
		double targetNormal = 0;
		for (int vehicleId = 1; vehicleId <= trafficManager.getVehiclesCount(); vehicleId++) {
			int v = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (v == -1)
				continue;
			int src = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
			int dst = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId);
			double distanceCost = 
				trafficManager.getNavigator().travelDistance(src, dst, false) +
				trafficManager.getNavigator().travelDistance(dst, src, false);
			distanceCost /= 2;
			boolean smart = 1 == v;
			int targetCount = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION_REACH_COUNTER, vehicleId);
			int co2 = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId);
			//int speed = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SPEED_INDEX_NASCH, vehicleId);
			if (smart) {
				totalSmart++;
				targetSmart += targetCount / distanceCost;
				co2Smart += co2 / distanceCost * 100;
//				speedSmart += speed;
			} else {
				totalNormal++;
				targetNormal += targetCount / distanceCost;
				co2Normal += co2 / distanceCost * 100;
//				speedNormal += speed;
			}
		}
		totalCO2Smart += 1.0 * co2Smart / totalSmart;
		totalCO2Normal += 1.0 *  co2Normal / totalNormal;
		double s = 1.0 * targetSmart / totalSmart / trafficManager.getTime();
		double n = 1.0 * targetNormal / totalNormal / trafficManager.getTime();
//		double k = s / n;
		double s2 = (((1.0 * co2Smart / (targetSmart / totalSmart)) / totalSmart) / trafficManager.getTime());
		double n2 = (((1.0 * co2Normal / (targetNormal  / totalNormal)) / totalNormal) / trafficManager.getTime());
		final boolean T = false;
		if (T) {
			System.out.println("s  <- c(s, "+s+ ");\tn <- c(n, "+n+"); #TRG "+trafficManager.getTime());
			System.out.println("s2 <- c(s2, "+s2+");\tn2 <- c(n2, "+n2+"); #CO2 "+trafficManager.getTime());
			System.out.println("k  <-c(k,"+totalCO2Smart*1.0/totalCO2Normal+")");
		}
	}
	boolean FPSMode = false;
	
	
	long totalTime[];
	long totalDistance[];
	long totalCO2[];
	long totalTrips[];
	
	int getIndex(int src, int dst, boolean smartB, int N) {
		int smart = smartB ? 1 : 0;
		return getIndex(src, dst, smart, N);
	}
	
	int getIndex(int src, int dst, int smart, int N) {
		return 2 * (src + (N + 1) * dst) + smart;
	}
	
	int getMaxIndex(int N) {
		return 2 * (N + (N + 1) * N) + 2;
	}
	
	
	int tripTime[];
	int tripCO2[];
	int tripDistance[];
	
	private long simulationTime = 0;
	
	int leadingZeros; // How many stats are missing due to lack of data
	
	
	Map<String, List<Double>> totalStats = new TreeMap<String, List<Double>>();
	
	
	final private int H(double t) {
		if (t < 0)
			return 0;
		return 1;
	}

	final private int HR(double t) {
		if (t > 0)
			return 0;
		return 1;
	}

	
	long tns = 0;
	long dns = 0;
	long cns = 0;
	long rns = 0;
	long tnsR = 0;
	long dnsR = 0;
	long cnsR = 0;
	long rnsR = 0;
	int totalCompared = 0;
	
	long tns14 = 0;
	long dns14 = 0;
	long cns14 = 0;
	long rns14 = 0;
	long tnsR14 = 0;
	long dnsR14 = 0;
	long cnsR14 = 0;
	long rnsR14 = 0;
	int totalCompared14 = 0;
	
	
	
	long lastTns = 0;
	long lastDns = 0;
	long lastCns = 0;
	long lastRns = 0;

	
	public void computeStatistics2(int trial) {
		//long simulationTimeDIV = simulationTime * trafficManager.getTimeResolution();
		
		int maxVehicle = trafficManager.getVehiclesCount();
		int smartCount = 0;
		int normalCount = 0;
		for (int vehicleId = 1; vehicleId <= maxVehicle; vehicleId++) {
			int v = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (Display.ONE_TO_FOUR) {
				int o = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
				int d = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId);
				if (!(o == 1 && d == 4)) {
					continue;
				}
			}
			if (v == 1) {
				smartCount++;
			} else if (v == 0) {
				normalCount++;
			}
		}
		
		int N = trafficManager.getNavigator().getPossibleDestinationCount();

		int MIN = 1;
		lastCns = lastDns = lastTns = 0;
		for (int src = 0; src < N; src++) {
			for (int dst = 0; dst < N; dst++) {
				int smartIdx = getIndex(src, dst, 1, N);
				int normalIdx = getIndex(src, dst, 0, N);
				if ((totalTrips[smartIdx] >= MIN) && 
						(totalTrips[normalIdx] >= MIN)) {
					
					boolean add14 = src == 1 && dst == 4;
					if (add14) {
						totalCompared14++;
					}

					totalCompared++;
					{
						double avg_smart_t = 1.0 * totalTime[smartIdx] / totalTrips[smartIdx];
						double avg_normal_t = 1.0 * totalTime[normalIdx] / totalTrips[normalIdx];
						tns += H(avg_normal_t - avg_smart_t);
						tnsR += HR(avg_normal_t - avg_smart_t);
						lastTns += H(avg_normal_t - avg_smart_t);
						if (add14) {
							tns14 += H(avg_normal_t - avg_smart_t);
							tnsR14 += HR(avg_normal_t - avg_smart_t);
						}
						
					}
					{
						double avg_smart_c = 1.0 * totalCO2[smartIdx] / totalTrips[smartIdx];
						double avg_normal_c = 1.0 * totalCO2[normalIdx] / totalTrips[normalIdx];
						cns += H(avg_normal_c - avg_smart_c);
						cnsR += HR(avg_normal_c - avg_smart_c);
						lastCns += H(avg_normal_c - avg_smart_c);
						if (add14) {
							cns14 += H(avg_normal_c - avg_smart_c);
							cnsR14 += HR(avg_normal_c - avg_smart_c);
						}
					}
					{
						double avg_smart_d = 1.0 * totalDistance[smartIdx] / totalTrips[smartIdx];
						double avg_normal_d = 1.0 * totalDistance[normalIdx] / totalTrips[normalIdx];
						dns += H(avg_normal_d - avg_smart_d);
						dnsR += HR(avg_normal_d - avg_smart_d);
						lastDns += H(avg_normal_d - avg_smart_d);
						if (add14) {
							dns14 += H(avg_normal_d - avg_smart_d);
							dnsR14 += HR(avg_normal_d - avg_smart_d);
						}
					}
					{
						double avg_smart_r = 1.0 * totalTrips[smartIdx] / smartCount;// /  simulationTimeDIV;
						double avg_normal_r = 1.0 * totalTrips[normalIdx] / normalCount;// / simulationTimeDIV;
						rns += H(avg_normal_r - avg_smart_r);
						rnsR += HR(avg_normal_r - avg_smart_r);
						lastRns += H(avg_normal_r - avg_smart_r);
						if (add14) {
							rns14 += H(avg_normal_r - avg_smart_r);
							rnsR14 += HR(avg_normal_r - avg_smart_r);
						}
					}
					
				}
			}
		}
		if (Display.SHOW_PARTIAL_STATS) {
			System.out.print('#');
			System.out.println(simulationTime);
			System.out.printf("tdcrNmS_CO2R[,%d] <- c(%d, %d, %d, %d, %d, %d, %d, %d, %d, %f, %f, %d, %d, %d)\n", trial, tns, dns, cns, rns, tnsR, dnsR, cnsR, rnsR, totalCompared, totalCO2Smart, totalCO2Normal, smartCount, normalCount, trafficManager.getRemovedVehiclesCount());
		}
	}
	
	public static String HEADER = "#trial, tns, dns, cns, rns, tnsR, dnsR, cnsR, rnsR, "+
		"totalCompared, normalCount, smartCount, removedVehiclesCount, "+
		"totalCO2N, totalCO2S, "+
		"totalTripsN ,totalTripsS, "+
		"totalTimeN, totalTimeS, "+
		"totalDistanceN, totalDistanceS, "+
		"totalCO2N14, totalCO2S14, "+
		"totalTripsN14, totalTripsS14, "+
		"totalTimeN14, totalTimeS14, "+
		"totalDistanceN14, totalDistanceS14, "+
		"normalCount14, smartCount14, "+
		"tns14, dns14, "+
		"cns14, rns14, "+
		"tnsR14, dnsR14, "+
		"cnsR14, rnsR14, "+
		"totalCompared14 ";
	
	public void showTotalStats2(int trial) {
		int smartCount = 0;
		int normalCount = 0;
		int smartCount14 = 0;
		int normalCount14 = 0;
		for (int vehicleId = 1; vehicleId <= trafficManager.getVehiclesCount(); vehicleId++) {
			int smart = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			int src = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
			int dst = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId);
			if (smart == 1) {
				smartCount++;
				if ((src == 1 && dst == 4) || (src == 4 && dst == 1)) {
					smartCount14++;
				}
			} else if (smart == 0) {
				normalCount++;
				if ((src == 1 && dst == 4) || (src == 4 && dst == 1)) {
					normalCount14++;
				}
			}
		}
		
		System.out.printf("tdcrNmS_CO2R[,%d] <- c("+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d, %d, "+
				"%d, %d, %d, %d"+
				")\n",
						trial, tns, dns, cns, rns, tnsR, dnsR, cnsR, rnsR,
						totalCompared,
						//totalCO2Smart, totalCO2Normal,
						//[10]		[11]
						normalCount, smartCount,
						//[12]
						trafficManager.getRemovedVehiclesCount(),
						//[13] CO2 N, [14] CO2 S
						totalCO2N, totalCO2S,
						//[15] total trips N, [16] total trips S, 
						totalTripsN ,totalTripsS,
						//[17] total time N, [18] total time S, 
						totalTimeN, totalTimeS,
						//[19] total distance N, [20] total distance S
						totalDistanceN, totalDistanceS,
						//[21] CO2 N, [22] CO2 S
						totalCO2N14, totalCO2S14,
						//[23] total trips N, [24] total trips S, 
						totalTripsN14, totalTripsS14,
						//[25] total time N, [26] total time S, 
						totalTimeN14, totalTimeS14,
						//[27] total distance N, [28] total distance S
						totalDistanceN14, totalDistanceS14,
						//[29]		   [30]
						normalCount14, smartCount14,
						
						//[31] [32]
						tns14, dns14,
						//[33] [34]
						cns14, rns14,
						//[35] [36]
						tnsR14, dnsR14,
						//[37] [38]
						cnsR14, rnsR14,
						//[39]
						totalCompared14
						);
	}

	/*
	private long sumAll(long a[]) {
		long res = 0;
		for (long l : a) {
			res += l;
		}
		return res;
	}
	*/
	public void computeStatistics() {
		
		ArrayList<Double> S_t_smart = new ArrayList<Double>();
		ArrayList<Double> S_t_normal = new ArrayList<Double>();
		ArrayList<Double> S_c_smart = new ArrayList<Double>();
		ArrayList<Double> S_c_normal = new ArrayList<Double>();
		ArrayList<Double> S_d_smart = new ArrayList<Double>();
		ArrayList<Double> S_d_normal = new ArrayList<Double>();
		ArrayList<Double> S_r_smart = new ArrayList<Double>();
		ArrayList<Double> S_r_normal = new ArrayList<Double>();
		
		int N = trafficManager.getNavigator().getPossibleDestinationCount();
		int MIN = 1;
		//long simulationTimeDIV = simulationTime * trafficManager.getTimeResolution();
		
		int maxVehicle = trafficManager.getVehiclesCount();
		int smartCount = 0;
		int normalCount = 0;
		for (int vehicleId = 1; vehicleId <= maxVehicle; vehicleId++) {
			if (Display.ONE_TO_FOUR) {
				int o = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
				int d = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId);
				if (!(o == 1 && d == 4) && !(o== 4 && d == 1)) {
					continue;
				}
			}
			int v = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (v == 1) {
				smartCount++;
			} else if (v == 0) {
				normalCount++;
			}
		}
		if (Display.ONE_TO_FOUR && (smartCount == 0 || normalCount == 0)) {
			System.out.println("#1-4 smart: "+smartCount+" normal: "+normalCount+ " [STOP] ");
			System.exit(0);
		}
		
		for (int src = 0; src < N; src++) {
			for (int dst = 0; dst < N; dst++) {
				if (Display.ONE_TO_FOUR) {
					if (!((src == 1 && dst == 4))) {// || (src == 4 && dst == 1))) {
						continue;
					}
				}
				int smartIdx = getIndex(src, dst, 1, N);
				int normalIdx = getIndex(src, dst, 0, N);
				if ((totalTrips[smartIdx] >= MIN) && 
						(totalTrips[normalIdx] >= MIN)) {
						{
							double avg_smart_t = 1.0 * totalTime[smartIdx] / totalTrips[smartIdx];
							S_t_smart.add(avg_smart_t);
							updateTotalStats("S_t_smart", avg_smart_t);
						}
						{
							double avg_normal_t = 1.0 * totalTime[normalIdx] / totalTrips[normalIdx];
							S_t_normal.add(avg_normal_t);
							updateTotalStats("S_t_normal", avg_normal_t);
						}
						{
							double avg_smart_c = 1.0 * totalCO2[smartIdx] / totalTrips[smartIdx];
							S_c_smart.add(avg_smart_c);
							updateTotalStats("S_c_smart", avg_smart_c);
						}
						{
							double avg_normal_c = 1.0 * totalCO2[normalIdx] / totalTrips[normalIdx];
							S_c_normal.add(avg_normal_c);
							updateTotalStats("S_c_normal", avg_normal_c);
						}
						{
							double avg_smart_d = 1.0 * totalDistance[smartIdx] / totalTrips[smartIdx];
							S_d_smart.add(avg_smart_d);
							updateTotalStats("S_d_smart", avg_smart_d);
						}
						{
							double avg_normal_d = 1.0 * totalDistance[normalIdx] / totalTrips[normalIdx];
							S_d_normal.add(avg_normal_d);
							updateTotalStats("S_d_normal", avg_normal_d);
						}
						{
							
							double avg_smart_r = 1.0 * totalTrips[smartIdx] / smartCount;// /  simulationTimeDIV;
							S_d_smart.add(avg_smart_r);
							updateTotalStats("S_r_smart", avg_smart_r);
						}
						{
							double avg_normal_r = 1.0 * totalTrips[normalIdx] / normalCount;// / simulationTimeDIV;
							S_d_normal.add(avg_normal_r);
							updateTotalStats("S_r_normal", avg_normal_r);
						}
				} else {
					leadingZeros++;
				}
			}
		}
		if (Display.SHOW_PARTIAL_STATS) {
			System.out.print('#');
			System.out.println(simulationTime);
			print("S_t_smart", S_t_smart);
			print("S_t_normal", S_t_normal);
			print("S_c_smart", S_c_smart);
			print("S_c_normal", S_c_normal);
			print("S_d_smart", S_d_smart);
			print("S_d_normal", S_d_normal);
			print("S_r_smart", S_r_smart);
			print("S_r_normal", S_r_normal);
		}
		
	}
	
	private void updateTotalStats(String name, double val) {
		List<Double> l = totalStats.get(name);
		if (l == null) {
			totalStats.put(name, l = new LinkedList<Double>()); 
		}
		l.add(val);
	}
	
	public void showTotalStats(int trial) {
		System.out.print('#');
		System.out.println(simulationTime);
		for (String name : totalStats.keySet()) {
			String t = trial > 0 ? "[,"+trial+"]" : "";
			print(name + t, totalStats.get(name));
			if (Display.SHOW_PLOT_CMD) {
				System.out.println("plot("+name+",type=\"p\",col=rgb(0,0,0,0.2), cex=.1);");
			}
		}
	}
	
	
	void print(String name, Collection<Double> al) {
		System.out.print(name);
		System.out.print(" <- c(");
		
		int max = leadingZeros;
		
		while (max-- > 0) {
			System.out.print("NA,");
		}
		
		max = al.size();
		for (Double d : al) {
			System.out.format("%.4f", d);
			if (--max > 0) {
				System.out.print(',');		
			}
		}
		System.out.println(");");
	}
	
	long totalTripsN;
	long totalTripsS;
	long totalTimeN;
	long totalTimeS;
	long totalDistanceN;
	long totalDistanceS;
	long totalCO2N;
	long totalCO2S;

	long totalTripsN14;
	long totalTripsS14;
	long totalTimeN14;
	long totalTimeS14;
	long totalDistanceN14;
	long totalDistanceS14;
	long totalCO2N14;
	long totalCO2S14;

	
	public void updateStatistics() {
		
		int N = trafficManager.getNavigator().getPossibleDestinationCount();
		
		if (totalTime == null) {
			int size = getMaxIndex(N);
			totalTime = new long[size];
			totalDistance = new long[size];
			totalCO2 = new long[size];
			totalTrips = new long[size];
			tripTime = new int [trafficManager.getVehiclesCount()];
			tripCO2 = new int[trafficManager.getVehiclesCount()];
			tripDistance = new int[trafficManager.getVehiclesCount()];
		}
		
		simulationTime++;
		for (int vehicleId = 1; vehicleId <= trafficManager.getVehiclesCount(); vehicleId++) {
			int v = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (v == -1)
				continue;
			int src = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
			int dst = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId);
			/*
			double distanceCost = 
				trafficManager.getNavigator().travelDistance(src, dst, false) +
				trafficManager.getNavigator().travelDistance(dst, src, false);
			distanceCost /= 2;
			*/
			boolean smart = 1 == v;
			int co2 = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId);
			if (smart) {
				totalCO2Smart += co2;
			} else {
				totalCO2Normal += co2;
			}
			
			trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.CO2_EMITTED, vehicleId, 0);
			tripTime[vehicleId - 1]++;
			tripCO2[vehicleId - 1] += co2;
			tripDistance[vehicleId - 1] += trafficManager.getPropertyManager()
					.getVehicleProperty(VehicleProperty.CELLS_DRIVEN_IN_TRIP, vehicleId); 
			trafficManager.getPropertyManager().setVehicleProperty(
					VehicleProperty.CELLS_DRIVEN_IN_TRIP, vehicleId, 0);
			
			if (trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION_REACHED_JUST_RIGHT_NOW, vehicleId) == 1) {
				int idx = getIndex(src, dst, smart, N);
				try {
					totalTime[idx] += tripTime[vehicleId - 1];
					totalCO2[idx] += tripCO2[vehicleId - 1];
					totalDistance[idx] += tripDistance[vehicleId - 1]; 
					totalTrips[idx] += 1;
				} catch (ArrayIndexOutOfBoundsException e) {
					logger.error(trafficManager.getPropertyManager().vehiclePropertiesToString(vehicleId));
					logger.error(e);
				}
				if (Display.ONE_TO_FOUR && ((src == 1 && dst == 4) || (src == 4 && dst == 1))) {
					if (smart) {
						totalTripsS14++;
						totalTimeS14 += tripTime[vehicleId - 1];
						totalDistanceS14 += tripDistance[vehicleId - 1];
						totalCO2S14 += tripCO2[vehicleId - 1];
					} else {
						totalTripsN14++; 
						totalTimeN14 += tripTime[vehicleId - 1];
						totalDistanceN14 += tripDistance[vehicleId - 1];
						totalCO2N14 += tripCO2[vehicleId - 1];
					}
				}
				if (smart) {
					totalTripsS++;
					totalTimeS += tripTime[vehicleId - 1];
					totalDistanceS += tripDistance[vehicleId - 1];
					totalCO2S += tripCO2[vehicleId - 1];
				} else {
					totalTripsN++; 
					totalTimeN += tripTime[vehicleId - 1];
					totalDistanceN += tripDistance[vehicleId - 1];
					totalCO2N += tripCO2[vehicleId - 1];
				}
				tripTime[vehicleId - 1] = 0;
				tripCO2[vehicleId - 1] = 0;
				tripDistance[vehicleId - 1] = 0;
				trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.DESTINATION_REACHED_JUST_RIGHT_NOW, vehicleId, 0);
			}
		}
	}
}
