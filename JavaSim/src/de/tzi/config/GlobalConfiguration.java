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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.tzi.utils.Base64Decoder;
import de.tzi.utils.Base64FormatException;

/**
 * @author Michal Markiewicz
 * 
 */
public class GlobalConfiguration {

	private static GlobalConfiguration instance;
	
	private static Logger logger = Logger.getLogger(GlobalConfiguration.class);

	public static GlobalConfiguration getInstance() {
		if (instance == null)
			instance = new GlobalConfiguration();
		return instance;
	}
	
	final String CONFIG_FILE = "simulator.properties";
	Properties settings = new Properties();

	private GlobalConfiguration() {
		try {
			settings.load(getClass().getResourceAsStream("/"+CONFIG_FILE));
		} catch (IOException e) {
			logger.error("Cannot load default settings!", e);
		}
		try {
			logger.debug("Reading configuration file "+CONFIG_FILE);
			settings.load(new FileInputStream(CONFIG_FILE));
		} catch (IOException e) {
			logger.warn("Cannot load configuration file, using defaults");
		}
		try {
			logger.debug("Trying to read configuation file for dataset in "+getString(SettingsKeys.DATA_FILES_DIRECTORY));
			settings.load(new FileInputStream(concatenateDirWithFileName(
					getString(SettingsKeys.DATA_FILES_DIRECTORY), CONFIG_FILE)));
			logger.debug("Loaded successfully");
		} catch (IOException e) {
			logger.debug("Not found, doesn't matter...");
		}
	}
	
	public void setString(String key, String value) {
		settings.setProperty(key, value);
	}
	
	public String getString(String key) {
		return settings.getProperty(key).trim();
	}
	
	public String getDecodedString(String key) {
		try {
			return new Base64Decoder(getString(key)).processString();
		} catch (Base64FormatException e) {
			logger.error(e);
		}
		return null;
	}

	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
	
	public float getFloat(String key) {
		return Float.parseFloat(getString(key));
	}
	
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key));
	}
	
	public static String concatenateDirWithFileName(String dir, String file) {
		return dir + (dir.endsWith(File.separator) ? "" : File.separator) + file;
	}
	
	public String toString() {
		return settings == null ? "null" : settings.toString();
	}
}
