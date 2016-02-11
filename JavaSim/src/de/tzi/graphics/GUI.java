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

 * 2012-05-25
 */
package de.tzi.graphics;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.geometry.City;
import de.tzi.io.Loader;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.lights.LightControllerFactory;
import de.tzi.traffic.navigation.NavigatorFactory;
import de.tzi.traffic.strategy.StrategyFactory;

/**
 * @author Michal Markiewicz
 *
 */
public class GUI {

	private static Logger logger = Logger.getLogger(GUI.class); 
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		logger.debug("Loading data");
		City world;
		WorldMap worldMap;
		
		String persistenceFile = GlobalConfiguration.getInstance().getString(SettingsKeys.PERSISTENCE_FILE);
		if (!new File(persistenceFile).exists()) {
			logger.debug("No persistence file found, reading csv files");
			world = Loader.loadWorld();
			worldMap = Loader.loadWorldMap(world);
			logger.debug("Serializing to: "+persistenceFile);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(persistenceFile));
			oos.writeObject(world);
			oos.writeObject(worldMap);
			logger.debug("Loaded from csv files");
		} else {
			logger.debug("Deserializing from "+persistenceFile);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(persistenceFile));
			world = (City)ois.readObject();
			worldMap = (WorldMap)ois.readObject();
			logger.debug("Done");
		}
		logger.debug("Data loaded");
		TrafficManager trafficManager = new TrafficManager(world,
				StrategyFactory.Type.RULE184,
				LightControllerFactory.Type.RANDOM,
				NavigatorFactory.Type.STANDARD);

		Renderer renderer = new Renderer(world, worldMap, trafficManager);
		JFrame frame = new JFrame("Traffic simulator");
		frame.getContentPane().add(new Display(renderer, trafficManager));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((dim.width-frame.getSize().width)/2, (dim.height-frame.getSize().height)/2);
		frame.setVisible(true);
	}
}
