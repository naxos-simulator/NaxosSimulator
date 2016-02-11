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



public class GraphTest {
	
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
		final Verticle v1 = new V(1);
		final Verticle v2 = new V(2);
		final Verticle v3 = new V(3);
		final Verticle v4 = new V(4);
		
		final Edge e12 = new E(v1, v2, 1);
		final Edge e23 = new E(v2, v3, 2);
		final Edge e34 = new E(v3, v4, 3);
		final Edge e41 = new E(v4, v1, 4);
		
		
		EdgeCostEstimator ece = new EdgeCostEstimator() {
			public double cost(Edge edge) {
				return 1;
			}
		};
		
		Graph g = new Graph(ece, new Verticle[] {v1, v2, v3, v4}, new Edge[] {e12, e23, e34, e41});
		g.printAllPaths(System.out);
		
	}
	

}
