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

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;

/**
 * @author Michal Markiewicz
 *
 */
class DisplaySettings {
	
	boolean showFPS;
	
	int shiftX;
	int shiftY;
	
	double zoom = 1.0;
	double pixelSize = 1;

	int width = 1024;
	int height = 768;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Width: ").append(width);
		sb.append(" height: ").append(height);
		sb.append(" pixelSize: ").append(pixelSize);
		sb.append(" zoom: ").append(zoom);
		return sb.toString();
	}
	
	public DisplaySettings() {
		this.width =  GlobalConfiguration.getInstance().getInt(SettingsKeys.WINDOW_SIZE_WIDTH);
		this.height = GlobalConfiguration.getInstance().getInt(SettingsKeys.WINDOW_SIZE_HEIGHT);
		this.pixelSize = GlobalConfiguration.getInstance().getFloat(SettingsKeys.WINDOW_CELL_SIZE);
		this.zoom = GlobalConfiguration.getInstance().getFloat(SettingsKeys.WINDOW_ZOOM);
	}

}
