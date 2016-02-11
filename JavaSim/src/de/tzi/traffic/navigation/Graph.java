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
 * 2012-06-20
 */
package de.tzi.traffic.navigation;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Michal Markiewicz
 * @see http://algs4.cs.princeton.edu/44sp/FloydWarshall.java.html
 */
public class Graph {
	
	Logger logger = Logger.getLogger(Graph.class);
	
	Verticle[] verticles;
	Edge[] edges;
	
	final static boolean COMPUTE_FIRST_ELEMENTS_AT_ONCE = !true;

	public Graph(EdgeCostEstimator edgeCostEstimator, 
			Verticle[] verticles, Edge[] edges) {
		this.verticles = verticles; 
		this.edges = edges;
		this.edgeCostEstimator = edgeCostEstimator;
		V = verticles.length;
		logger.debug("Edges: "+edges.length+ " verticles "+verticles.length);
		distTo = new double[V][V];
		edgeTo = new Edge[V][V];
		edgeFirst = new Edge[V][V];
		update();
	}
	
	final EdgeCostEstimator edgeCostEstimator;
	final int V;
	
	//PassingPossibility	~ Edge
	//distTo[v][w] = length of shortest v->w path
	final double[][] distTo;
	//Segment 				~ Verticle
	// edgeTo[v][w] = last edge on shortest v->w path
	final Edge[][] edgeTo;
	
	final Edge[][] edgeFirst;
	
	private void initalize() {
		for (int i = 0; i < V; i++) {
			for (int j = 0; j < V; j++) {
				distTo[i][j] = Double.POSITIVE_INFINITY;
			}
			distTo[i][i] = 0;
		}
		for (Edge edge: edges) {
			if (edge.getInput() == null || edge.getOutput() == null)
				continue;
			int from = edge.getInput().getId();
			int to = edge.getOutput().getId();
			distTo[from - 1][to - 1] = edgeCostEstimator.cost(edge);
			edgeTo[from - 1][to - 1] = edge;
		}
	}
	
	/**
	 * @see http://algs4.cs.princeton.edu/44sp/FloydWarshall.java.html
	 */
	void computeFloydWarshall() {
		for (int i = 0; i < V; i++) {
			for (int v = 0; v < V; v++) {
				if (edgeTo[v][i] == null)
					continue;
				for (int w = 0; w < V; w++) {
					if (distTo[v][w] > distTo[v][i] + distTo[i][w]) {
						distTo[v][w] = distTo[v][i] + distTo[i][w];
						edgeTo[v][w] = edgeTo[i][w];
					}
				}
			}
		}
	}
	 // return view of shortest path from v to w, null if no such path
    private Iterable<Edge> path(int v, int w) {
        if (!hasPath(v, w)) 
        	return null;
        List<Edge> path = new LinkedList<Edge>();
        for (Edge e = edgeTo[v][w]; e != null; e = edgeTo[v][e.getInput().getId() - 1]) {
            path.add(e);
        }
        Collections.reverse(path);
        return path;
    }
    
	public double pathCost(int v, int w, EdgeCostEstimator ece) {
        if (!hasPath(v, w)) 
        	return Double.POSITIVE_INFINITY;
        double sum = 0;
        for (Edge e = edgeTo[v][w]; e != null; e = edgeTo[v][e.getInput().getId() - 1]) {
        	sum += ece.cost(e);
        }
        return sum;
    }
	
    public boolean hasPathById(int srcId, int dstId) {
    	return hasPath(srcId - 1, dstId - 1);
    }
    
    public Edge whereToGo(int srcId, int dstId) {
		return COMPUTE_FIRST_ELEMENTS_AT_ONCE ? 
				edgeFirst[srcId - 1][dstId - 1] : 
				firstElementAtPath(srcId - 1, dstId - 1);
    }
        
    private Edge firstElementAtPath(int v, int w) {
        if (!hasPath(v, w)) 
        	return null;
        Edge last = null;
        for (Edge e = edgeTo[v][w]; e != null; e = edgeTo[v][e.getInput().getId() - 1]) {
            last = e;
        }
        return last;
    }
    
    private void computeAllFirstElements() {
    	for (int v = 0; v < verticles.length; v++) {
        	for (int w = 0; w < verticles.length; w++) {
				if (w == v || !hasPath(v, w)) {
					edgeFirst[v][w] = null;
				} else {
					edgeFirst[v][w] = firstElementAtPath(v, w);
				}
			}
    	}
    }
    
    private boolean hasPath(int v, int w) {
        return distTo[v][w] < Double.POSITIVE_INFINITY;
    }
	
    private double dist(int v, int w) {
        return distTo[v][w];
    }
    
	public void update() {
		initalize();
		computeFloydWarshall();
		if (COMPUTE_FIRST_ELEMENTS_AT_ONCE)
			computeAllFirstElements();
	}
	
	public void printAllPaths(PrintStream ps) {
		for (Verticle v : verticles) {
			for (Verticle w : verticles) {
				if (hasPath(v.getId() - 1, w.getId() - 1)) {
					ps.printf("%d-%d %.0f ", v.getId(), w.getId(),
							dist(v.getId() - 1, w.getId() - 1));
                    for (Edge e : path(v.getId() - 1, w.getId() - 1)) {
                        ps.print(e.toString());
                        ps.print(' ');
                    }
                    //ps.print(" @");
                    //ps.print(firstElementAtPath(v.getId() - 1, w.getId() - 1));
                    ps.println();
                }
			}
		}
	}
}
