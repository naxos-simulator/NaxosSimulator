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
 * 2014-06-07
 */
package de.tzi.readouts;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michal Markiewicz
 *
 */
public class CO2RuleGenerator {

	
	static int counts[] = new int[16]; 
	
	final static int ROAD_LENGTH = 30;
	
	byte[][] road = new byte[2][];
	
	
	
	public CO2RuleGenerator()  throws Exception {
		road[0] = new byte[ROAD_LENGTH];
		road[1] = new byte[ROAD_LENGTH];
		
		int turn = 0;
		road[turn][0] = 1;
		//road[turn][2] = 15;
		road[turn][3] = 1;
		while ((char) System.in.read() == '\n') {
			printRoad(road[turn]);
			for (int i = 1; i < ROAD_LENGTH - 1; i++) {
				road[1 - turn][i] = (byte)co2rule(road[turn][i-1], road[turn][i], road[turn][i+1]); 
			}
			road[1 - turn][0] = (byte)co2rule(5, road[turn][0], road[turn][1]);
			road[1 - turn][ROAD_LENGTH - 1] = (byte)co2rule(road[turn][ROAD_LENGTH - 2], 
					road[turn][ROAD_LENGTH - 1], 15); 
			turn = 1 - turn;
		}
		
		
		
		
	}
	
	void printRoad(byte[] r) {
		for (byte b : r) {
			if (b == 0)
				System.out.printf(".");
			else 
				System.out.printf("%01x", b);
		}
		System.out.printf("");
	}
	
	private static void entry(byte a, byte b, byte c, byte d, int no) {
		//StringBuffer sb = new StringBuffer();
		//int z = ((a & 0xf) << 4) | (b & 0x0f);
		//int v = ((c & 0xf) << 4) | (d & 0x0f);
		
		//System.out.printf("%01x\n", d);
		
		counts[d]++; 
		
		
//		System.out.printf("%02d\t%02d\t%02d\t%02d\n", a,b,c,d);
		
		//System.out.printf("%02x, %02x\n", z, v);
		if ((no & 0x1) > 0) {
//			System.out.printf("/* %01x%01x%01x */ ", a,b,c);
			System.out.printf("%01x", d);
		} else {
			System.out.printf("%01x, ", d);
//			System.out.printf("/* %01x%01x%01x */ \n", a,b,c);
		}
	}
	
	public static void mainBool(String[] args) throws Exception {
		for (int prev = 0; prev < 0x10; prev++) {
			int Zp = 1 - ((prev & 1) | ((prev & 2) >> 1) |  ((prev & 4) >> 2) | ((prev & 8) >> 3));
			int Ep = (1 - (prev & 1)) & ((prev & 2) >> 1) &  ((prev & 4) >> 2) & ((prev & 8) >> 3);
			int Fp = (prev + 1) >> 4;//15 = f = 1111
			
			boolean ZpB = prev == 0;
			boolean EpB = prev == 14;
			boolean FpB = prev == 15;
			
			if (Zp == 1 != ZpB) {
				throw new RuntimeException("ERR: Z:"+prev);
			}
			if (Zp == 0 != !ZpB) {
				throw new RuntimeException("ERR: Z:"+prev);
			}
			
			if (Ep == 1 != EpB) {
				throw new RuntimeException("ERR: E:"+prev);
			}
			if (Ep == 0 != !EpB) {
				throw new RuntimeException("ERR: E:"+prev);
			}
			if (Fp == 1 != FpB) {
				throw new RuntimeException("ERR: F:"+prev);
			}
			if (Fp == 0 != !FpB) {
				throw new RuntimeException("ERR: F:"+prev);
			}
		}
		
		
	}
	public static void main(String[] args) throws Exception {
		new CO2RuleGenerator();
	}
	
	public static void mainSingle(String[] args) {
		co2rule4(14,0,1);
	}
	
	/**
	 * @param args
	 */
	public static void mainAll(String[] args) {
		byte[] res = new byte[(0x10 * 0x10 * 0x10) / 2];
		int counter = 0;
		for (int a = 0; a <= 0xf; a++) {
			for (int b = 0; b <= 0xf; b++) {
				for (int c = 0; c <= 0xf; c++) {
					int d = co2rule(a,b,c);
					if ((counter & 0x1) > 0) {
						res[counter >> 1] |= (byte)d;
					} else {
						res[counter >> 1] = (byte)(d << 4);
					}
					counter++;
					entry((byte)a,(byte)b,(byte)c,(byte)d, counter);
				}
			}
		}
		System.out.println(counter);
		for (int i = 0; i < counts.length; i++) {
			System.out.println(i+": "+counts[i]);
		}

	}
	
