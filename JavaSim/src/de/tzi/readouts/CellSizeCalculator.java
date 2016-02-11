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
 * 2014-06-22
 */
package de.tzi.readouts;

/**
 * 
 * @author Michal Markiewicz
 *
 */
public class CellSizeCalculator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int start = 1;
		int step = 1;
		int stop = 76;
		int order = 10;
		
		double[] errors = new double[(stop - start) / step];
		
		double[] values = {0.721944444,1.655209538,2.797862771,4.11354745,5.537017742,6.994280096,8.424760062,9.792268648,11.08323539,12.29907858,13.44880633,14.54401295};
		int idx = 0;
		for (int v = start; v < stop; v+= step) {
			double error = 0;
			double[] multiplications = new double[values.length];
			int[] mul = new int[values.length];
			double[] err = new double[values.length];
			double[] err2 = new double[values.length];
			for (int i = 0; i < multiplications.length; i++) {
				mul[i] = (int) Math.round(order * values[i] / (1.0 * v));
				//The vehicle has to advance by at least one cell when moving with the lowest speed
				mul[i] = Math.max(1, mul[i]);
				multiplications[i] = Math.round(mul[i] * v) / (1.0 * order);
				double e = Math.abs(multiplications[i] - values[i]);
				error += e * e;
				err[i] = e;
				err2[i] = e*e;
			}
			errors[idx++] = error;//Math.sqrt(error);
			final boolean disable = true;
			System.out.printf("Cell size: %02d error: %2.4f: \n", v, error);
			if (!disable) {
				print(values, "%02.2f ", " ", true);
				print(multiplications, "%02.2f ", " ", true);
				print(mul, "%4d ", " ", true);
			}
			if (v == 14 || v == 7 || v == 29 || v == 75) {
				print(values, "%02.2f ", "&", true);
				print(multiplications, "%02.2f ", "&", true);
				print(mul, "%4d ", "&", true);
				print(err, "%02.2f ", "&", true);
				print(err2, "%02.2f ", "&", true);
			}
		}
		System.out.print("\ne <- c(");
		print(errors, "%2.3f", ", ", false);
		System.out.println(");\nplot(e, type=\"l\");");
	}
	
	private static void print(double[] c, String formatter, String spacer, boolean newLine) {
		int max = c.length;
		for (int i = 0; i < c.length; i++) {
			System.out.printf(formatter, c[i]);
			if (--max > 0) {
				System.out.print(spacer);
			}
		}
		if (newLine) {
			System.out.println();
		}
	}
	
	
	private static void print(int[] c, String formatter, String spacer, boolean newLine) {
		int max = c.length;
		for (int i = 0; i < c.length; i++) {
			System.out.printf(formatter, c[i]);
			if (--max > 0) {
				System.out.print(spacer);
			}
		}
		if (newLine) {
			System.out.println();
		}
	}

}
