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
package de.tzi.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

/**
 * @author m
 *
 */
public class ImageComposer {

	public static String PATH_TO_FILE_LIST = "tiles/files.txt";
	
	SortedMap<String, List<String>> ss = new TreeMap<String, List<String>>();
	
	private void log(Object o) {
		System.out.println(o);
	}
	
	int linesCount = 0;
	
	private void readFileList() throws Exception {
		
		BufferedReader fr = new BufferedReader(new FileReader((PATH_TO_FILE_LIST)));
		String line;
		while ((line = fr.readLine()) != null) {
			line = line.trim();
			String[] arr = line.split("/");
			if (arr.length < 5) {
				log(line);
				continue;
			}
			linesCount++;
			String ttf = arr[1];
			String dst = line.substring(line.indexOf('/', line.indexOf('/') + 1) + 1);
			List<String> ttfs = ss.get(dst);
			if (ttfs == null) {
				ss.put(dst, ttfs = new LinkedList<String>());
			}
			ttfs.add(ttf);
		}
		fr.close();
		log("Lines read: "+linesCount);
	}
	
	private void showAll() throws Exception {
		for (Map.Entry<String, List<String>> e : ss.entrySet()) {
			if (e.getValue().size() > 1) {
				log(e.getKey()+": "+e.getValue().size());
				if (!new File(DST_BASE_PATH, e.getKey()).exists()) {
					composeImages(e);
				}
			}
		}
		log("Files combined: "+filesCombined);
		log("Tiles counter: "+tileCounter);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ImageComposer ic = new ImageComposer();
		ic.readFileList();
		ic.showAll();
	}
	
	final static int WIDTH = 256;
	final static int HEIGHT = 256;
	final static String SRC_BASE_PATH = "/Volumes/m/Desktop/Mapy/Ortofoto";//"tiles/src";
	final static String DST_BASE_PATH = "tiles/dst";
	
	int filesCombined = 0;
	int tileCounter = 0;
	
	private void composeImages(Map.Entry<String, List<String>> e) throws Exception {
		try {
			BufferedImage combined = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics g = combined.getGraphics();
			for (String ttf : e.getValue()) {
				String path = ttf + File.separatorChar + e.getKey();
				BufferedImage image = ImageIO.read(new File(SRC_BASE_PATH, path));
				g.drawImage(image, 0, 0, null);
				filesCombined++;
			}
			String dir = e.getKey().substring(0, e.getKey().lastIndexOf('/'));
			new File(DST_BASE_PATH, dir).mkdirs();
			ImageIO.write(combined, "PNG", new File(DST_BASE_PATH, e.getKey()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		tileCounter++;
	}

	//http://stackoverflow.com/questions/2318020/merging-two-images
}
