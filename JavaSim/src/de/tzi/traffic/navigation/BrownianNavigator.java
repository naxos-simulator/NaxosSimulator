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
 * 2012-07-12
 */
package de.tzi.traffic.navigation;

import de.tzi.traffic.Crossing;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 *
 */
public class BrownianNavigator extends Navigator {
	
	public BrownianNavigator(TrafficManager trafficManager) {
		super(trafficManager, null, null, null);
	}
	
	protected int randomTargetId(int src) {
		return trafficManager.getRandom().nextInt(trafficManager.getCrossingsCount()) + 1;
	}
	
	public boolean destinationReached(int vehicleId, Segment leavingSegment) {
		int dst = getVehicleDestination(vehicleId) ;
		return  dst == leavingSegment.getOutput().getId();
	}
	
	public boolean initVehicleSource(int vehicleId, Segment segment) {
		if (segment.getOutput() == null)
			return false;
		setVehicleSource(vehicleId, segment.getOutput().getId());
		return true;
	}
	
	protected Edge[] edges() {
		return trafficManager.getSegments().toArray(new Edge[0]);
	}
	
	protected Verticle[] verticles() {
		return trafficManager.getCrossings().toArray(new Verticle[0]);
	}
	
	protected EdgeCostEstimator createStaticEdgeCostEstimator() {
		return null;
	}

	protected EdgeCostEstimator createDynamicEdgeCostEstimator() {
		return null;
	}

	public PassingPossibility lookupForNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment) {
		return crossing.getAllPossibilities()[trafficManager.getRandom().nextInt(crossing.getAllPossibilities().length)];
	}
	
	public PassingPossibility findNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment, int time) {
		return lookupForNextPassingPossibility(vehicleId, crossing, segment);	
	}
	
	public PassingPossibility findFirstPassingPossibility(int vehicleId, Segment segment, int time) {
		if (segment.getOutput() == null)
			return null;
		return lookupForNextPassingPossibility(vehicleId, segment.getOutput(), segment);
	}

	protected boolean isVehicleTargetReachable(Segment source, int vehicleId) {
		if (source.getInput() == null)
			return false;
		return true;
	}
	
	public double travelDistance(int fromCrossingId,  int toCrossingId, boolean isSmart) {
		return Double.NaN;
	}

	public double travelTime(int fromCrossingId,  int toCrossingId, boolean isSmart) {
		return Double.NaN;
	}
	
	public void update() {
		return;
	}
}
