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
 * 2014-05-13
 */
package de.tzi.readouts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Michal Markiewicz
 *
 */
public class AccCalculator {

	private static Logger logger = Logger.getLogger(AccCalculator.class);

	final static int MIN_SEQUENCE_LENGTH = 5;//5;//10
	
//	final static String FILE_NAME = "/Users/m/SVN/Doktorat/Dane/TBL/gpx_lublin.tbl";	
//	final static String HISTOGRAM_OUTPUT = "/tmp/out-LU-"+MIN_SEQUENCE_LENGTH+".txt";

	final static String FILE_NAME = "/Users/m/SVN/Doktorat/Dane/TBL/gpx_krakow.tbl";
	final static String HISTOGRAM_OUTPUT = "/tmp/out-KR-"+MIN_SEQUENCE_LENGTH+".txt";
	final static String SPEED_ACCELERATION_OUTPUT = "/tmp/out-KR-sa.txt";

	final boolean SAVE_HISTOGRAM = !false;
	final boolean SAVE_SPEED_ACCELERATION = !false;
	
	final static boolean FIND_ERRORS = false;
	
	static boolean DECELLERATIONS = true;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		new AccCalculator().compute();
	}
	
	void compute() throws Exception {
	
		FileReader fr = new FileReader(FILE_NAME);
		BufferedReader br = new BufferedReader(fr); 
		String line;
		int lineNo = 0;
		double fromLat, fromLon, toLat = 0, toLon = 0;
		long fromTime, toTime = 0;
		double fromVkmh, toVkmh = 0;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fromLine, toLine = null;
		//50 lat
				
		
		int MAX_IN_SEQUENCE = 100;
		double[]  sequence = new double[MAX_IN_SEQUENCE]; 
		int currentInSequence = 0;
		
		int MAX_SPEED = 150;
		
		int[] distributionOfSequenceLengths = new int[MAX_IN_SEQUENCE];
		int[] vehicleWasDrivingAtThisSpeed = new int[MAX_SPEED];
		@SuppressWarnings("unchecked")
		List<Double>[] accelerationsAtGivenSpeed = new List[MAX_SPEED]; 
		
		accelerationsAtGivenSpeed[0] = null;
		int errors = 0;
		double maxAkmhs = 0;
		double distanceInKmHs = 0;
		double distanceInKmHsAll = 0;
		double distancePointsDiff = 0;
		int sequenceCounter = 0;
		int numberOfReadoutsInSequences = 0;
		while ((line = br.readLine()) != null) {
			lineNo++;
			String[] fields = line.split(",");
			fromLat = toLat;
			fromLon = toLon;
			fromTime = toTime;
			fromLine = toLine;
			fromVkmh = toVkmh;
			toLat = Double.parseDouble(fields[0]);
			toLon = Double.parseDouble(fields[1]);
			toTime = formatter.parse(fields[3]).getTime() / 1000;
			toLine = line;
			
			if (lineNo > 1) {
				double d = distanceInMeters(fromLat, fromLon, toLat, toLon);
				distancePointsDiff += d;
				long t = toTime - fromTime;
				double vms = d/t;
				double kmh = vms / 1000 * 3600;
				double kmhFromGPS = Double.parseDouble(fields[2]);
				toVkmh = kmh;
				
				double akmhs = (toVkmh - fromVkmh) /  t;
				
				if (toVkmh < 150) {
					distanceInKmHsAll += toVkmh;
				}
				
				//Fixme: akhm: 15
				boolean valid = !(t != 1 || t <= 0 || Math.abs(kmhFromGPS - kmh) > 10 || d > 500 || akmhs > 15);
				
				if (DECELLERATIONS) {
					valid = !(t != 1 || t <= 0 || Math.abs(kmhFromGPS - kmh) > 10 || d > 500 || akmhs > 30 || kmh > 140);
				}
				
				if (!valid) {
					errors++;
					currentInSequence = 0;
					if (FIND_ERRORS) {
						//logger.info(d+"m "+t+"s "+vms+"m/s "+kmh+"km/h "+fields[2]+ " "+akmhs+"km/h/s");
						logger.info(String.format("%03.1fm %03ds %02.1fm/s %02.1fkm/h - > %02.1fkm/h %s %02.1fkm/h/s ", d,t,vms,fromVkmh, toVkmh,fields[2],akmhs));
						logger.info("Prev: "+fromLine);
						logger.info("Curr: "+toLine);
					}
				} else { 
					maxAkmhs = Math.max(maxAkmhs, akmhs);
					if (currentInSequence >= MAX_IN_SEQUENCE) {
						logger.error("Allocate more space for the speed sequence!");
						break;
					}
				}
				if (valid && (currentInSequence == 0 || 
						(currentInSequence > 0 && 
								((!DECELLERATIONS && sequence[currentInSequence - 1] < kmhFromGPS) ||
								((DECELLERATIONS && sequence[currentInSequence - 1] > kmhFromGPS)) )))) {
					sequence[currentInSequence++] = kmhFromGPS;
				} else {
					distributionOfSequenceLengths[currentInSequence]++;
					
					//10 - OK
					//5 - OK
					//2 - OK
					if (currentInSequence >= MIN_SEQUENCE_LENGTH && ((!DECELLERATIONS || 
							(DECELLERATIONS && sequence[currentInSequence - 1] < 5
							)) )) {
						numberOfReadoutsInSequences += currentInSequence;
						for (int i = 1; i < currentInSequence; i++) {
							distanceInKmHs += sequence[i-1];
							int startSpeed = (int)Math.round(sequence[i - 1]);
							int stopSpeed = (int)Math.round(sequence[i]);
							if (DECELLERATIONS) {
								int tmp = startSpeed;
								startSpeed = stopSpeed;
								stopSpeed = tmp;
							}
							double a = sequence[i] - sequence[i - 1];
							for (int j = startSpeed; j < stopSpeed; j++) {
								vehicleWasDrivingAtThisSpeed[j]++;
								if (accelerationsAtGivenSpeed[j] == null) {
									accelerationsAtGivenSpeed[j] = new LinkedList<Double>();
								}
								accelerationsAtGivenSpeed[j].add(a);
							}
						}
						sequenceCounter++;
					}
					if (currentInSequence > 35) {
						StringBuffer sb = new StringBuffer(currentInSequence * 5);
						sb.append("v40 <- c(");
						//sb.append(currentInSequence);
						for (int i = 0; i < currentInSequence; i++) {
							//sb.append((int)sequence[i]);
							sb.append(String.format("%.2f", sequence[i]));
							if (i + 1 < currentInSequence) {
								sb.append(',');
							}
						}
						sb.append(")");
						logger.info(sb.toString());
					}
					currentInSequence = 0;
				}
				
				//We can assume that we have correct data here
			}
		}
		//
		StringBuffer sb = new StringBuffer(MAX_IN_SEQUENCE * 3);
		sb.append("dosl <- c(");
		for (int i = 1; i < MAX_IN_SEQUENCE; i++) {
			sb.append(distributionOfSequenceLengths[i]);
			if (i + 1 < MAX_IN_SEQUENCE) {
				sb.append(',');
			}
		}
		sb.append(')');
		//
		StringBuffer sb10 = new StringBuffer(MAX_IN_SEQUENCE * 3);
		sb10.append("dosl10 <- c(");
		for (int i = 10; i < MAX_IN_SEQUENCE; i++) {
			sb10.append(distributionOfSequenceLengths[i]);
			if (i + 1 < MAX_IN_SEQUENCE) {
				sb10.append(',');
			}
		}
		sb10.append(')');
		//
		logger.info("Distribution of sequence lengths: (all and then the sequences with at least ten elements)");
		logger.info(sb.toString());
		logger.info(sb10.toString());
		//
		sb.setLength(0);
		sb.setLength(MAX_SPEED * 4);
		sb.append("vSpeedFreq <- c(");
		for (int i = 0; i < MAX_SPEED; i++) {
			sb.append(vehicleWasDrivingAtThisSpeed[i]);
			if (i + 1 < MAX_SPEED) {
				sb.append(',');
			}
		}
		sb.append(')');
		logger.info("How many times the vehicle has a given speed");
		logger.info(sb.toString());
		//
		sb.setLength(0);
		sb.setLength(MAX_SPEED * 4);
		sb.append("vacc <- c(");
		for (int i = 0; i < MAX_SPEED; i++) {
			sb.append(String.format("%.2f", avg(accelerationsAtGivenSpeed[i])));
			if (i + 1 < MAX_SPEED) {
				sb.append(',');
			}
		}
		sb.append(')');
		logger.info("What was the value of acceleration at given speed (mean)");
		logger.info(sb.toString());
		//
		sb.setLength(0);
		sb.setLength(MAX_SPEED * 4);
		sb.append("vacc2 <- c(");
		for (int i = 0; i < MAX_SPEED; i++) {
			sb.append(String.format("%.2f", median(accelerationsAtGivenSpeed[i])));
			if (i + 1 < MAX_SPEED) {
				sb.append(',');
			}
		}
		sb.append(')');
		logger.info("What was the value of acceleration at given speed (median)");
		logger.info(sb.toString());
		logger.info("Errors: "+errors+ " "+String.format("%.3f", (errors*100.0/lineNo))+"% out of total "+lineNo+" records");
		//
		sb.setLength(0);
		sb.setLength(MAX_SPEED * 4);
		sb.append("vacc3 <- c(");
		for (int i = 0; i < MAX_SPEED; i++) {
			sb.append(String.format("%.2f", stdDev(accelerationsAtGivenSpeed[i])));
			if (i + 1 < MAX_SPEED) {
				sb.append(',');
			}
		}
		sb.append(')');
		logger.info("What was the value of acceleration at given speed (Std Dev)");
		logger.info(sb.toString());
		//
		logger.info("The values of acceleration for a given speed: ");
		for (int speed = 0; speed < MAX_SPEED; speed++) {
			List<Double> aags = accelerationsAtGivenSpeed[speed];
			sb.setLength(0);
			//sb.setLength(aags != null ? aags.size() * 3 + 10: 10);
			sb.append("sa"+speed+" <- c(");
			if (aags != null) {
				Double[] sorted = aags.toArray(new Double[0]);
				Arrays.sort(sorted);
				for (int i = 0; i < sorted.length; i++) {
					sb.append(String.format("%.2f", sorted[i]));
					if (i + 1 < sorted.length) {
						sb.append(',');
					}
				}
			}
			sb.append(");\n");
			if (aags != null && aags.size() > 1) {
				sb.append("s = ");
				if (DECELLERATIONS) {
					sb.append("-");
				}
				sb.append("sa").append(speed).append("; distname=\"gamma\"; f1 <-fitdist(s,distname);\n");
				sb.append("est[").append(speed+1).append(",1]=f1$estimate[1];\n");
				sb.append("est[").append(speed+1).append(",2]=1/f1$estimate[2];\n");
			}
			
			if (SAVE_SPEED_ACCELERATION) {
				if (speed == 0) {
					saveFile(SPEED_ACCELERATION_OUTPUT, "est = array(dim=c("+MAX_SPEED+", 2));\n", false);
				}
				saveFile(SPEED_ACCELERATION_OUTPUT, sb.toString(), true);
			}
			//logger.info(sb.toString());	
		}
		//
		sb.setLength(0);
		int buckets = (int)Math.round(maxAkmhs)*1;//35;
		sb.setLength(MAX_SPEED * buckets * 10);
		logger.info("Max acc: "+maxAkmhs);
		sb.append("\naDistr <- array(dim=c(").append(MAX_SPEED).append(", ").append(buckets).append("))\n");
		int[] bucketsCounts = new int[buckets];
		for (int s = 0; s < MAX_SPEED; s++) {
			List<Double> aags2 = accelerationsAtGivenSpeed[s];
			if (aags2 == null) {
				continue;
			}
			for (int i = 0; i < bucketsCounts.length; i++) {
				bucketsCounts[i] = 0;
			}
			for (Double d : aags2) {
				int idx = (int)((d / maxAkmhs) * (buckets));
				if (DECELLERATIONS) {
					idx = (int)((d / -maxAkmhs) * (buckets));
				}
				idx = Math.min(idx, buckets - 1);
				idx = Math.max(idx, 0);
				bucketsCounts[idx]++;
			}
			for (int i = 0; i < bucketsCounts.length; i++) {
				sb.append("aDistr[").append(s+1).append(',').append(i+1).append("]=").append(bucketsCounts[i]).append('\n');
			}
		}
		logger.info("The distribution of acceleration/speed");
		//logger.info(sb.toString());
		
		if (SAVE_HISTOGRAM) {
			saveFile(HISTOGRAM_OUTPUT, sb.toString(), false);
		}
		
		//
		logger.info("Errors: "+errors+ " "+String.format("%.3f", (errors*100.0/lineNo))+"% out of total "+lineNo+" records. Valid: "+(lineNo - errors));
		logger.info("Total number of sequences: "+sequenceCounter);
		logger.info("Total number of readouts in sequences: "+numberOfReadoutsInSequences);
		logger.info("Total distance: "+String.format("%.3f", distanceInKmHs/3600));
		logger.info("Distance in file: "+String.format("%.3f", distanceInKmHsAll/3600));
		logger.info("Distance between points in file: "+String.format("%.3f", distancePointsDiff/1000));
		
		br.close();
	}
	

	private void saveFile(String fileName, String content, boolean append) throws Exception {
		FileWriter fw = new FileWriter(fileName, append);
		fw.write(content);
		fw.close();
	}

	private double avg(List<Double> list) {
		if (list == null) {
			return 0;
		}
		double count = list.size();
		double sum = 0;
		for (Double d : list) {
			sum += d;
		}
		return sum / count;
	}
	
	private double median(List<Double> list) {
		if (list == null) {
			return 0;
		}
		if (list.size() == 1)
			return list.get(0).doubleValue();
		Double[] sorted = list.toArray(new Double[0]);
		Arrays.sort(sorted);
		double a = sorted[(int)Math.ceil(sorted.length /  2.0)].doubleValue();
		double b = sorted[(int)Math.floor(sorted.length /  2.0)].doubleValue();
		return (a+b)/2;
	}
	
	private double stdDev(List<Double> list) {
		if (list == null) {
			return 0;
		}
		double mean = avg(list);
		double count = list.size();
		double sum = 0;
		for (Double d : list) {
			double r = d - mean; 
			sum += r * r;
		}
		return Math.sqrt(sum / count);
	}


	/// @brief The usual PI/180 constant
	final static double DEG_TO_RAD = 0.017453292519943295769236907684886;
	/// @brief Earth's quatratic mean radius for WGS-84
	final static double EARTH_RADIUS_IN_METERS = 6372797.560856;

	/** 
	  * @see Taken from http://blog.julien.cayzac.name/2008/10/arc-and-distance-between-two-points-on.html
	  */
	double arcInRadians(double fromLat, double fromLon, double toLat, double toLon) {
	    double latitudeArc  = (fromLat - toLat) * DEG_TO_RAD;
	    double longitudeArc = (fromLon - toLon) * DEG_TO_RAD;
	    double latitudeH = Math.sin(latitudeArc * 0.5);
	    latitudeH *= latitudeH;
	    double lontitudeH = Math.sin(longitudeArc * 0.5);
	    lontitudeH *= lontitudeH;
	    double tmp = Math.cos(fromLat*DEG_TO_RAD) * Math.cos(toLat*DEG_TO_RAD);
	    return 2.0 * Math.asin(Math.sqrt(latitudeH + tmp*lontitudeH));
	}
	
	/** @brief Computes the distance, in meters, between two WGS-84 positions.
	  *
	  * The result is equal to <code>EARTH_RADIUS_IN_METERS*ArcInRadians(from,to)</code>
	  *
	  * @sa ArcInRadians
	  */
	double distanceInMeters(double fromLat, double fromLon, double toLat, double toLon) {
	    return EARTH_RADIUS_IN_METERS*arcInRadians(fromLat, fromLon, toLat, toLon);
	}
	
}
