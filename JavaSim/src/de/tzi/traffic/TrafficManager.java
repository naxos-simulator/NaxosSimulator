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
 * 2012-05-02
 */
package de.tzi.traffic;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.geometry.City;
import de.tzi.geometry.Passage;
import de.tzi.geometry.Section;
import de.tzi.traffic.lights.LightController;
import de.tzi.traffic.lights.LightControllerFactory;
import de.tzi.traffic.navigation.Navigator;
import de.tzi.traffic.navigation.NavigatorFactory;
import de.tzi.traffic.properties.PropertyManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;
import de.tzi.traffic.strategy.Strategy;
import de.tzi.traffic.strategy.StrategyFactory;

/**
 * @author Michal Markiewicz
 *
 */
public class TrafficManager {
	
	private final static Logger logger = Logger.getLogger(TrafficManager.class); 
	
	protected final City world;
	protected final LightController lightController;
	protected final Strategy strategy;
	protected final PropertyManager propertyManager;
	protected final Navigator navigator;
	
	protected Collection<Segment> segments;
	protected Collection<Crossing> crossings; 
	protected final PassingPossibility[] passingPossibilitiesById;

	public TrafficManager(City world) {
		this(world,
			StrategyFactory.Type.valueOf(GlobalConfiguration.getInstance().getString(SettingsKeys.TRAFFIC_STRATEGY_TYPE)),
			LightControllerFactory.Type.valueOf(GlobalConfiguration.getInstance().getString(SettingsKeys.TRAFFIC_LIGHTS_TYPE)),
			NavigatorFactory.Type.valueOf(GlobalConfiguration.getInstance().getString(SettingsKeys.TRAFFIC_NAVIGATION_TYPE)));
	}

	public TrafficManager(City world, StrategyFactory.Type strategyType,
			LightControllerFactory.Type lightControllerType,
			NavigatorFactory.Type navigatorType,
			Collection<PropertyManager> historicalPropertyManagers,
			Random randomSegmentFillerGenerator) {
		random  = GlobalConfiguration.getInstance().getInt(SettingsKeys.TRAFFIC_RANDOM_SEED) == 0 ? 
					new Random() :
					new Random(GlobalConfiguration.getInstance().getInt(SettingsKeys.TRAFFIC_RANDOM_SEED));
		lightsRandom  = GlobalConfiguration.getInstance().getInt(SettingsKeys.LIGHTS_RANDOM_SEED) == 0 ? 
				new Random() :
				new Random(GlobalConfiguration.getInstance().getInt(SettingsKeys.LIGHTS_RANDOM_SEED));
		this.world = world;
		long length = createSegments();
		createCrossingsHavingSegments();
		passingPossibilitiesById = fillPassingPossibilitiesById();
		if (GlobalConfiguration.getInstance().getInt(SettingsKeys.TRAFFIC_SEGMENTS_STD_DEV) != 0) {
			randomizeSegments(new Random());
		} else {
			randomizeSegments(randomSegmentFillerGenerator);
		}
		propertyManager = new PropertyManager(this);
		if (historicalPropertyManagers != null)
			propertyManager.importHistoricalTravelTimes(historicalPropertyManagers);
		createDelayTunnelsForSegments();
		this.lightController = LightControllerFactory.createLightController(lightControllerType, this);
		this.strategy = StrategyFactory.createStrategy(strategyType, this);
		randomizeVehiclesSeeds();
		randomizeVehicleInteligence();
		randomizeVehicleTransmittingProperty();
		navigator = NavigatorFactory.createNavigator(navigatorType, this);
		initVehicleSources();
		
		navigator.randomizeVehiclesTargets();
		int removed = navigator.removeVehiclesWithUreachableOrTheSameDestinations();
		if (removed > 0) {
			logger.info(removed + " vehicle(s) with ureachable targets removed");
		}
		logger.info("Vehicles count: "+(lastVehicleId - removed));
		logger.info("Traffic network length: "+length);
	}
	
	private void initVehicleSources() {
		for (Segment sgm : segments) {
			int len = sgm.getSectionLength();
			for (int i = 0; i < len; i++) {
				int vehicleId = sgm.getVehicleAtIndex(i);
				if (vehicleId > 0) {
					navigator.initVehicleSource(vehicleId, sgm);
				}
			}
		}
	}

	public TrafficManager(City world, StrategyFactory.Type strategyType, LightControllerFactory.Type lightControllerType,
			NavigatorFactory.Type navigatorType) {
		this(world, strategyType, lightControllerType, navigatorType, null, null);
	}
	
