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
 * 2012-07-10
 */
package de.tzi.scenarios;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.geometry.City;
import de.tzi.graphics.Display;
import de.tzi.graphics.Renderer;
import de.tzi.graphics.WorldMap;
import de.tzi.io.Loader;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.lights.LightControllerFactory;
import de.tzi.traffic.navigation.NavigatorFactory;
import de.tzi.traffic.strategy.NaSchCO2;
import de.tzi.traffic.strategy.StrategyFactory;

/**
 * @author Michal Markiewicz
 *
 */
public class Scenario {

	static boolean SHOW_GUI = false;// GlobalConfiguration.getInstance().getBoolean(SettingsKeys.INTERACTIVE_MODE);

	protected static int MAX_TRIALS = 1;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		double rho = 0.25;
		double ratio = 0.5;
				
			
		if (args.length > 0) {
			ratio = Double.parseDouble(args[0]);
		}
		
		if (args.length > 1) {
			rho = Double.parseDouble(args[1]);
		}

		if (args.length > 2) {
			MAX_TRIALS = Integer.parseInt(args[2]);
		}

		String map = "GEN13"; //GEN13 - CIRCLE CITY; SMALL TWO DIRECTIONS WITH U-TURNS 6x6 200 dist - GEN16
		if (args.length > 3) {
			map = args[3];
		}
		map = "data/"+map;
		
		if (args.length > 4) {
			Display.STOP_AT_TIME = Integer.parseInt(args[4]);
		}

		//if (args.length > 5) {
			//Display.SHOW_BOTH = Display.ONE_TO_FOUR = Boolean.parseBoolean(args[5]);
		//}

		int segmentStdDev = 0;
		if (args.length > 5) {
			segmentStdDev = Integer.parseInt(args[6]);
		}


		long startTime = System.currentTimeMillis();
		
		GlobalConfiguration.getInstance().setString(SettingsKeys.VEHICLE_DENSITY, String.valueOf(rho));
		GlobalConfiguration.getInstance().setString(SettingsKeys.VEHICLE_TRANSMITTING, String.valueOf(1));
		GlobalConfiguration.getInstance().setString(SettingsKeys.VEHICLE_LICENSE_PLATE_RATIO, String.valueOf(1));
		GlobalConfiguration.getInstance().setString(SettingsKeys.VEHICLE_SMART_R, String.valueOf(ratio)); //route
		GlobalConfiguration.getInstance().setString(SettingsKeys.VEHICLE_SMART_S, String.valueOf(0)); //speed
		GlobalConfiguration.getInstance().setString(SettingsKeys.TRAFFIC_LIGHTS_TYPE, String.valueOf(LightControllerFactory.Type.RANDOMLY_FIXED));
		GlobalConfiguration.getInstance().setString(SettingsKeys.FIXED_PERIOD, String.valueOf(4));
		GlobalConfiguration.getInstance().setString(SettingsKeys.TRAFFIC_NAVIGATION_TYPE, String.valueOf(NavigatorFactory.Type.SIMPLE));
		GlobalConfiguration.getInstance().setString(SettingsKeys.TRAFFIC_STRATEGY_TYPE, String.valueOf(StrategyFactory.Type.NASCH_CO2));
		GlobalConfiguration.getInstance().setString(SettingsKeys.DATA_FILES_DIRECTORY, map);
		GlobalConfiguration.getInstance().setString(SettingsKeys.TRAFFIC_SEGMENTS_STD_DEV, String.valueOf(segmentStdDev));
		//GlobalConfiguration.getInstance().setString(SettingsKeys.DATA_FILES_DIRECTORY, "data/GEN13");
		
		for (int trial = 1; trial <= MAX_TRIALS; trial++) {
			City world = Loader.loadWorld();
			WorldMap worldMap =  Loader.loadWorldMap(world);
			TrafficManager trafficManager = new TrafficManager(world);
			if (trial == 1) {
				if (Display.SHOW_BOTH || Display.COMPLETE_STATISTICS) 
				{
					int N = trafficManager.getCrossingsCount();
					long N2 = (long)N * N * Display.STOP_AT_TIME * Display.STOP_AT_TIME;
					if (Display.ONE_TO_FOUR) {
						N2 = (long)Display.STOP_AT_TIME * Display.STOP_AT_TIME;
					}
					System.out.println("S_c_normal <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_c_smart <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_t_normal <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_t_smart <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_d_normal <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_d_smart <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_r_normal <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
					System.out.println("S_r_smart <- matrix(ncol=" + MAX_TRIALS + ", nrow="+(N2 / Display.STOP_AT_TIME)+")");
				} 
				if (Display.SHOW_BOTH || !Display.COMPLETE_STATISTICS) {
					System.out.println("tdcrNmS_CO2R <- matrix(ncol=" + MAX_TRIALS + ", nrow=39)");
					System.out.println("#tdcrNmS_CO2R: Time: normal - smart, Distance: ns, CO2: ns, Trips completed: ns, Time: normal - smart, "+
							"Distance: sn, CO2 sn, Trips completed: sn, Total comparisons, Smart count, "+
							"Normal count, number of vehicles removed, Total CO2 for N, Total CO2 for S, total trips N, total trips S, total time N, total time S, total distance N, total distance S, Results for 14, Normal 14 count, Smart 14 count"+
							(Display.ONE_TO_FOUR ? " [1-4] " : " ALL DIRS ")+" [max speed in cells: "+NaSchCO2.getMaxSpeedInCells()+"]");
				}
				System.out.format("#Simulation parameters: rho = %.4f, smart vehicle ratio = %.4f, cells: %d, vehicles: %d (first trial), map: %s, trials: %d, time: %d, sgmStdDev: %d\n", rho, ratio,
					world.getTotalNetworkLength(),
					trafficManager.getVehiclesCount(),
					map, MAX_TRIALS, Display.STOP_AT_TIME, segmentStdDev);
			}
			
	
			Renderer renderer = new Renderer(world, worldMap, trafficManager);
			
			
			JFrame frame = new JFrame("Traffic simulator");
			Display display = new Display(renderer, trafficManager);
			
			if (SHOW_GUI) {
				frame.getContentPane().add(display);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				frame.setLocation((dim.width - frame.getSize().width) / 2,
						(dim.height - frame.getSize().height) / 2);
				frame.setVisible(true);
				break;
			} else {
				display.doStats(trial);
			}
		}
		long timeDiff = System.currentTimeMillis() - startTime;
		System.out.println("#Time elapsed in ms: " +timeDiff);
	}
}


