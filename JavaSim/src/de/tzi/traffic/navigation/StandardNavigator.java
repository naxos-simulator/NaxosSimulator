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

import de.tzi.traffic.Crossing;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.PassingPossibilityProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class StandardNavigator extends Navigator {

	public StandardNavigator(TrafficManager trafficManager) {
		super(trafficManager);
	}
	
	protected int randomTargetId(int src) {
		return trafficManager.getRandom().nextInt(trafficManager.getSegmentsCount()) + 1;
	}
	
	public boolean destinationReached(int vehicleId, Segment leavingSegment) {
		return getVehicleDestination(vehicleId) == leavingSegment.getId();
	}
	
	public boolean initVehicleSource(int vehicleId, Segment segment) {
		setVehicleSource(vehicleId, segment.getId());
		return true;
	}
	
	protected Edge[] edges() {
		Edge[] edges = new Edge[trafficManager.getPassingPossibitiesCount()];
		System.arraycopy(trafficManager.getPassingPossibilitiesById(), 0, edges, 0, edges.length);
		return edges;
	}
	
	protected Verticle[] verticles() {
		return trafficManager.getSegments().toArray(new Verticle[0]);
	}
	
	protected EdgeCostEstimator createStaticEdgeCostEstimator() {
		return new EdgeCostEstimator() {
			public double cost(Edge edge) {
				return ((PassingPossibility) edge).getInput().getSectionLength();
			}
		};
	}
	
	protected EdgeCostEstimator createDynamicEdgeCostEstimator() {
		return new EdgeCostEstimator() {
			public double cost(Edge edge) {
				PassingPossibility pp = (PassingPossibility) edge;
				int pt = trafficManager.getPropertyManager().getPassingPossibilityProperty(PassingPossibilityProperty.II_PASSING_TIME, pp.getId());
				if (pt == 0)
					pt = trafficManager.getPropertyManager().getPassingPossibilityProperty(PassingPossibilityProperty.HISTORICAL_TIME, pp.getId());
				if (pt == 0)
					pt = pp.getInput().getSectionLength();
				return pt; 
			}
		};
	}
	
	public PassingPossibility lookupForNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment) {
		return (PassingPossibility)graphForVehicle(vehicleId).whereToGo(segment.getId(), getVehicleDestination(vehicleId));
	}
	
	public PassingPossibility findNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment, int time) {
		PassingPossibility pp = lookupForNextPassingPossibility(vehicleId, crossing, segment);
		if (pp != null)
			setVehiclePassingPossibility(vehicleId, pp, time);
		return pp;
	}
	
	public PassingPossibility findFirstPassingPossibility(int vehicleId, Segment segment, int time) {
		return findNextPassingPossibility(vehicleId, null, segment, 0);
	}
	
	protected boolean isVehicleTargetReachable(Segment source, int vehicleId) {
		int srcSegmentId = source.getId();
		int dstSegmentId = getVehicleDestination(vehicleId);
		return graphForVehicle(vehicleId).hasPathById(srcSegmentId, dstSegmentId);
	}

}
