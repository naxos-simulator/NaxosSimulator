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
 * 2012-05-06
 */
package de.tzi.traffic;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.tzi.geometry.Passage;
import de.tzi.traffic.navigation.Verticle;

/**
 * @author Michal Markiewicz
 *
 */
public class Crossing implements Verticle {
	
	private static Logger logger = Logger.getLogger(Crossing.class);

	private final int id;
	
	protected final PassingPossibility[][] groups;
	protected final PassingPossibility[] allPossibilities;
	
	protected int currentGroup;
	
	final TrafficManager trafficManager;

	public void setCurrentGroup(int currentGroup) {
		this.currentGroup = currentGroup;
	}
	
	public int getCurrentGroup() {
		return currentGroup;
	}
	
	public void changeLights() {
		if (groups == null || groups.length == 0)
			return;
		currentGroup = (currentGroup + 1) % groups.length;
	}
	
	public final static int ALL_RED = -1;
	
	public Crossing(TrafficManager trafficManager, Collection<Passage> passages, Collection<Segment>allSegments) {
		this.id = trafficManager.nextCrossingId();
		this.trafficManager = trafficManager;
		groups = connectMatchingSegments(passages, allSegments);
		allPossibilities = allPossibilitiesToArray();
	}
	
	private PassingPossibility[] allPossibilitiesToArray() {
		LinkedList<PassingPossibility> listWithAllPossibilities = new LinkedList<PassingPossibility>();
		for (PassingPossibility[] passingPossibilites : groups) {
			for (PassingPossibility passingPossibility : passingPossibilites) {
				listWithAllPossibilities.add(passingPossibility);
			}
		}
		return listWithAllPossibilities.toArray(new PassingPossibility[0]);
	}

	protected PassingPossibility[][] connectMatchingSegments(Collection<Passage> passages, Collection<Segment> allSegments) {
		//Gets only interesting segments (listed in appropriate passages)
		HashSet<Integer> pois = new HashSet<Integer>();
		for (Passage passage : passages) {
			pois.add(new Integer(passage.getFrom()));
			pois.add(new Integer(passage.getBy()));
			pois.add(new Integer(passage.getTo()));
		}
		//Gets only interesting segments
		List<Segment> matchingSegments = new LinkedList<Segment>();
		for (Segment sgm: allSegments) {
			if (pois.contains(new Integer(sgm.getSection().getFrom())) ||
				pois.contains(new Integer(sgm.getSection().getTo()))) {
				matchingSegments.add(sgm);
			}
		}
		//Constructs passing possibilities by pairing input segment with output segment
		//Sets input and output crossings accordingly
		List<PassingPossibility> passingPossibilities = new LinkedList<PassingPossibility>();
		for (Passage passage : passages) {
			Segment input = findSegment(matchingSegments, passage.getFrom(), passage.getBy());
			Segment output = findSegment(matchingSegments, passage.getBy(), passage.getTo());
			if (input == null || output == null) {
				continue;
			}
			passingPossibilities.add(new PassingPossibility(trafficManager.nextPassingPossibilityId(), input, output));
			input.output = this;
			output.input = this;
		}
		//Groups of non mutually excluding passing possibilities
		List<List<PassingPossibility>> groupsLocal = new LinkedList<List<PassingPossibility>>(); 
		for (PassingPossibility p1 : passingPossibilities) {
			//take passing possibility and add it to a group where it doesn't collide with any other group
			//if such a group doesn't exist, then create a new one
			List<PassingPossibility> groupToAdd = null;
			for (List<PassingPossibility> group : groupsLocal) {
				boolean collides = false;
				for (PassingPossibility p2 : group) {
					if (p2.excludes(p1)) {
						collides = true;
						break;
					}
				}
				if (!collides) {
					groupToAdd = group;
					break;
				}
			}
			if (groupToAdd == null) {
				groupsLocal.add(groupToAdd = new LinkedList<PassingPossibility>());
			}
			groupToAdd.add(p1);
		}
		PassingPossibility[][] res = new PassingPossibility[groupsLocal.size()][];
		int idx = 0;
		for (List<PassingPossibility> pp : groupsLocal) {
			res[idx++] = pp.toArray(EMPTY_ARRAY);
		}
		return res;
	}
	
	private Segment findSegment(Collection<Segment> matchingSegments, int from, int to) {
		for (Segment sgm : matchingSegments) {
			if ((sgm.getSection().getFrom() == from) && (sgm.getSection().getTo() == to))
				return sgm;
		}
		logger.warn("Non-existing segment given in passage from: "+from+" to: "+to);
		return null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Crossing ").append(id).append("\n");
		int i = 0;
		for (PassingPossibility[] group : groups) {
			sb.append("Group: ");
			sb.append((char)('A'+i));
			sb.append(currentGroup == i ? " green" : " red");
			i++;
			sb.append('\n');
			for (PassingPossibility passingPossibility : group) {
				sb.append('\t');
				sb.append(' ');
				sb.append(passingPossibility.toString());
				sb.append('\n');
				delaysForPassingPossibilitiesToString(sb, passingPossibility);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}
	
	private void delaysForPassingPossibilitiesToString(StringBuffer sb, PassingPossibility pp) {
		if (pp.getInput().greenWaveDelayForPassingPossibility == null) {
			sb.append("no data available");
			return;
		}
		sb.append(Arrays.toString(pp.getInput().greenWaveDelayForPassingPossibility.get(pp)));
	}
	
	public PassingPossibility[][] getGroups() {
		return groups;
	}

	final static PassingPossibility[] EMPTY_ARRAY = new PassingPossibility[0];
	
	public PassingPossibility[] getGroupWithGreenLight() {
		if (currentGroup == ALL_RED || groups == null || groups.length == 0)
			return EMPTY_ARRAY;
		return groups[currentGroup];
	}
	
	public int getId() {
		return id;
	}
	
	public PassingPossibility[] getAllPossibilities() {
		return allPossibilities;
	}
}
