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
 * 2012-07-11
 */
package de.tzi.gis;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.resources.LineListener;
import de.tzi.resources.LineReader;

/**
 * @author Michal Markiewicz
 *
 */
public class GISCreator implements ActionListener {
	
	private static Logger logger = Logger.getLogger(GISCreator.class);
	
	JButton connectButton, checkPostGIS, cleanDatabase, importData;
	JButton findCrossings, exportData, downloadData;
	JTextArea logArea;
	JComboBox geoArea;
	
	String[][] regions = new String[][] {
			new String[]{"Bremen", "http://download.geofabrik.de/osm/europe/germany/bremen.osm.bz2", "bremen.osm", ""},
			new String[]{"Krak—w", "http://download.geofabrik.de/osm/europe/poland.osm.bz2", "poland.osm", "19.7,49.9,20.1,50.2"},
			new String[]{"Krak—w (Center)", "http://download.geofabrik.de/osm/europe/poland.osm.bz2", "poland.osm", "19.89,50,20.04,50.11"},			
			new String[]{"Bengaluru", "http://download.geofabrik.de/osm/asia/india.osm.bz2", "india.osm", "77.49,12.9,77.69,13.04"},
			new String[]{"New York", "http://download.geofabrik.de/osm/north-america/us/new-york.osm.bz2", "new-york.osm", "-74.069,40.531,-73.767,40.955"},
	};
	
	String[] regionNames;
	{
		regionNames = new String[regions.length];
		for (int i = 0; i < regions.length; i++) {
			regionNames[i] = regions[i][0];
		}
	}
	
	JComponent[] items = new JComponent[] {
			connectButton = new JButton("Connect"),
			checkPostGIS = new JButton("Check PostGIS"),
			downloadData = new JButton("Download data"),
			cleanDatabase = new JButton("Clean database"),
			importData = new JButton("Import data"),
			findCrossings = new JButton("Find crossings"),
			exportData = new JButton("Export"), 
	};
	
	String[][] scripts = new String[][] {
			new String[] {},
			new String[] {"check_postgis.sql"},
			new String[] {},
			new String[] {"clean.sql"},
			new String[] {},
			new String[] {"crossings.sql"},
			new String[] {"export.sql"},
	};
	
	final static String DB_SCRIPTS_PATH_PREFIX = "db";
	
	public GISCreator() {
		JFrame frame = new JFrame("GIS Creator");
		logArea = new JTextArea(20, 70);
		logArea.setLineWrap(true);
		logArea.setBackground(frame.getBackground());
		logArea.setEditable(false);
		Container panel = new JPanel();
		DefaultCaret caret = (DefaultCaret)logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane(logArea);
		Container main = frame.getContentPane();
		main.setLayout(new BorderLayout());
		main.add(panel, BorderLayout.LINE_START);
		main.add(scrollPane, BorderLayout.CENTER);
		geoArea = new JComboBox(regionNames);
		main.add(geoArea, BorderLayout.NORTH);		
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		for (JComponent jButton : items) {
			panel.add(jButton);
			jButton.setEnabled(false);
			if (jButton instanceof JButton) {
				((JButton)jButton).addActionListener(this);
			}
		}
		connectButton.setEnabled(true);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((dim.width-frame.getSize().width)/2, (dim.height-frame.getSize().height)/2);
		frame.setVisible(true);
		log("Url:\t"+GlobalConfiguration.getInstance().getString(SettingsKeys.DB_URL));
		log("User:\t"+GlobalConfiguration.getInstance().getDecodedString(SettingsKeys.DB_USER));
		log("Pass:\t"+GlobalConfiguration.getInstance().getDecodedString(SettingsKeys.DB_PASS));
	}

	
	public static void main(String[] args) {
		new GISCreator();
	}

	public void actionPerformed(ActionEvent e) {
		log("");
		if (e.getSource() == connectButton) {
			log(DAO.getInstance().debugInfo());
			boolean enabled = DAO.getInstance().checkConnection();
			allButtons(enabled);
			connectButton.setEnabled(true);
		} else if (e.getSource() == downloadData) { 
			log("Please download proper file from: "+regions[geoArea.getSelectedIndex()][1]);
			log("wget "+regions[geoArea.getSelectedIndex()][1]);
		} else if (e.getSource() == importData) { 
			log("Please import data using osm2pgsql tool:");
			String host = GlobalConfiguration.getInstance().getString(SettingsKeys.DB_URL);
			host = host.substring(host.lastIndexOf("//") + 2, host.lastIndexOf('/'));
			host = (host.contains(":") ? host.substring(0, host.indexOf(':')) : host);
			String db = GlobalConfiguration.getInstance().getString(SettingsKeys.DB_URL);
			db = db.substring(db.lastIndexOf('/') + 1);
			String area = regions[geoArea.getSelectedIndex()][3];
			area = area == "" ? "" : " --bbox "+area;
			String user = GlobalConfiguration.getInstance().getDecodedString(SettingsKeys.DB_USER);
			log("osm2pgsql  -m  -U "+user+" -H " + host
					+ " -d " + db + " " + regions[geoArea.getSelectedIndex()][2] + area);
		} else if (e.getSource() instanceof JButton) {
			runScript((JButton)e.getSource());
		}
	}
	
	private void runScript(JButton button) {
		int idx = Integer.MIN_VALUE;
		for (int i = 0; i < items.length; i++) {
			if (button == items[i]) {
				idx = i;
				break;
			}
		}
		if (idx == Integer.MIN_VALUE || idx >= scripts.length) {
			log("Script not found for this action!");
			return;
		}
		final String[] scr = scripts[idx];
		Thread thread = new Thread(new Runnable() {
			public void run() {
				allButtons(false);
				for (String script : scr) {
					executeSQL(GlobalConfiguration.concatenateDirWithFileName(DB_SCRIPTS_PATH_PREFIX, script));
				}
				allButtons(true);
			}
		});
		thread.start();
	}
	
	private void allButtons(boolean enabled) {
		for (JComponent jButton : items) {
			jButton.setEnabled(enabled);
		}
	}
	
	private void executeSQL(String path) {
		try {
			new LineReader(new LineListener() {
				StringBuffer sb = new StringBuffer();
				public void acceptLine(String line) {
					try {
						line = line.trim();
						if (line.length() == 0)
							return;
						log(line);
						DAO.getInstance().executeSQL(line, sb);
						log(sb.toString());
						sb.setLength(0);
					} catch (SQLException e) {
						log(e);
					}
				}
			}).read(new FileInputStream(path));
		} catch (IOException e) {
			log(e);
		}
	}

	private void log(Throwable e) {
		logger.error(e);
		log(e.getMessage());
	}
	
	private void log(Object o) {
		logger.info(o);
		logArea.append(String.valueOf(o));
		logArea.append("\n");
		//logArea.setCaretPosition(logArea.getText().length());
	}
	
}
