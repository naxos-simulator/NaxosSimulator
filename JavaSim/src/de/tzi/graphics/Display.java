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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.statistics.R;
import de.tzi.traffic.Crossing;
import de.tzi.traffic.Segment;
import de.tzi.traffic.TrafficManager;

/**
 * @author Michal Markiewicz
 * @see http://jakobvogel.net/
 */

public class Display extends JPanel {
	
	public static boolean SHOW_PARTIAL_STATS = false;
	public static boolean SHOW_PLOT_CMD = false;
	
	public static boolean COMPLETE_STATISTICS = false;
	
	public static boolean SHOW_BOTH = false;
	public static boolean ONE_TO_FOUR = true;
	
	public static int STOP_AT_TIME = 1000000;
	
	private static Logger logger = Logger.getLogger(Display.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Image imageToShow; 
	private Image imageBuffer;

	final Renderer renderer;
	
	final TrafficManager trafficManager;
	
	final R r;
	
	public Display(Renderer renderer, TrafficManager trafficManager) {
		this.renderer = renderer;
		this.trafficManager = trafficManager;
		r = new R(trafficManager);
		imageBuffer = createImage(renderer.getMemoryImageSource());
		imageToShow = new BufferedImage(renderer.displaySettings.width, renderer.displaySettings.height, BufferedImage.TYPE_3BYTE_BGR);
		setPreferredSize(new Dimension(renderer.displaySettings.width, renderer.displaySettings.height));
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		setFocusable(true);
		render();
	}

	public boolean render() {
		//long start = System.currentTimeMillis();
		boolean atLeastOneVehicle = renderer.render();
		if (dragging) {
			imageToShow.getGraphics().fillRect(0, 0, getWidth(), getHeight());
		}
		imageToShow.getGraphics().drawImage(imageBuffer, viewDiffX, viewDiffY, this);
		repaint();
		//long stop = System.currentTimeMillis();
		//logger.debug(1000.0/(stop-start));
		return atLeastOneVehicle;
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(imageToShow, 0, 0, this);
	}

	public static boolean resetCO2 = !true; 
	protected void processKeyEvent(KeyEvent e) {
		boolean FPSMode = true;
		boolean doRender = false;
		char c = e.getKeyChar();
		if (c == '.' && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.tick();
			doRender = true;
		} else if (c=='c'  && (e.getID() == KeyEvent.KEY_TYPED)) {
			r.computeStatistics();
		} else if (c=='r'  && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.tick();
			r.updateStatistics();
			doRender = true;
		} else if ((c == ' ' && (e.getID() == KeyEvent.KEY_PRESSED))||
				(c == '1' && (e.getID() == KeyEvent.KEY_TYPED))) {
			//Target reached per normal vehicle and per smart vehicle
			r.showQuickStatistics();
			//long time = trafficManager.tick();
			//logger.debug(time);
			doRender = true;
		} else if (c == '2' && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.changeLights();
		} else if (c == '3' && (e.getID() == KeyEvent.KEY_TYPED)) {
			logger.debug(trafficManager);
		} else if (c == '4' && (e.getID() == KeyEvent.KEY_TYPED)) {
			trafficManager.getPropertyManager().print(System.out);
		} else if (c == '5' && (e.getID() == KeyEvent.KEY_TYPED)) {
			logger.info(renderer.displaySettings);
		} else if (c == '0' && (e.getID() == KeyEvent.KEY_TYPED)) {
			resetCO2 = true;
		} else if (c == '6' && (e.getID() == KeyEvent.KEY_TYPED)) {
			resetCO2 = !true;
		} else if (c == 'l' && (e.getID() == KeyEvent.KEY_TYPED)) {
			new Thread(new Runnable() {
				public void run() {
					try {
						for (int i = 0; i < 25000; i++) {
							//Thread.sleep(90);
							synchronized (Display.this) {
								trafficManager.tick();
								r.updateStatistics();
							}
							if (!render())
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
					}
				}
			}).start();
		} else if (!FPSMode && c == 't' && (e.getID() == KeyEvent.KEY_TYPED)) {
			//doStats(0);
			//doRender = true;	

			if (!shouldBreak) {
				shouldBreak = true;
			} else {
				shouldBreak = false;
				new Thread(new Runnable() {
					public void run() {
						
						int fps = GlobalConfiguration.getInstance().getInt(SettingsKeys.WINDOW_FPS);
						int milis = 1000 / fps;
						try {
							while (!Display.this.shouldBreak){
								long start = System.currentTimeMillis();
								synchronized (Display.this) {
									trafficManager.tick();
									r.updateStatistics();
								}
								
								if (!render())
									break;
								long diff = System.currentTimeMillis() - start;
								long toGo = Math.max(0, milis - diff);
								if (toGo > 0) {
									Thread.sleep(toGo);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							logger.error(e);
						}
					}
				}).start();
			}

			
		} else if (c == 'q' && (e.getID() == KeyEvent.KEY_TYPED)) {
			logger.info("Exitting...");
			System.exit(0);
		}	
		if (doRender)
			render();
		e.consume();
	}
	
	boolean shouldBreak = true;;
	

	
	int clickX, clickY;
	int viewDiffX, viewDiffY;
	boolean dragging = false;
	
	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			clickX = e.getX();
			clickY = e.getY();
			viewDiffX = 0;
			viewDiffY = 0;			
			Segment segment = renderer.getSegmentUnderMouse(clickX, clickY);
			if (segment != null) {
				int cellNo = renderer.getCellNoUnderMouse(clickX, clickY);
				Crossing crossing = null;
				if (cellNo == 0) {
					crossing = segment.getInput();
				} else if (cellNo == segment.getSectionLength() - 1) {
					crossing = segment.getOutput();
				}
				if (crossing != null) {
					logger.info(crossing);
				}
				int vehicleId = segment.getVehicleAtIndex(cellNo);
				logger.info(segment+" "+cellNo + " vehicle: "+vehicleId);
				if (vehicleId > 0) {
					logger.info(trafficManager.getPropertyManager().vehiclePropertiesToString(vehicleId));
				}
			}
			dragging = true;
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {			
			dragging = false;
			renderer.displaySettings.shiftX += viewDiffX;
			renderer.displaySettings.shiftY += viewDiffY;
			viewDiffX = 0;
			viewDiffY = 0;
			renderer.adjustScreenCoordinates();
			renderer.clearBackground();
			render();
		}
	}
	
	protected void processMouseMotionEvent(MouseEvent e) {
		if (dragging) {
			viewDiffX = e.getX() - clickX;
			viewDiffY = e.getY() - clickY;
			render();
		}
	}
	
	protected void processMouseWheelEvent(MouseWheelEvent e) {
		double prevZoom = e.isShiftDown() 
				? renderer.displaySettings.pixelSize
				: renderer.displaySettings.zoom;
		double newZoom = prevZoom;
		double step = 0.4;
		if (e.getWheelRotation() > 0) {
			newZoom += step;	
		} else  {
			newZoom -= step;
			if (newZoom <= 0.6) {
				newZoom = 0.6;
			}
		}
		if (e.isShiftDown()) {
			renderer.displaySettings.pixelSize = newZoom;
		} else { 
			renderer.displaySettings.shiftX *= (newZoom / prevZoom);
			renderer.displaySettings.shiftY *= (newZoom / prevZoom);
			renderer.displaySettings.zoom = newZoom;
		}
		if (prevZoom != newZoom) {
			renderer.adjustScreenCoordinates();
			renderer.clearBackground();
			render();
		}
		e.consume();

	}

	public void doStats(final int trial) {

		
		new Thread(new Runnable() {
			public void run() {
					int mod = 1;
					float fps = GlobalConfiguration.getInstance().getFloat(SettingsKeys.WINDOW_FPS);
					long milis = 
						//0;
						(int)Math.max(1, Math.round(1000.0 / fps));
					long lastUpdate = System.currentTimeMillis();
					while (true) {
						synchronized (Display.this) {
							trafficManager.tick();
							r.updateStatistics();
						}
						if (System.currentTimeMillis() - lastUpdate > milis) {
							lastUpdate = System.currentTimeMillis();
							
							if (GlobalConfiguration.getInstance().getBoolean(SettingsKeys.INTERACTIVE_MODE)) {
								if (!render())
									break;
							}
						}
						if (trafficManager.getTime() % mod == 0) {							
							if (Display.SHOW_BOTH) {
								r.computeStatistics();
								r.computeStatistics2(trial);
							} else {
								if (Display.COMPLETE_STATISTICS) {
									r.computeStatistics();
								} else {
									r.computeStatistics2(trial);
								}
							}
							
							if (trafficManager.getTime() >= Display.STOP_AT_TIME) {
								if (Display.SHOW_BOTH) {
									r.showTotalStats(trial);
									r.showTotalStats2(trial);
								} else {
									if (Display.COMPLETE_STATISTICS) {
										r.showTotalStats(trial);
									} else {
										r.showTotalStats2(trial);
									}
								}
								logger.info("Exitting...");
								//System.exit(0);
								return;
							}
						}
						
					}
			}
		}).run();
		//start();
	}
	

}
