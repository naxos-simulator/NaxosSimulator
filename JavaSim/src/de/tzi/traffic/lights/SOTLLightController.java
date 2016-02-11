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
 * 2012-06-01
 */
package de.tzi.traffic.lights;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.traffic.Crossing;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.CrossingProperty;
import de.tzi.traffic.properties.PropertyManager.PassingPossibilityProperty;

/**
 * @author Michal Markiewicz
 * 
 */
public class SOTLLightController extends LightController {

	public SOTLLightController(TrafficManager trafficManager) {
		super(trafficManager);
	}

	final static int D = GlobalConfiguration.getInstance().getInt(SettingsKeys.SOTL_D);
	final static int R = GlobalConfiguration.getInstance().getInt(SettingsKeys.SOTL_R);
	final static int E = GlobalConfiguration.getInstance().getInt(SettingsKeys.SOTL_E);
	// Minimum green time period
	final static int U = GlobalConfiguration.getInstance().getInt(SettingsKeys.SOTL_U);
	final static int M = GlobalConfiguration.getInstance().getInt(SettingsKeys.SOTL_M);
	final static int THRESHOLD = GlobalConfiguration.getInstance().getInt(SettingsKeys.SOTL_THRESHOLD);

	private int addVehicles(PassingPossibility pp, PassingPossibilityProperty ppp, int count) {
		count += trafficManager.getPropertyManager().getPassingPossibilityProperty(ppp, pp.getId());
		trafficManager.getPropertyManager().setPassingPossibilityProperty(ppp, pp.getId(), count);
		return count;
	}

	private void clearVehicles(PassingPossibility pp, PassingPossibilityProperty ppp) {
		trafficManager.getPropertyManager().setPassingPossibilityProperty(ppp, pp.getId(), 0);
	}

	
	public void tick(int time, Crossing crossing) {
		
		int d[] = new int [crossing.getGroups().length];
		int r[] = new int [crossing.getGroups().length];
		int e[] = new int [crossing.getGroups().length];
		
		/**
		 * 1. On every tick, add to a counter the number of vehicles approaching
		 * or waiting at a red light within distance d. When this counter
		 * exceeds a threshold n, switch the light. (Whenever the light
		 * switches, reset the counter to 0.)
		 */
		int groupIdx = 0;
		int groupWithTheGreatestCounter = groupIdx;
		for (PassingPossibility[] group : crossing.getGroups()) {
			for (PassingPossibility pp : group) {
				Segment input = pp.getInput();
				Segment output = pp.getOutput();
				int length = output.getSectionLength();
				d[groupIdx] += addVehicles(pp, PassingPossibilityProperty.SOTL_D,
						countVehicles(output, length - D, length));
				r[groupIdx] += addVehicles(pp, PassingPossibilityProperty.SOTL_R, 
						countVehicles(output, length - R, length));
				e[groupIdx] += addVehicles(pp, PassingPossibilityProperty.SOTL_E, 
						countVehicles(input, 0, E));
			}
			if (d[groupWithTheGreatestCounter] < d[groupIdx])
				groupWithTheGreatestCounter = groupIdx;
			groupIdx++;
		}
		/**
		 * 2. Lights must remain green for a minimum time u.
		 */
		int greenLightTime = trafficManager.getPropertyManager()
				.getCrossingProperty(CrossingProperty.CURRENT_CYCLE_LENGTH,
						crossing.getId());
		if (greenLightTime++ < U) {
			trafficManager.getPropertyManager().setCrossingProperty(
					CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId(),
					greenLightTime);
			return;
		}
		int currentGroup = crossing.getCurrentGroup();
		/**
		 * 3. If a few vehicles (m or fewer, but more than zero) are left to
		 * cross a green light at a short distance r, do not switch the light.
		 */
		if (currentGroup != Crossing.ALL_RED && r[currentGroup] > 0
				&& r[currentGroup] < M) {
			trafficManager.getPropertyManager().setCrossingProperty(
					CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId(),
					greenLightTime);
			return;
		}
		/**
		 * 4. If no vehicle is approaching a green light within a distance d,
		 * and at least one vehicle is approaching the red light within a
		 * distance d, then switch the light.
		 */
		int nextGroup = Crossing.ALL_RED;
		if (currentGroup != Crossing.ALL_RED && d[currentGroup] == 0) {
			for (int i = 0; i < d.length; i++) {
				if (nextGroup == Crossing.ALL_RED || d[i] > d[nextGroup])
					nextGroup = i;
			}
		}
		/**
		 * 5. If there is a vehicle stopped on the road in a short distance e
		 * beyond a green traffic light, then switch the light.
		 */
		// ...
		/**
		 * 6. If there are vehicles stopped on both directions at a short
		 * distance e beyond the intersection, then switch both lights to red.
		 * Once one of the directions is free, restore the green light in that
		 * direction.
		 */
		//...
		if (nextGroup == Crossing.ALL_RED) {
			if (d[groupWithTheGreatestCounter] > THRESHOLD) {
				d[groupWithTheGreatestCounter] = 0;
				nextGroup = groupWithTheGreatestCounter;
			}
		}
		if (nextGroup != Crossing.ALL_RED) {
			for (PassingPossibility pp : crossing.getGroups()[nextGroup]) {
				clearVehicles(pp, PassingPossibilityProperty.SOTL_D);
				clearVehicles(pp, PassingPossibilityProperty.SOTL_R);
				clearVehicles(pp, PassingPossibilityProperty.SOTL_E);
			}
			greenLightTime = 0;
			trafficManager.getPropertyManager().setCrossingProperty(
					CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId(),
					greenLightTime);
		}
		crossing.setCurrentGroup(nextGroup);
	}

	private int countVehicles(Segment sgm, int from, int to) {
		int length = sgm.getSectionLength();
		from = Math.min(Math.max(0, from), length - 1);
		to = Math.min(Math.max(0, to), length - 1);
		int res = 0;
		for (int i = from; i <= to; i++) {
			res += sgm.isVehicleThere(i) ? 1 : 0;
		}
		return res;
	}
}
