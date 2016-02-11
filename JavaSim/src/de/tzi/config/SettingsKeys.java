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
 * 2008-12-21
 */
package de.tzi.config;

/**
 * 
 * @author Michal Markiewicz
 *
 */
public class SettingsKeys {

	public final static String DATA_FILES_DIRECTORY = "data.files.directory";
	public final static String DATA_FILE_CROSSINGS = "data.files.crossings";
	public final static String DATA_FILE_SEGMENTS = "data.files.segments";
	public final static String DATA_FILE_OUTPUT = "data.files.ouptput";
	public final static String VEHICLE_DENSITY = "vehicle.density";
	public final static String VEHICLE_SMART_R = "vehicle.smart.r";
	public final static String VEHICLE_SMART_S = "vehicle.smart.s";
	public final static String VEHICLE_TRANSMITTING = "vehicle.transmitting";
	public final static String VEHICLE_LICENSE_PLATE_RATIO = "vehicle.licensePlate.ratio";
	public final static String DATA_FILE_MAP_SEGMENTS = "data.files.map.segments";
	public final static String PERSISTENCE_FILE = "persistence.file";
	public final static String SOTL_D = "lights.sotl.d";
	public final static String SOTL_R = "lights.sotl.r";
	public final static String SOTL_E = "lights.sotl.e";
	public final static String SOTL_U = "lights.sotl.u";
	public final static String SOTL_M = "lights.sotl.m";
	public final static String SOTL_THRESHOLD = "lights.sotl.threshold";
	public final static String RANDOM_MIN = "lights.random.min";
	public final static String RANDOM_MAX = "lights.random.max";
	public final static String LIGHTS_RANDOM_SEED = "lights.random.seed";
	public final static String FIXED_PERIOD = "lights.fixed.period";
	public final static String TRAFFIC_RANDOM_SEED = "traffic.random.seed";
	public final static String TRAFFIC_UPDATE_FREQUENCY = "traffic.update.frequency";
	public final static String TRAFFIC_SEGMENTS_STD_DEV ="traffic.segments.stddev";
	public final static String TRAFFIC_DEADLOCK_WAIT = "traffic.deadlock.wait";
	public final static String TRAFFIC_LIGHTS_TYPE = "traffic.lights.type";
	public final static String TRAFFIC_STRATEGY_TYPE = "traffic.strategy.type";
	public final static String TRAFFIC_NAVIGATION_TYPE = "traffic.navigation.type";
	public final static String TRAFFIC_INFINITE_TRIP = "traffic.infiniteTrip";
	public final static String RANDOM_TABLES_DIR = "random.tables.dir";
	public final static String WINDOW_SIZE_WIDTH = "window.size.width";
	public final static String WINDOW_SIZE_HEIGHT = "window.size.height";
	public final static String WINDOW_CELL_SIZE = "window.cell.size";
	public final static String WINDOW_ZOOM = "window.zoom";
	public final static String WINDOW_FPS = "window.fps";	
	public final static String DB_URL = "db.url";
	public final static String DB_USER = "db.user";
	public final static String DB_PASS = "db.pass";
	public final static String DB_DRIVER = "db.driver";
	public final static String INTERACTIVE_MODE = "interactive.mode";
}
