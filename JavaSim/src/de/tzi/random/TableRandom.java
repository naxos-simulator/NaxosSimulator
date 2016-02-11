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
 * 2012-07-02
 */
package de.tzi.random;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;

/**
 * @author Michal Markiewicz
 *
 */
public class TableRandom extends Random {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -857276440143807518L;
	
	Scanner scanner;
	
	public TableRandom(String fileName) throws IOException {
		scanner = new Scanner(new File(
				GlobalConfiguration.concatenateDirWithFileName(
						GlobalConfiguration.getInstance().getString(
								SettingsKeys.RANDOM_TABLES_DIR), fileName)));
	}
	
	private Lock l = new ReentrantLock();
	
	public float nextFloat() {
		l.lock();
		try {
			return scanner.nextFloat();
		} finally {
			l.unlock();
		}
	}
	
	static {
		Locale.setDefault(Locale.US);
	}

	public static void main(String[] args) throws IOException {
		for (double stdDev = 0.01; stdDev <= 0.25; stdDev += 0.01) {
			int v = (int)Math.round(stdDev * 100);
			String randomTableFileName = "r_0"+(v <  10 ? "0" : "")+v+".txt";
			System.out.println(randomTableFileName);
			TableRandom t = new TableRandom(randomTableFileName);
			System.out.println(t.nextFloat());
		}
	}

}