	private void createDelayTunnelsForSegments() {
		//Segment and in which passing possibilities it occurs as input
		HashMap<Segment, List<PassingPossibility>> map = new HashMap<Segment, List<PassingPossibility>>();
		for (PassingPossibility pp : getPassingPossibilitiesById()) {
			List<PassingPossibility> list = map.get(pp.getInput());
			if (list == null) {
				list = new LinkedList<PassingPossibility>();
				map.put(pp.getInput(), list);
			}
			list.add(pp);
		}
		for (Map.Entry<Segment, List<PassingPossibility>> mapEntry : map.entrySet()) {
			Segment segment = mapEntry.getKey(); 
			segment.greenWaveDelayForPassingPossibility = new HashMap<PassingPossibility, byte[]>();
			for (PassingPossibility passingPossibility : mapEntry.getValue()) {
				segment.greenWaveDelayForPassingPossibility.put(passingPossibility, new byte[segment.getSectionLength()]);
			}
		}
	}

	private void randomizeVehicleInteligence() {
		float tresholdR = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_SMART_R);
		float tresholdS = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_SMART_S);
		for (int vehicleId = 1; vehicleId <= lastVehicleId; vehicleId++) {
			if (random.nextFloat() < tresholdR) {
				propertyManager.setVehicleProperty(VehicleProperty.SMART_R, vehicleId, 1);
			}
			if (random.nextFloat() < tresholdS) {
				propertyManager.setVehicleProperty(VehicleProperty.SMART_S, vehicleId, 1);
			}
		}
	}
	
	
	private void randomizeVehicleTransmittingProperty() {
		float treshold = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_TRANSMITTING);
		for (int vehicleId = 1; vehicleId <= lastVehicleId; vehicleId++) {
			if (random.nextFloat() < treshold) {
				propertyManager.setVehicleProperty(VehicleProperty.TRANSMITTING, vehicleId, 1);
			}
		}
	}

	//Having vehicles with their IDs generated, add vehicle properties
	private PassingPossibility[] fillPassingPossibilitiesById() {
		final PassingPossibility[] res = new PassingPossibility[getPassingPossibitiesCount()];
		for (Crossing crossing : crossings) {
			for (PassingPossibility pp : crossing.allPossibilities) {
				res[pp.getId() - 1] = pp;
			}
		}
		return res;
	}

	private void randomizeVehiclesSeeds() {
		int currentTime = (int)System.currentTimeMillis();
		for (int vehicleId = 1; vehicleId <= lastVehicleId; vehicleId++) {
			propertyManager.setVehicleProperty(VehicleProperty.SEED, vehicleId, vehicleId ^ currentTime);
		}
	}

	private void randomizeSegments(Random densityInSegment) {
		//For each segment get value that represents probability
		if (densityInSegment == null) {
			float fixedTreshold = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_DENSITY);
			for (Segment sgm : segments) {
				sgm.randomizeVehiclePositions(random, fixedTreshold);
			}			
		} else {
			//FIXME: Legitimate way:
			/*
			for (Segment sgm : segments) {
				float randomTreshold = densityInSegment.nextFloat();
				sgm.randomizeVehiclePositions(random, randomTreshold);
			}
			*/
			float fixedTreshold = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_DENSITY);
			boolean increase = true;
			for (Segment sgm : segments) {
				float variableTreshold;
				if (random.nextFloat() > 0.5) {
					variableTreshold = fixedTreshold * 2;
				} else {
					variableTreshold = fixedTreshold / 2;
				}
				sgm.randomizeVehiclePositions(random, variableTreshold);
				increase = !increase;
			}
		}
	}
	
	protected Object[] toArray(List<? extends Object> list) {
		if (list == null || list.size() == 0)
			return null;
		Class<? extends Object> clazz = list.get(0).getClass();
		return list.toArray((Object[])Array.newInstance(clazz, list.size()));
	}
	
	/**
	 * Creates crossings having segments already created
	 * Sets references to Crossing instances at the ends of segments
	 */
	protected void createCrossingsHavingSegments() {
		HashMap<Integer, LinkedList<Passage>> geometricCrossings = new HashMap<Integer, LinkedList<Passage>>();
		for (Passage crs : world.getCrossings()) {
			Integer key = new Integer(crs.getBy());
			LinkedList<Passage> value = geometricCrossings.get(key); 
			if (value == null) {
				geometricCrossings.put(key, value = new LinkedList<Passage>());
			}
			value.add(crs);
		}
		LinkedList<Crossing> crossingsList = new LinkedList<Crossing>();
		//for (Map.Entry<Integer, LinkedList<Passage>> entry : geometricCrossings.entrySet()) {
		for (LinkedList<Passage> crossingCollection : geometricCrossings.values()) {
			crossingsList.add(createCrossingInstance(crossingCollection, segments));
		}
		crossings = crossingsList;
	}

	protected long createSegments() {
		long len = 0;
		ArrayList<Segment> list = new ArrayList<Segment>();
		for (Section sgm : world.getSections()) {
			len += sgm.getLength();
			list.add(createSegmentInstance(sgm));
		}
		segments = list;
		return len;
	}
	
	public void printVehiclePositions(Writer w) throws IOException {
		for (Segment sgm : segments) {
			sgm.printVehiclePositions(time, w);
		}
	}
	
	protected int time = 0;
	
	/**
	 * Performs one tick
	 * 
	 * @return duration of a tick in nanoseconds  
	 */
	public long tick() {
		long startTime = System.nanoTime();
		for (Segment sgm : segments) {
			strategy.tick(time, sgm);
		}
		for (Crossing crs : crossings) {
			strategy.tick(time, crs);
		}
		if (time % strategy.getTimeResolution() == 0) {
			for (Crossing crs : crossings) {
				lightController.tick(time, crs);
			}
			if (time % GlobalConfiguration.getInstance().getInt(SettingsKeys.TRAFFIC_UPDATE_FREQUENCY) == 0) {
				logger.debug("Updating quickest routes at time: "+time);
				navigator.update();
			}
		}
		for (Segment sgm : segments) {
			sgm.tick();
		}
		/*
		int vehicleId = 1;
		int ppid = propertyManager.getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
		if (ppid > 0) {
			logger.error(propertyManager.vehiclePropertiesToString(vehicleId));
			logger.error("Curr @"+ ppid + ": "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.REF_PASSING_TIME, ppid)+" "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.FCD_ENDING_TIME, ppid) +" "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.FCD_ESTIMATION_TIME, ppid) +" "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.II_PASSING_TIME, ppid));
		}
		ppid = propertyManager.getVehicleProperty(VehicleProperty.PP_PREV_ID, vehicleId);
		if (ppid > 0) {
			logger.error("Prev @"+ ppid + ": "+ 
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.REF_PASSING_TIME, ppid)+" "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.FCD_ENDING_TIME, ppid) +" "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.FCD_ESTIMATION_TIME, ppid) +" "+
			propertyManager.getPassingPossibilityProperty(PassingPossibilityProperty.II_PASSING_TIME, ppid));
		}
		*/
		strategy.postProcessing(time);
		time = (time + 1) % Integer.MAX_VALUE;
		return System.nanoTime() - startTime;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nCrossings: \n");
		for (Crossing crs : crossings) {
			sb.append('\t');
			sb.append(crs);
			//sb.append('\n');
		}
		sb.append("\nSegments: \n");
		for (Segment sgm : segments) {
			sb.append('\t');
			sb.append(sgm);
			//sb.append('\n');
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public Collection<Crossing> getCrossings() {
		return crossings;
	}
	
	public Collection<Segment> getSegments() {
		return segments;
	}

	public void changeLights() {
		for (Crossing crs : crossings) {
			crs.changeLights();
		}
	}
	
	private Crossing createCrossingInstance(Collection<Passage> passages, Collection<Segment> segments) {
		return new Crossing(this, passages, segments);
	}

	public Segment createSegmentInstance(Section section) {
		return new Segment(this, section);
	}

	public PropertyManager getPropertyManager() {
		return propertyManager;
	}
	
	int lastSegmentId = 0;
	synchronized int nextSegmentId() {
		return ++lastSegmentId;
	}
	
	int lastVehicleId = 0;
	synchronized int nextVehicleId() {
		return ++lastVehicleId;
	}

	int lastCrossingId = 0;
	synchronized int nextCrossingId() {
		return ++lastCrossingId;
	}
	
	int lastPassingPossibilityId = 0;
	synchronized int nextPassingPossibilityId() {
		return ++lastPassingPossibilityId;
	}
	
	public int getVehiclesCount() {
		return lastVehicleId;
	}

	public int getCrossingsCount() {
		return lastCrossingId;
	}

	public int getSegmentsCount() {
		return lastSegmentId;
	}
	
	public int getPassingPossibitiesCount() {
		return lastPassingPossibilityId;
	}

	public PassingPossibility[] getPassingPossibilitiesById() {
		return passingPossibilitiesById;
	}
	
	public int getTimeResolution() {
		return strategy.getTimeResolution();
	}
	
	final Random random;
	final Random lightsRandom;
	
	public Random getRandom() {
		return random;
	}

	public Random getLightsRandom() {
		return lightsRandom;
	}

	public Navigator getNavigator() {
		return navigator;
	}
	
	public int getTime() {
		return time;
	}
	
	private int removedVehiclesCount;

	/**
	 * @return the removedVehiclesCount
	 */
	public int getRemovedVehiclesCount() {
		return removedVehiclesCount;
	}

	/**
	 * @param removedVehiclesCount the removedVehiclesCount to set
	 */
	public void setRemovedVehiclesCount(int removedVehiclesCount) {
		this.removedVehiclesCount = removedVehiclesCount;
	}
	
}

