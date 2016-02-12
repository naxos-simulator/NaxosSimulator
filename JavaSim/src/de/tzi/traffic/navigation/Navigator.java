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
 * 2012-07-09
 */
package de.tzi.traffic.navigation;

import java.io.PrintStream;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.traffic.Crossing;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public abstract class Navigator {

	protected TrafficManager trafficManager;
	protected Graph staticGraph;
	protected Graph dynamicGraph;
	protected Graph hopGraph;

	protected Navigator(TrafficManager trafficManager, Graph staticGraph, Graph dynamicGraph, Graph hopGraph) {
		this.trafficManager = trafficManager;
		this.staticGraph = staticGraph;
		this.dynamicGraph = dynamicGraph;
		this.hopGraph = hopGraph;
	}
	
	public Navigator(TrafficManager trafficManager) {
		this.trafficManager = trafficManager;
		Edge[] edges = edges();
		Verticle[] verticles = verticles();
		staticGraph = new Graph(createStaticEdgeCostEstimator(), verticles, edges);
		dynamicGraph = new Graph(createDynamicEdgeCostEstimator(), verticles, edges);
		hopGraph = new Graph(createHopEdgeCostEstimator(), verticles, edges);
	}
	
	protected abstract Edge[] edges();

	protected abstract Verticle[] verticles();

	protected abstract EdgeCostEstimator createStaticEdgeCostEstimator();
	
	protected abstract EdgeCostEstimator createDynamicEdgeCostEstimator();
	
	protected EdgeCostEstimator createHopEdgeCostEstimator() {
		return new EdgeCostEstimator() {
			public double cost(Edge edge) {
				return 1;
			}
		};
	}

	protected abstract int randomTargetId(int src);
	
	public void randomizeVehiclesTargets() {
		int lastVehicleId = trafficManager.getVehiclesCount();
		for (int vehicleId = 1; vehicleId <= lastVehicleId; vehicleId++) {
			int src = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
			setVehicleDestination(vehicleId, randomTargetId(src));
		}
	}

	public abstract boolean destinationReached(int vehicleId, Segment leavingSegment);
	
	//Question asked by NaSch to find out where to go after crossing the nearest crossing
	public abstract PassingPossibility lookupForNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment);
	
	//This question is asked when the vehicle is at crossing	
	public abstract PassingPossibility findNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment, int time);

	//Question asked when the vehicle is in the middle of the segment (just after set up simulation environment) 
	public abstract PassingPossibility findFirstPassingPossibility(int vehicleId, Segment segment, int time);
	
	/**
	 * Returns destination id 
	 *  
	 * @param vehicleId
	 * @return
	 */
	protected int getVehicleDestination(int vehicleId) {
		return trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.DESTINATION, vehicleId);
	}

	/**
	 * Sets destination id for a given vehicle 
	 *  
	 * @param vehicleId
	 * @return
	 */
	protected void setVehicleDestination(int vehicleId, int dstId) {
		trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.DESTINATION, vehicleId, dstId);
	}

	protected int getVehicleSource(int vehicleId) {
		return trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SOURCE, vehicleId);
	}

	protected void setVehicleSource(int vehicleId, int srcId) {
		trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.SOURCE, vehicleId, srcId);
	}
	
	public abstract boolean initVehicleSource(int vehicleId, Segment segment);

	public void flipSourceDestination(int time, int vehicleId) {
		trafficManager.getPropertyManager().setVehicleProperty(
				VehicleProperty.DESTINATION_REACHED_LAST_TIME, vehicleId, time);
		int dstId = getVehicleDestination(vehicleId);
		int srcId = getVehicleSource(vehicleId);
		setVehicleDestination(vehicleId, srcId);
		setVehicleSource(vehicleId, dstId);
	}
	
	public PassingPossibility getVehiclePassingPossibility(int vehicleId) {
		int id = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
		if (id <= 0)
			return null;
		return trafficManager.getPassingPossibilitiesById()[id - 1];
	}
	
	public boolean isPassingPossibilityValid(PassingPossibility pp, Crossing crossing) {
		for (PassingPossibility p1 : crossing.getAllPossibilities()) {
			if (p1 == pp)
				return true;
		}
		return false;
	}

	protected void setVehiclePassingPossibility(int vehicleId, PassingPossibility pp, int time) {
		int prevId = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId);
		trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.PP_PREV_ID, vehicleId, prevId);
		trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId, pp.getId());
	}
	
	public void resetVehiclePassingPossibility(int vehicleId, Crossing crossing) {
		PassingPossibility[] group = crossing.getGroupWithGreenLight();
		PassingPossibility pp = group[trafficManager.getRandom().nextInt(group.length)];
		trafficManager.getPropertyManager().setVehicleProperty(VehicleProperty.PP_ENTER_ID, vehicleId, pp.getId());
	}
	
	protected abstract boolean isVehicleTargetReachable(Segment source, int vehicleId);
	
	public abstract int getPossibleDestinationCount();
	
	public int removeVehiclesWithUreachableOrTheSameDestinations() {
		int removed = 0;
		for (Segment segment : trafficManager.getSegments()) {
			int len = segment.getSectionLength();
			for (int pos = 0; pos < len; pos++) {
				int vehicleId = segment.getVehicleAtIndex(pos);
				if (vehicleId == 0)
					continue;
				if (!isVehicleTargetReachable(segment, vehicleId) || 
						!initVehicleSource(vehicleId, segment) //||
						//destinationReached(vehicleId, segment)
						) {
					initVehicleSource(vehicleId, segment);
					segment.destroyVehicleNowAtIndex(pos);
					trafficManager.getPropertyManager().removeAllProperties(vehicleId);
					removed++;
				}
			}
		}
		return removed;
	}
	
	public void printAllPaths(PrintStream ps) {
		ps.append("Static graph:\n");
		staticGraph.printAllPaths(ps);
		ps.append("Dynamic graph:\n");
		dynamicGraph.printAllPaths(ps);
	}
	
	private double cost(int fromCrossingId,  int toCrossingId, boolean isSmart, boolean timeTDistanceF) {
		Graph g = isSmart ? dynamicGraph : staticGraph;
		EdgeCostEstimator ece = timeTDistanceF ? dynamicGraph.edgeCostEstimator : staticGraph.edgeCostEstimator;
		return g.pathCost(fromCrossingId - 1, toCrossingId - 1, ece);
	}

	public double travelDistance(int fromCrossingId,  int toCrossingId, boolean isSmart) {
		return cost(fromCrossingId, toCrossingId, isSmart, false);
	}

	public double travelTime(int fromCrossingId,  int toCrossingId, boolean isSmart) {
		return cost(fromCrossingId, toCrossingId, isSmart, true);
	}
	
	protected Graph graphForVehicle(int vehicleId) {
		int inteligence = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId);
		return inteligence > 0 ? dynamicGraph : staticGraph;
//		return dynamicGraph;
	}
	
	public void update() {
		if (GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_SMART_R) == 0)
			return;
		dynamicGraph.update();
	}
}
