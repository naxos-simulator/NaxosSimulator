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
 * 2012-05-25
 */
package de.tzi.graphics;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.traffic.Crossing;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 * @see http://jakobvogel.net/
 */

public class Display extends JPanel {
	
	public static boolean SHOW_PARTIAL_STATS = false;
	final static boolean SHOW_PLOT_CMD = false;
	
	public static boolean COMPLETE_STATISTICS = false;
	
	public static boolean SHOW_BOTH = false;
	public static boolean ONE_TO_FOUR = true;
	
	//public final static int STOP_AT_TIME = 150000;
	
	//public final static int STOP_AT_TIME = 300000;
	
	public static int STOP_AT_TIME = 1000000;
	
	//public final static int STOP_AT_TIME = 200000;
	
	
	private static Logger logger = Logger.getLogger(Display.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Image imageToShow; 
	private Image imageBuffer;

	final Renderer renderer;
	
	final TrafficManager trafficManager;
	
	public Display(Renderer renderer, TrafficManager trafficManager) {
		this.renderer = renderer;
		this.trafficManager = trafficManager;
		imageBuffer = createImage(renderer.getMemoryImageSource());
		imageToShow = new BufferedImage(renderer.displaySettings.width, renderer.displaySettings.height, BufferedImage.TYPE_3BYTE_BGR);
		setPreferredSize(new Dimension(renderer.displaySettings.width, renderer.displaySettings.height));
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		setFocusable(true);
		render();
		//System.out.println("s <- NULL; n <- NULL;");
	}

	public boolean render() {
		//long start = System.currentTimeMillis();
		boolean atLeastOneVehicle = renderer.render();
		//imageToShow.getGraphics().drawImage(renderer.getMapImage(), 0, 0, this);
		if (dragging) {
			imageToShow.getGraphics().fillRect(0, 0, getWidth(), getHeight());
		}
		imageToShow.getGraphics().drawImage(imageBuffer, viewDiffX, viewDiffY, this);
		repaint();
		//long stop = System.currentTimeMillis();
		//logger.debug(1000.0/(stop-start));
		return atLeastOneVehicle;
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(imageToShow, 0, 0, this);
	}

	public static boolean resetCO2 = !true; 
	protected void processKeyEvent(KeyEvent e) {
		boolean doRender = false;
		char c = e.getKeyChar();
		if (c == '.' && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.tick();
			doRender = true;
		} else if (c=='c'  && (e.getID() == KeyEvent.KEY_TYPED)) {
			computeStatistics();
		} else if (c=='r'  && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.tick();
			updateStatistics();
			doRender = true;
		} else if ((c == ' ' && (e.getID() == KeyEvent.KEY_PRESSED))||
				(c == '1' && (e.getID() == KeyEvent.KEY_TYPED))) {
			//Target reached per normal vehicle and per smart vehicle
			showQuickStatistics();
			//long time = trafficManager.tick();
			//logger.debug(time);
			doRender = true;
		} else if (c == '2' && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.changeLights();
		} else if (c == '3' && (e.getID() == KeyEvent.KEY_TYPED)) {
			logger.debug(trafficManager);
		} else if (c == '4' && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.getPropertyManager().print(System.out);
		} else if (c == '5' && (e.getID() == KeyEvent.KEY_TYPED)) {
			logger.info(renderer.displaySettings);
		} else if (c == '0' && (e.getID() == KeyEvent.KEY_TYPED)) {
			resetCO2 = true;
		} else if (c == '6' && (e.getID() == KeyEvent.KEY_TYPED)) {
			resetCO2 = !true;
		} else if (c == 'l' && (e.getID() == KeyEvent.KEY_TYPED)) {
			new Thread(new Runnable() {
				public void run() {
					try {
						for (int i = 0; i < 25000; i++) {
							//Thread.sleep(90);
							synchronized (Display.this) {
								trafficManager.tick();
								updateStatistics();
							}
							if (!render())
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
					}
				}
			}).start();
		} else if (!FPSMode && c == 't' && (e.getID() == KeyEvent.KEY_TYPED)) {
			//doStats(0);
			//doRender = true;	

			if (!shouldBreak) {
				shouldBreak = true;
			} else {
				shouldBreak = false;
				new Thread(new Runnable() {
					public void run() {
						
						int fps = GlobalConfiguration.getInstance().getInt(SettingsKeys.WINDOW_FPS);
						int milis = 1000 / fps;
						try {
							while (!Display.this.shouldBreak){
								long start = System.currentTimeMillis();
								synchronized (Display.this) {
									trafficManager.tick();
									updateStatistics();
								}
								
								if (!render())
									break;
								long diff = System.currentTimeMillis() - start;
								long toGo = Math.max(0, milis - diff);
								if (toGo > 0) {
									Thread.sleep(toGo);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							logger.error(e);
						}
					}
				}).start();
			}

			
		} else if (c == 'q' && (e.getID() == KeyEvent.KEY_TYPED)) {
			logger.info("Exitting...");
			System.exit(0);
		}	
		if (doRender)
			render();
		e.consume();
	}
	
	boolean shouldBreak = true;;
	
	public void doStats(final int trial) {
		/*
		if (false){//ONE_TO_FOUR) {
			logger.warn("RANDOMIZING SRC/DST: 1-4");
	//		Random random = new Random();
			int max = trafficManager.getVehiclesCount();
			for (int vehicleId = 1; vehicleId <= max; vehicleId++) {
				int src = 1;//random.nextInt(4) + 1;
				int dst = 4;//random.nextInt(4) + 1;
				trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.SOURCE, vehicleId, src);
				trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.DESTINATION, vehicleId, dst);
			}
		}
		*/
		FPSMode = true;
		
		
		new Thread(new Runnable() {
			public void run() {
					int mod = 1;
					float fps = GlobalConfiguration.getInstance().getFloat(SettingsKeys.WINDOW_FPS);
					long milis = 
						//0;
						(int)Math.max(1, Math.round(1000.0 / fps));
					long lastUpdate = System.currentTimeMillis();
					while (true) {
						synchronized (Display.this) {
							trafficManager.tick();
							updateStatistics();
						}
						if (System.currentTimeMillis() - lastUpdate > milis) {
							lastUpdate = System.currentTimeMillis();
							
							if (GlobalConfiguration.getInstance().getBoolean(SettingsKeys.INTERACTIVE_MODE)) {
								if (!render())
									break;
							}
						}
						if (trafficManager.getTime() % mod == 0) {
							//showQuickStatistics();
							
							if (SHOW_BOTH) {
								computeStatistics();
								computeStatistics2(trial);
							} else {
								if (COMPLETE_STATISTICS) {
									computeStatistics();
								} else {
									computeStatistics2(trial);
								}
							}
							
							if (trafficManager.getTime() >= STOP_AT_TIME) {
								if (SHOW_BOTH) {
									showTotalStats(trial);
									showTotalStats2(trial);
								} else {
									if (COMPLETE_STATISTICS) {
										showTotalStats(trial);
									} else {
										showTotalStats2(trial);
									}
								}
								logger.info("Exitting...");
								//System.exit(0);
								return;
							}
						}
						
					}
			}
		}).run();
		//start();
	}
	
	
	double totalCO2Smart = 0;
	double totalCO2Normal = 0;
	
	private void showQuickStatistics() {
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
		//logger.debug("Target count for smart and normal: "+s2+" "+n2);
		final boolean T = false;
		if (T) {
			System.out.println("s  <- c(s, "+s+ ");\tn <- c(n, "+n+"); #TRG "+trafficManager.getTime());
			System.out.println("s2 <- c(s2, "+s2+");\tn2 <- c(n2, "+n2+"); #CO2 "+trafficManager.getTime());
			System.out.println("k  <-c(k,"+totalCO2Smart*1.0/totalCO2Normal+")");
//			double s3 = speedSmart / totalSmart;
//			double n3 = speedNormal / totalNormal;
//			System.out.println("s3 <- c(s3, "+s3+");\tn3 <- c(n3, "+n3+"); #Speed "+trafficManager.getTime());
		}
	}
	//s <- list(); n <-list()
	//plot(unlist(s), col="red",type="l");lines(unlist(n), col="blue",type="l");
	//plot(unlist(s)/unlist(n), col="red",type="l");

	boolean FPSMode = false;
	
	int clickX, clickY;
	int viewDiffX, viewDiffY;
	boolean dragging = false;
	
	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			clickX = e.getX();
			clickY = e.getY();
			viewDiffX = 0;
			viewDiffY = 0;			
			Segment segment = renderer.getSegmentUnderMouse(clickX, clickY);
			if (segment != null) {
				int cellNo = renderer.getCellNoUnderMouse(clickX, clickY);
				Crossing crossing = null;
				if (cellNo == 0) {
					crossing = segment.getInput();
				} else if (cellNo == segment.getSectionLength() - 1) {
					crossing = segment.getOutput();
				}
				if (crossing != null) {
					logger.info(crossing);
				}
				int vehicleId = segment.getVehicleAtIndex(cellNo);
				logger.info(segment+" "+cellNo + " vehicle: "+vehicleId);
				if (vehicleId > 0) {
					logger.info(trafficManager.getPropertyManager().vehiclePropertiesToString(vehicleId));
				}
			}
			dragging = true;
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {			
			dragging = false;
			renderer.displaySettings.shiftX += viewDiffX;
			renderer.displaySettings.shiftY += viewDiffY;
			viewDiffX = 0;
			viewDiffY = 0;
			renderer.adjustScreenCoordinates();
			renderer.clearBackground();
			render();
		}
	}
	
	protected void processMouseMotionEvent(MouseEvent e) {
		if (dragging) {
			viewDiffX = e.getX() - clickX;
			viewDiffY = e.getY() - clickY;
			render();
		}
	}
	
	protected void processMouseWheelEvent(MouseWheelEvent e) {
		double prevZoom = e.isShiftDown() 
				? renderer.displaySettings.pixelSize
				: renderer.displaySettings.zoom;
		double newZoom = prevZoom;
		double step = 0.4;
		if (e.getWheelRotation() > 0) {
			newZoom += step;	
		} else  {
			newZoom -= step;
			if (newZoom <= 0.6) {
				newZoom = 0.6;
			}
		}
		if (e.isShiftDown()) {
			renderer.displaySettings.pixelSize = newZoom;
		} else { 
			renderer.displaySettings.shiftX *= (newZoom / prevZoom);
			renderer.displaySettings.shiftY *= (newZoom / prevZoom);
			renderer.displaySettings.zoom = newZoom;
		}
		if (prevZoom != newZoom) {
			renderer.adjustScreenCoordinates();
			renderer.clearBackground();
			render();
		}
		e.consume();

	}
	
	
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

	
	private void computeStatistics2(int trial) {
		//long simulationTimeDIV = simulationTime * trafficManager.getTimeResolution();
		
		int maxVehicle = trafficManager.getVehiclesCount();
		int smartCount = 0;
		int normalCount = 0;
		for (int vehicleId = 1; vehicleId <= maxVehicle; vehicleId++) {
			int v = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
			if (ONE_TO_FOUR) {
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
		
		int N = trafficManager.getCrossingsCount();
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
		if (SHOW_PARTIAL_STATS) {
			System.out.print('#');
			System.out.println(simulationTime);
			System.out.printf("tdcrNmS_CO2R[,%d] <- c(%d, %d, %d, %d, %d, %d, %d, %d, %d, %f, %f, %d, %d, %d)\n", trial, tns, dns, cns, rns, tnsR, dnsR, cnsR, rnsR, totalCompared, totalCO2Smart, totalCO2Normal, smartCount, normalCount, trafficManager.getRemovedVehiclesCount());
			//System.out.printf("tns[%d] <- %d / %d\n", trial, tns, totalCompared);
			//System.out.printf("dns[%d] <- %d / %d\n", trial, dns, totalCompared);
			//System.out.printf("cns[%d] <- %d / %d\n", trial, cns, totalCompared);
		}
	}
	
	public static String HEADER = "trial, tns, dns, cns, rns, tnsR, dnsR, cnsR, rnsR, "+
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
	
	private void showTotalStats2(int trial) {
		//System.out.print('#');
		//System.out.println(simulationTime);
		//System.out.printf("tns[%d] <- %d / %d\n", trial, tns, totalCompared);
		//System.out.printf("dns[%d] <- %d / %d\n", trial, dns, totalCompared);
		//System.out.printf("cns[%d] <- %d / %d\n", trial, cns, totalCompared);
		//System.out.printf("tdc[%d] <- c(%d, %d, %d, %d, %d, %d, %d)\n", trial, tns, dns, cns, lastTns, lastDns, lastCns, totalCompared);
		
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
	private void computeStatistics() {
		//FIXME:
		//totalStats.clear();
		//leadingZeros = 0;
		
		ArrayList<Double> S_t_smart = new ArrayList<Double>();
		ArrayList<Double> S_t_normal = new ArrayList<Double>();
		ArrayList<Double> S_c_smart = new ArrayList<Double>();
		ArrayList<Double> S_c_normal = new ArrayList<Double>();
		ArrayList<Double> S_d_smart = new ArrayList<Double>();
		ArrayList<Double> S_d_normal = new ArrayList<Double>();
		ArrayList<Double> S_r_smart = new ArrayList<Double>();
		ArrayList<Double> S_r_normal = new ArrayList<Double>();
		
		int N = trafficManager.getCrossingsCount();
		int MIN = 1;
		//long simulationTimeDIV = simulationTime * trafficManager.getTimeResolution();
		
		int maxVehicle = trafficManager.getVehiclesCount();
		int smartCount = 0;
		int normalCount = 0;
		for (int vehicleId = 1; vehicleId <= maxVehicle; vehicleId++) {
			if (ONE_TO_FOUR) {
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
		if (ONE_TO_FOUR && (smartCount == 0 || normalCount == 0)) {
			System.out.println("#1-4 smart: "+smartCount+" normal: "+normalCount+ " [STOP] ");
			System.exit(0);
		}
		
		for (int src = 0; src < N; src++) {
			for (int dst = 0; dst < N; dst++) {
				if (ONE_TO_FOUR) {
					if (!((src == 1 && dst == 4))) {// || (src == 4 && dst == 1))) {
						continue;
					}
				}
				int smartIdx = getIndex(src, dst, 1, N);
				int normalIdx = getIndex(src, dst, 0, N);
				if ((totalTrips[smartIdx] >= MIN) && 
						(totalTrips[normalIdx] >= MIN)) {
						//System.out.println(src+" "+dst);
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
		if (SHOW_PARTIAL_STATS) {
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
	
	private void showTotalStats(int trial) {
		System.out.print('#');
		System.out.println(simulationTime);
		for (String name : totalStats.keySet()) {
			String t = trial > 0 ? "[,"+trial+"]" : "";
			print(name + t, totalStats.get(name));
			if (SHOW_PLOT_CMD) {
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

	
	private void updateStatistics() {
		
		int N = trafficManager.getCrossingsCount();
		
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
//			int c = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.TOTAL_CELLS_DRIVEN, vehicleId);
//			if (c == 0 && simulationTime > 500) {
//				System.out.println(c);
//			}
			tripTime[vehicleId - 1]++;
			tripCO2[vehicleId - 1] += co2;
			tripDistance[vehicleId - 1] += trafficManager.getPropertyManager()
					.getVehicleProperty(VehicleProperty.CELLS_DRIVEN_IN_TRIP, vehicleId); 
			trafficManager.getPropertyManager().setVehicleProperty(
					VehicleProperty.CELLS_DRIVEN_IN_TRIP, vehicleId, 0);
			
			if (trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION_REACHED_JUST_RIGHT_NOW, vehicleId) == 1) {
//						if (tripDistance[vehicleId - 1] == 0 && simulationTime > 500) {
//					System.out.println(trafficManager.getPropertyManager());
//					int a =3;
//					a+=1;
//				}
				//System.out.println(""+tripDistance[vehicleId - 1]);
				int idx = getIndex(src, dst, smart, N);
				totalTime[idx] += tripTime[vehicleId - 1];
				totalCO2[idx] += tripCO2[vehicleId - 1];
				totalDistance[idx] += tripDistance[vehicleId - 1]; 
				totalTrips[idx] += 1;
				
				if (ONE_TO_FOUR && ((src == 1 && dst == 4) || (src == 4 && dst == 1))) {
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
