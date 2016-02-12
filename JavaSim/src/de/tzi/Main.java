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
package de.tzi;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import de.tzi.geometry.City;
import de.tzi.graphics.Display;
import de.tzi.graphics.Renderer;
import de.tzi.graphics.WorldMap;
import de.tzi.io.Loader;
import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 * 
 */
public class Main {

	private static Logger logger = Logger.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		

		logger.info("Starting...");
		
		/* Bigger cities use a lot of memory, so in n case of 
		 * OutOfMemoryError: Java heap space please run with 
		 * -Xmx512m -Xms512m
		 */
		
		City world = Loader.loadWorld();
		WorldMap worldMap =  Loader.loadWorldMap(world);
		TrafficManager trafficManager = new TrafficManager(world);

		Renderer renderer = new Renderer(world, worldMap, trafficManager);
		
		
		JFrame frame = new JFrame("Traffic simulator");
		Display display = new Display(renderer, trafficManager);

		frame.getContentPane().add(display);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((dim.width - frame.getSize().width) / 2,
				(dim.height - frame.getSize().height) / 2);
		frame.setVisible(true);

		logger.info(world);
	}
}
