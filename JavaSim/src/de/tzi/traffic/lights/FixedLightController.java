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
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.CrossingProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class FixedLightController extends LightController {

	public FixedLightController(TrafficManager trafficManager) {
		super(trafficManager);
	}
	
	final static int PERIOD = GlobalConfiguration.getInstance().getInt(SettingsKeys.FIXED_PERIOD);;
	
	//Unaligned (only once)
	//timeForOthers
	//timeForMe
	
	//Repeat:
	//timeForOthers
	//timeForMe
	private byte delayForCell(int distanceFromLights, int whichInOrder,
			int remainTimeCurrentLight, int period, int groupCount) {
		//So we have:
		//1..remainTimeCurrentLight PERIOD PERDIOD PERDIOD PERDIOD PERDIOD ourTurn!  
		//                           \______ x (groupCount - 1) _____/ 
		boolean hasGreen = hasGreen(distanceFromLights, whichInOrder, remainTimeCurrentLight, period, groupCount);
		return (byte)(hasGreen ? 0 : 
			Math.min(127, 
				//redDelay(distanceFromLights, whichInOrder, remainTimeCurrentLight, period, groupCount)
				(period - (period + distanceFromLights - remainTimeCurrentLight - 1) % period)));
	}
	
	private boolean hasGreen(int distanceFromLights, int whichInOrder,
			int remainTimeCurrentLight, int period, int groupCount) {
		return ((period + distanceFromLights - remainTimeCurrentLight - 1) / period) % groupCount == whichInOrder; 
	}
	
//	private int redDelay(int distanceFromLights, int whichInOrder,
//			int remainTimeCurrentLight, int period, int groupCount) {
//		return (period + distanceFromLights - remainTimeCurrentLight - 1); 
//	}
//	
	public void tick(int time, Crossing crossing) {
		int counter = trafficManager.getPropertyManager().getCrossingProperty(CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId());
		if (++counter > PERIOD) {
			counter = 0;
			crossing.changeLights();
		}
		int currentGroupIdx = crossing.getCurrentGroup();
		//All directions go in cycle
		PassingPossibility[][] allGroups = crossing.getGroups();
		for (int i = 0; i < allGroups.length; i++) {
			PassingPossibility[] group = allGroups[(i + currentGroupIdx) % allGroups.length];
			for (PassingPossibility passingPossibility : group) {
				byte[] delayTunnel = passingPossibility.getInput().getDelayForPassingPossibility(passingPossibility);
				for (int j = 0; j < delayTunnel.length; j++) {
					delayTunnel[j] = delayForCell(j, i, PERIOD - counter, PERIOD, allGroups.length);
				}
				//Logger.getLogger(FixedLightController.class).info(Arrays.toString(delayTunnel));
			}
		}
		trafficManager.getPropertyManager().setCrossingProperty(CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId(), counter);
	}
	
}
