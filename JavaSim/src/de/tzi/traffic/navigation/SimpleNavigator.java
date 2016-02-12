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
import de.tzi.traffic.properties.PropertyManager.SegmentProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class SimpleNavigator extends Navigator {
	
	public SimpleNavigator(TrafficManager trafficManager) {
		super(trafficManager);
	}
	
	public int getPossibleDestinationCount() {
		return trafficManager.getCrossingsCount();
	}
	
	protected int randomTargetId(int src) {
		//FIXME: MINIMAL DISTANCE > 2
		//FIXME: NOT THE SAME VERTEX
		int proposal;
		do {
			proposal = trafficManager.getRandom().nextInt(trafficManager.getCrossingsCount()) + 1;
		} while (proposal == src); //hopGraph.distTo[src - 1][proposal - 1] <= 2); 
		
		//at least with distance = 2
		return proposal;
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
		return new EdgeCostEstimator() {
			public double cost(Edge edge) {
				return ((Segment) edge).getSectionLength();
			}
		};
	}

	protected EdgeCostEstimator createDynamicEdgeCostEstimator() {
		return new EdgeCostEstimator() {
			public double cost(Edge edge) {
				Segment segment = (Segment) edge;
				int pt = trafficManager.getPropertyManager().getSegmentProperty(SegmentProperty.II_PASSING_TIME, segment.getId());
				if (pt == 0) 
					pt = trafficManager.getPropertyManager().getSegmentProperty(SegmentProperty.HISTORICAL_TIME, segment.getId());
				if (pt == 0)
					pt = segment.getSectionLength();
				return pt;
			}
		};
	}

	public PassingPossibility lookupForNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment) {
		Segment nextSegment = (Segment) graphForVehicle(vehicleId).whereToGo(
				segment.getOutput().getId(), getVehicleDestination(vehicleId));
		if (nextSegment == null)
			return null;
		PassingPossibility ppToGo = null;
		for (PassingPossibility pp : crossing.getAllPossibilities()) {
			if (pp.getOutput() == nextSegment && pp.getInput() == segment) {
				ppToGo = pp;
				break;
			}
		}
		return ppToGo;
	}
	
	public PassingPossibility findNextPassingPossibility(int vehicleId, Crossing crossing, Segment segment, int time) {
		PassingPossibility pp = lookupForNextPassingPossibility(vehicleId, crossing, segment);
		if (pp != null)
			setVehiclePassingPossibility(vehicleId, pp, time);
		return pp;
	}
	
	public PassingPossibility findFirstPassingPossibility(int vehicleId, Segment segment, int time) {
		if (segment.getOutput() == null)
			return null;
		return findNextPassingPossibility(vehicleId, segment.getOutput(), segment, 0);
	}

	protected boolean isVehicleTargetReachable(Segment source, int vehicleId) {
		if (source.getInput() == null)
			return false;
		int srcCrossingId = source.getInput().getId();
		int dstCrossingId = getVehicleDestination(vehicleId);
		return graphForVehicle(vehicleId).hasPathById(srcCrossingId, dstCrossingId);
	}
}