	private static final boolean inRange(int x, int a, int b) {
		return x >= a && x <= b;
	}
	
	
	private static int co2rule3(int prev, int curr, int next) {
		int rcnt = 0;
		int res = -1;
		if (inRange(prev, 1, 15) && inRange(curr, 1, 15) && inRange(next,1, 15)) { //1 
			rcnt++;
			res =  15;
		}
		if (inRange(prev, 1, 15) && curr == 15 && next == 0) { //2
			rcnt++;
			res =  0;
		}
		if (inRange(prev, 1, 15) && inRange(curr, 1, 14) && next == 0) {//3
			rcnt++;
			res =  0;
		}
		if (inRange(prev, 1, 13) && curr == 0 && inRange(next,1, 14)) {//4
			rcnt++;
			res =  Math.min(prev + 1, next);;
		}
		if (prev == 14 && curr == 0 && inRange(next,1, 14)) {//5
			rcnt++;
			res =  next;
		}

		if (inRange(prev, 1, 14) && curr == 0 && next == 15) {//6
			rcnt++;
			res =  15;
		}
		if (prev == 15 && curr == 0 && inRange(next, 1, 15)) {//7
			rcnt++;
			res =  1;
		}
		if (inRange(prev, 1, 13) && curr == 0 && next == 0) {//8
			rcnt++;
			res =  prev + 1;
		}
		if (prev == 14 && curr == 0 && next == 0) {//9
			rcnt++;
			res =  14;
		}
		if (prev == 15 && curr == 0 && next == 0) {//10
			rcnt++;
			res =  1;
		}

		if (prev == 0 && inRange(curr, 1, 15) && inRange(next, 1, 15)) {//11
			rcnt++;
			res =  15;
		}
		
		if (prev == 0 && inRange(curr, 1, 15) && next == 0) {//12
			rcnt++;
			res =  0;
		}
		if (prev == 0 && curr == 0 && inRange(next, 1, 15)) {//13
			rcnt++;
			res =  0;
		}
		if (prev == 0 && curr == 0 && next == 0) {//14
			rcnt++;
			res =  0;
		}
		if (rcnt != 1) 
			throw new RuntimeException(prev+ " "+curr+" "+next+" -> "+rcnt);
		return res;
	}
	
	
	private static int co2rule4(int prev, int curr, int next) {
		int Zp = 1 - ((prev & 1) | ((prev & 2) >> 1) |  ((prev & 4) >> 2) | ((prev & 8) >> 3));
		int Zc = 1 - ((curr & 1) | ((curr & 2) >> 1) |  ((curr & 4) >> 2) | ((curr & 8) >> 3));
		int Zn = 1 - ((next & 1) | ((next & 2) >> 1) |  ((next & 4) >> 2) | ((next & 8) >> 3));
		//int Ep = Math.min(0, (prev & 0xf) ^ ~(14 & 0xf)) << 1);
		//15 lub 14
		int Ep = (1 - (prev & 1)) & ((prev & 2) >> 1) &  ((prev & 4) >> 2) & ((prev & 8) >> 3);
		//int Ec = ((curr + 2) & 0x10) >> 4;
		//boolean En = next == 14; // e = 1000  1 0001   
		int Fp = (prev + 1) >> 4;//15 = f = 1111
		//int Fc = (curr + 1) >> 4;
		int Fn = (next + 1) >> 4;
		//min
		int sizeof_int_in_bits = 4 * 8;
		int x = prev+1;
		int y = next;
		int min = //Math.min(prev + 1, next);
				y + ((x - y) & ((x - y) >> (sizeof_int_in_bits - 1)));
		int res = (1 - Zp) * (1 - Zc) * (1 - Zn) * 15;// 1
		res += (1 - Zp) * Zc * (1 - Ep) * (1 - Zn) * (1 - Fp) * (1 - Fn) * min; // 4
		res += Zc * (1 - Zn) * Ep * (1 - Fn) * next; // 5
		res += (1 - Zp) * Zc * (1 - Fp) * Fn * 15; // 6
		res += Zc * (1 - Zn) * Fp; // 7
		res += (1 - Zp) * Zc * Zn * (1 - Ep) * (1 - Fp) * (prev + 1); // 8
		res += Zc * Zn * Ep * 14; // 9
		res += Zc * Zn * Fp; // 10
		res += Zp * (1 - Zc) * (1 - Zn) * 15;// 11
		return res;
	}
	
