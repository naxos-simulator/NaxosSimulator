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
 * 2014-06-20
 */
package de.tzi.traffic.lights;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.traffic.Crossing;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.CrossingProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class RandomlyFixedLightController extends LightController {
	
	private static Logger logger = Logger.getLogger(RandomlyFixedLightController.class);
	

	final SortedMap<Integer, int[]> groupTimes = new TreeMap<Integer, int[]>();
	
	final static int MIN = GlobalConfiguration.getInstance().getInt(SettingsKeys.RANDOM_MIN);
	final static int MAX = GlobalConfiguration.getInstance().getInt(SettingsKeys.RANDOM_MAX);

	
	public RandomlyFixedLightController(TrafficManager trafficManager) {
		super(trafficManager);
		Random random = trafficManager.getLightsRandom();
		for (Crossing c : trafficManager.getCrossings()) {
			int [] times = new int[c.getGroups().length];
			groupTimes.put(c.getId(), times); 
			for (int i = 0; i < times.length; i++) {
				times[i] = random.nextInt(MAX - MIN + 1) + MIN;
			}
		}
		//Show the times
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<Integer, int[]>  me: groupTimes.entrySet()) {
			sb.append(me.getKey()).append(":").append(Arrays.toString(me.getValue())).append(" ");	
		}
		logger.debug(sb.toString());
	}
	
	public void tick(int time, Crossing crossing) {
		int counter = trafficManager.getPropertyManager().getCrossingProperty(CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId());
		int period = trafficManager.getPropertyManager().getCrossingProperty(CrossingProperty.RANDOM_CYCLE_LENGTH, crossing.getId());		
		if (++counter > period) {
			counter = 0;
			period = groupTimes.get(crossing.getId())[crossing.getCurrentGroup()];
			trafficManager.getPropertyManager().setCrossingProperty(CrossingProperty.RANDOM_CYCLE_LENGTH, crossing.getId(), period);
			crossing.changeLights();
		}
		trafficManager.getPropertyManager().setCrossingProperty(CrossingProperty.CURRENT_CYCLE_LENGTH, crossing.getId(), counter);
	}
}
