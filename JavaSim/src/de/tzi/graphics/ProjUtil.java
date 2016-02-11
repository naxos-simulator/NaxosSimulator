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
 * May 25, 2012
 */
package de.tzi.graphics;

/**
 * 
 * @see http://en.wikipedia.org/wiki/Gudermannian_function
 * 
 * @see http 
 *      ://stackoverflow.com/questions/1166059/how-can-i-get-latitude-longitude
 *      -from-x-y-on-a-mercator-map-jpeg
 * @see http://projnet.codeplex.com/discussions/77458
 * 
 */
public class ProjUtil {

	public final static double DEGREES_PER_RADIAN = 180.0 / Math.PI;
	public final static double RADIANS_PER_DEGREE = Math.PI / 180.0;

	/**
	 * Calculates the Y-value (inverse Gudermannian function) for a latitude.
	 * 
	 * @param latitude
	 *            The latitude in degrees to use for calculating the Y-value
	 * @return The Y-value for the given latitude
	 */
	public static double gudermannianInv(double latitude) {
		double sign = Math.signum(latitude);
		double sin = Math.sin(latitude * RADIANS_PER_DEGREE * sign);
		return sign * (Math.log((1.0 + sin) / (1.0 - sin)) / 2.0);
	}

	/**
	 * 
	 * @param y
	 *            is in the range of +PI to -PI
	 * @return latitude in degrees
	 */
	public static double gudermannian(double y) {
		return Math.atan(Math.sinh(y)) * DEGREES_PER_RADIAN;
	}

	public static double latToMercator(double lat) {
		double y = Math.log(Math.tan((90 + lat) * Math.PI / 360))
				/ (Math.PI / 180);
		y = y * 20037508.34 / 180;
		return y;
	}

	public static double lonToMercator(double lon) {
		double x = lon * 20037508.34 / 180;
		return x;
	}

	public static double inverseMercatorToLon(double x) {
		double lon = (x / 20037508.34) * 180;
		return lon;
	}

	public static double inverseMercatorToLat(double y) {
		double lat = (y / 20037508.34) * 180;
		lat = 180 / Math.PI
				* (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
		return lat;
	}

}
