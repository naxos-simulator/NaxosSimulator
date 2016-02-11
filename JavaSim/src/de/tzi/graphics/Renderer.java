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

import java.awt.image.MemoryImageSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tzi.geometry.City;
import de.tzi.traffic.PassingPossibility;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;
import de.tzi.traffic.properties.PropertyManager.VehicleProperty;

/**
 * @author Michal Markiewicz
 *
 */
public class Renderer {
	
	private static Logger logger = Logger.getLogger(Renderer.class);
	
	City world;
	WorldMap worldMap;
	Map<Segment, RenderingSection> segmentsToRender;
	
	int[] argbBuffer;
	Segment[] screenCoordinatesToSegment;
	int[] screenCoordinatesToCellNo;
	
	MemoryImageSource memoryImageSource;
	
	final DisplaySettings displaySettings;
	final TrafficManager trafficManager;
	
	public Renderer(City world, WorldMap worldMap, TrafficManager trafficManager) {
		this(world, worldMap, trafficManager, new DisplaySettings());
	}

	public Renderer(City world, WorldMap worldMap, TrafficManager trafficManager, DisplaySettings displaySettings) {
		this.world = world;
		this.worldMap = worldMap;
		this.trafficManager = trafficManager;
		this.segmentsToRender = matchSegmentsToRender(worldMap.getSectionMapping(), trafficManager.getSegments());
		this.displaySettings = displaySettings;
		argbBuffer = new int[displaySettings.width * displaySettings.height];
		screenCoordinatesToSegment = new Segment[argbBuffer.length];
		screenCoordinatesToCellNo = new int[argbBuffer.length];
		adjustScreenCoordinates();
		clearBackground();
		memoryImageSource = new MemoryImageSource(displaySettings.width, displaySettings.height, argbBuffer, 0, displaySettings.width);
		memoryImageSource.setAnimated(true);
		memoryImageSource.setFullBufferUpdates(false);
	}
		
	void adjustScreenCoordinates() {
		int i=0;
		final double minX = worldMap.boundaries[i++];
		final double minY = worldMap.boundaries[i++];
		final double maxX = worldMap.boundaries[i++];
		final double maxY = worldMap.boundaries[i++];
		
		final double mapW = Math.abs(maxX - minX);
		final double mapH = Math.abs(maxY - minY);
		
		final double imgW = displaySettings.width * displaySettings.zoom;// * displaySettings.pixelSize;
		final double imgH = displaySettings.height * displaySettings.zoom;// * displaySettings.pixelSize;
		
		for (Map.Entry<RenderingSection, RenderingSection> entry : worldMap.sectionMapping.entrySet()) {
			RenderingSection rs = entry.getKey();
			int max = rs.getSection().getLength();
			for (int pos = 0; pos < max; pos++) {
				double worldX = rs.getMapX(pos);
				double worldY = rs.getMapY(pos);
				int x = (int)Math.round(((worldX - minX) / mapW) * imgW);
				int y = (int)Math.round(imgH - ((worldY - minY) / mapH) * imgH);
				x += displaySettings.shiftX;
				y += displaySettings.shiftY;
				if (x < 0 || x > displaySettings.width || y < 0 || y > displaySettings.height) {
					rs.setScreenX(pos, -1);
					rs.setScreenY(pos, -1);
					continue;
				}
				rs.setScreenX(pos, x);
				rs.setScreenY(pos, y);
			}
		}
	}

	private Map<Segment, RenderingSection> matchSegmentsToRender(
			Map<RenderingSection, RenderingSection> sectionMapping,
			Collection<? extends Segment> segments) {
		Map<Segment, RenderingSection> res = new HashMap<Segment, RenderingSection>();
		for (Segment abstractSegment : segments) {
			RenderingSection rsLookup = new RenderingSection(abstractSegment.getSection().getFrom(), abstractSegment.getSection().getTo());
			RenderingSection rs = sectionMapping.get(rsLookup);
			if (rs == null) {
				logger.warn("No rendering section found for geometric section: "+abstractSegment.getSection());
				continue;
			} else if (rs.getSection() != abstractSegment.getSection()) {
				logger.warn("Rendering section and abstract sections mismatch! "+rs+" "+abstractSegment);
				continue;
			}
			res.put(abstractSegment, rs);
		}
		return res;
	}

	final static int UNKNOWN_CELL = -1;
	