	private static int co2rule(int prev, int curr, int next) {
		int rcnt = 0;
		int res = -1;
		Set<Integer> rules = new TreeSet<Integer>();
		
		boolean Zp = prev == 0;
		boolean Zc = curr == 0;
		boolean Zn = next == 0;
		boolean Ep = prev == 14;
		//boolean Ec = curr == 14;
		//boolean En = next == 14;
		boolean Fp = prev == 15;
		boolean Fc = curr == 15;
		boolean Fn = next == 15;
		
		if (!Zp && !Zc && !Zn) { //1
		//if (inRange(prev, 1, 15) && inRange(curr, 1, 15) && inRange(next,1, 15)) { //1 
			rcnt++;
			res =  15;
			rules.add(1);
		}
		if (!Zp && Fc && Zn) { //2
		//if (inRange(prev, 1, 15) && curr == 15 && next == 0) { //2
			rcnt++;
			res =  0;
			rules.add(2);
		}
		if (!Zp && !Zc && !Fc && Zn) {//3
		//if (inRange(prev, 1, 15) && inRange(curr, 1, 14) && next == 0) {//3
			rcnt++;
			res =  0;
			rules.add(3);
		}
		if (!Zp && !Fp && !Ep && Zc && !Fn && !Zn) {//4
		//if (inRange(prev, 1, 13) && curr == 0 && inRange(next,1, 14)) {//4
			rcnt++;
			res =  Math.min(prev + 1, next);;
			rules.add(4);
		}
		if (Ep && Zc && !Zn && !Fn) {//5
		//if (prev == 14 && curr == 0 && inRange(next,1, 14)) {//5
			rcnt++;
			res =  next;
			rules.add(5);
		}
		if (!Zp && !Fp && Zc && Fn) {//6
		//if (inRange(prev, 1, 14) && curr == 0 && next == 15) {//6
			rcnt++;
			res =  15;
			rules.add(6);
		}
		if (Fp && Zc && !Zn) {//7
		//if (prev == 15 && curr == 0 && inRange(next, 1, 15)) {//7
			rcnt++;
			res =  1;
			rules.add(7);
		}
		if (!Zp && !Ep && !Fp && Zc && Zn) {//8
		//if (inRange(prev, 1, 13) && curr == 0 && next == 0) {//8
			rcnt++;
			res =  prev + 1;
			rules.add(8);
		}
		if (Ep && Zc && Zn) {//9
		//if (prev == 14 && curr == 0 && next == 0) {//9
			rcnt++;
			res =  14;
			rules.add(9);
		}
		if (Fp && Zc && Zn) {//10
		//if (prev == 15 && curr == 0 && next == 0) {//10
			rcnt++;
			res =  1;
			rules.add(10);
		}
		if (Zp && !Zc && !Zn) {//11
		//if (prev == 0 && inRange(curr, 1, 15) && inRange(next, 1, 15)) {//11
			rcnt++;
			res =  15;
			rules.add(11);
		}
		
		if (Zp && !Zc && Zn) {//12
		//if (prev == 0 && inRange(curr, 1, 15) && next == 0) {//12
			rcnt++;
			res =  0;
			rules.add(12);
		}
		if (Zp && Zc && !Zn) {//13
		//if (prev == 0 && curr == 0 && inRange(next, 1, 15)) {//13
			rcnt++;
			res =  0;
			rules.add(13);
		}
		
		if (Zp && Zc && Zn){
		//if (prev == 0 && curr == 0 && next == 0) {//14
			rcnt++;
			res =  0;
			rules.add(14);
		}
		int a = 0;
		int b = 0;
		if (rcnt != 1 || res != (a = co2rule3(prev,curr,next)) || res != (b = co2rule4(prev, curr, next))) {
			
			throw new RuntimeException("\n"+prev+ " "+curr+" "+next+" -> "+rcnt+" "+res+"  "+a+" "+b+" rules: "+rules.toString());
			
		}
		return res;
	}
}
