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

package de.tzi.traffic.navigation;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;
import de.tzi.geometry.City;
import de.tzi.io.Loader;
import de.tzi.traffic.TrafficManager;



public class GraphTestGen {
	
	static class V implements Verticle {
		final int id;
		public V(int id) {
			this.id = id;
		}
		public int getId() {
			return id;
		}
		public String toString() {
			return "("+id+")";
		}
	}

	
	static class E implements Edge {

		final int id;
		Verticle in;
		Verticle out;
		
		public E(Verticle in, Verticle out, int id) {
			this.id = id;
			this.in = in;
			this.out = out;
		}
		
		public int getId() {
			return id;
		}

		public Verticle getInput() {
			return in;
		}

		@Override
		public Verticle getOutput() {
			return out;
		}
		
		public String toString() {
			return in+"->"+out;
		}
		
		
	}


	
	/*
	 * v4 <--e34--- v3
	 * |            ^
	 * |            |
	 * e41         e23
	 * |            |
	 * \/           |
	 * v1 ---e12--> v2
	 * 
	 */
	
	public static void main(String[] args) {
		
		EdgeCostEstimator ece = new EdgeCostEstimator() {
			public double cost(Edge edge) {
				return 1;
			}
		};
		GlobalConfiguration.getInstance().setString(SettingsKeys.DATA_FILES_DIRECTORY, "data/GEN02");
		City city = Loader.loadWorld();
		TrafficManager trafficManager = new TrafficManager(city);
				
		Verticle[] v = trafficManager.getCrossings().toArray(new Verticle[0]);
		Edge[] e = trafficManager.getSegments().toArray(new Edge[0]);
		
		Graph g = new Graph(ece, v, e);
		for (int a = 0; a < v.length; a++) {
			for (int b = 0; b < v.length; b++) {
				if (0 == g.distTo[a][b])
				System.out.printf("%d %d %.0f\n", a, b, g.distTo[a][b]);
			}
		}
//		g.printAllPaths(System.out);
	}
	

}