	void clearBackground() {
		int r, g, b, a;
		a = 255;
		r = 255;
		g = 255;
		b = 255;
		for (int i = 0; i < argbBuffer.length; i++) {
			argbBuffer[i] = (a << 24) + (r << 16) + (g << 8) + (b);
			screenCoordinatesToSegment[i] = null;
			screenCoordinatesToCellNo[i] = UNKNOWN_CELL;
		}
	}
	
	boolean render() {
		boolean atLeastOneVehicle = false;
		int a = 250;
		double zoom = displaySettings.pixelSize;
		//logger.info(zoom+" "+displaySettings.cellsPerPixel);
		for (Map.Entry<Segment, RenderingSection> entry : segmentsToRender.entrySet()) {
			int max = entry.getKey().getSectionLength();
//			byte[] delays = entry.getKey().getDelayForPassingPossibility(entry.getKey().getOutput().getAllPossibilities()[0]);
			for (int i = 0; i < max; i++) {
				int vehicleId = entry.getKey().getVehicleAtIndex(i);
				int x = entry.getValue().getScreenX(i);
				int y = entry.getValue().getScreenY(i);
				int r, g, b;
				if (vehicleId == 1) {
					r = 192;
					g = 0;
					b = 0;
					atLeastOneVehicle = true;
				} else if (vehicleId > 1) {
					if (trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.SMART_R, vehicleId) > 0) {
						r = 0;
						g = 192;
						b = 0;
					} else {
						r = 0;
						g = 0;
						b = 192;
					}
					atLeastOneVehicle = true;
				} else {
//					double maxDelay = 100;
//					double ratio = delays == null ? 1 : (delays[i]/maxDelay);
					int grey = 200;//233//180
					int ratio = 1;
					r = (int)Math.round(ratio * grey);
					g = (int)Math.round(ratio * grey);
					b = (int)Math.round(ratio * grey);
					
					int ppId = trafficManager.getPropertyManager().getVehicleProperty(VehicleProperty.PP_ENTER_ID, 1);
					if (ppId > 0) {
						PassingPossibility pp = trafficManager.getPassingPossibilitiesById()[ppId - 1];
						if (pp.getInput() == entry.getKey() || pp.getOutput() == entry.getKey()) {
							//r = g = b = 128;
							//SHOWS TRACK OF THE VEHICLE WITH ID = 1
						}
					}
					
				}
				if (zoom == 1) {
					int idx = y * displaySettings.width + x;
					if (idx > 0 && idx < argbBuffer.length) {
						argbBuffer[idx] = (a << 24) + (r << 16) + (g << 8) + (b);
						screenCoordinatesToSegment[idx] = entry.getKey();
						screenCoordinatesToCellNo[idx] = i;
					}
				} else {
					if (x < 0 || x > displaySettings.width || y > displaySettings.height || y < 0)
						continue;
					for (int xi = 0; xi < zoom; xi++) {
						for (int yi = 0; yi < zoom; yi++) {
							int idx = (y+yi) * displaySettings.width + (x+xi);
							if (idx > 0 && idx < argbBuffer.length) {
								argbBuffer[idx] = (a << 24) + (r << 16) + (g << 8) + (b);
								screenCoordinatesToSegment[idx] = entry.getKey();
								screenCoordinatesToCellNo[idx] = i;
							}
						}
					}
				}
			}			
		}
		memoryImageSource.newPixels();
		return atLeastOneVehicle;
	}
	
	public Segment getSegmentUnderMouse(int x, int y) {
		int idx = y * displaySettings.width + x;
		if (idx > 0 && idx < argbBuffer.length) {
			return screenCoordinatesToSegment[idx];
		}
		return null;
	}

	public int getCellNoUnderMouse(int x, int y) {
		int idx = y * displaySettings.width + x;
		if (idx > 0 && idx < argbBuffer.length) {
			return screenCoordinatesToCellNo[idx];
		}
		return UNKNOWN_CELL;
	}

	
	public MemoryImageSource getMemoryImageSource() {
		return memoryImageSource;
	}

	public void setSegments(Collection<? extends Segment> segments) {
		this.segmentsToRender = matchSegmentsToRender(worldMap.getSectionMapping(), segments);
	}
	
	public Map<Segment, RenderingSection> getSegmentsToRender() {
		return segmentsToRender;
	}
}
