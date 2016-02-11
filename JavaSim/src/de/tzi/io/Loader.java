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
package de.tzi.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.geometry.City;
import de.tzi.geometry.Passage;
import de.tzi.geometry.Section;
import de.tzi.graphics.RenderingSection;
import de.tzi.graphics.WorldMap;
import de.tzi.resources.LineListener;
import de.tzi.resources.LineReader;
import de.tzi.utils.TextUtils;

/**
 * @author Michal Markiewicz
 *
 */
public class Loader {
	
	private static Logger logger = Logger.getLogger(Loader.class);
	
	public static City loadWorld() {
		City world = null;
		String dir = GlobalConfiguration.getInstance().getString(SettingsKeys.DATA_FILES_DIRECTORY);
		if (!dir.endsWith(java.io.File.separator)) {
			dir += java.io.File.separator;   
		}		
		world = new City(
				loadCrossings(dir+GlobalConfiguration.getInstance().getString(SettingsKeys.DATA_FILE_CROSSINGS)), 
				loadSegments(dir+GlobalConfiguration.getInstance().getString(SettingsKeys.DATA_FILE_SEGMENTS)));
		return world;
	}
	
	public static WorldMap loadWorldMap(City world) {
		WorldMap worldMap = null;
		String dir = GlobalConfiguration.getInstance().getString(SettingsKeys.DATA_FILES_DIRECTORY);
		if (!dir.endsWith(java.io.File.separator)) {
			dir += java.io.File.separator;
		}
		worldMap = new WorldMap(
				loadRenderingSections(dir+GlobalConfiguration.getInstance().getString(SettingsKeys.DATA_FILE_MAP_SEGMENTS), world));
		return worldMap;
	}

	private static Map<RenderingSection, RenderingSection> loadRenderingSections(final String path, City world) {
		final Map<RenderingSection, RenderingSection> res = new HashMap<RenderingSection, RenderingSection>();
		for (Section section : world.getSections()) {
			//x and y values interleaved
			RenderingSection gs = new RenderingSection(section); 
			res.put(gs, gs);
		}
		try {
			new LineReader(new LineListener() {
				int lineNo; 
				public void acceptLine(String line) {
					lineNo++;
					String[] arr = TextUtils.expandString(line, TextUtils.TAB);
					try {
						if (arr.length < 5) {
							logger.warn("File: "+path+" line: "+lineNo+": Some fields are missing, skipping");
							return;
						}
						int i = 0;
						int from = Integer.parseInt(arr[i++]);
						int to = Integer.parseInt(arr[i++]);
						//Counts from 1
						//FIXED: Count from 0 (no longer from 1)
						int cellNo = Integer.parseInt(arr[i++]);
						RenderingSection rs = res.get(new RenderingSection(from, to));
						if (rs == null) {
							logger.warn("File: "+path+" line: "+lineNo+" road section from: "+from+" to: "+to+" doesn't exist!");
							return;
						}
						int cellIdx = 2 * cellNo; 
						if (cellIdx + 1 >= rs.getMapPositionsArray().length) {
							logger.warn("File: " + path + " line: " + lineNo
									+ ": cellNo: " + cellNo
									+ " exceeds length of the road section: "
									+ rs.getMapPositionsArray().length / 2);
						}
						double x = Double.parseDouble(arr[i++]);
						double y = Double.parseDouble(arr[i++]);
						rs.setMapX(cellNo, x);
						rs.setMapY(cellNo, y);
					} catch (Exception e) {
						logger.warn("File: "+path+" line: "+lineNo+": \""+line+"\" "+e);
					}
				}
			}).read(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		return res;
	}
	
	private static Section[] loadSegments(final String path) {
		
		final LinkedList<Section> list = new LinkedList<Section>();		
		try {
			new LineReader(new LineListener() {
				int lineNo; 
				public void acceptLine(String line) {
					lineNo++;
					String[] arr = TextUtils.expandString(line, TextUtils.TAB);
					try {
						int length = Integer.parseInt(arr[2]);
						if (length < 2) {
							length = 2;
							logger.warn("File: "+path+" line: "+lineNo+": Segment length < 2m!, setting to: "+length);
						}
						list.add(new Section(Integer.parseInt(arr[0]), Integer
							.parseInt(arr[1]), length, Float.parseFloat(arr[3])));

					} catch (Exception e) {
						if (arr.length < 4) {
							logger.warn("File: "+path+" line: "+lineNo+": \""+line+"\" some fields are missing");
						} else {
							logger.warn("File: "+path+" line: "+lineNo+": \""+line+"\" "+e);
						}
					}
				}
			}).read(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		return list.toArray(new Section[0]);
	}

	private static Passage[] loadCrossings(final String path) {
		final LinkedList<Passage> list = new LinkedList<Passage>();		
		try {
			new LineReader(new LineListener() {
				int lineNo; 
				public void acceptLine(String line) {
					lineNo++;
					String[] arr = TextUtils.expandString(line, TextUtils.TAB);
					try {
						list.add(new Passage(Integer.parseInt(arr[0]), Integer
							.parseInt(arr[1]), Integer.parseInt(arr[2])));
					} catch (Exception e) {
						if (arr.length < 3) {
							logger.warn("File: "+path+" line: "+lineNo+": \""+line+"\" some fields are missing");
						} else {
							logger.warn("File: "+path+" line: "+lineNo+": \""+line+"\" "+e);
						}
					}
				}
			}).read(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		return list.toArray(new Passage[0]);
	}

}
