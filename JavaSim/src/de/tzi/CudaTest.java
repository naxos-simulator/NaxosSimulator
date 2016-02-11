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

package de.tzi;

import static jcuda.driver.JCudaDriver.cuCtxCreate;
import static jcuda.driver.JCudaDriver.cuCtxSynchronize;
import static jcuda.driver.JCudaDriver.cuDeviceGet;
import static jcuda.driver.JCudaDriver.cuInit;
import static jcuda.driver.JCudaDriver.cuLaunchKernel;
import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.driver.JCudaDriver.cuModuleGetFunction;
import static jcuda.driver.JCudaDriver.cuModuleLoad;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;
import jcuda.driver.JCudaDriver;
import jcuda.runtime.JCuda;

import org.apache.log4j.Logger;

public class CudaTest {

	private final static Logger logger = Logger.getLogger(CudaTest.class); 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		JCudaDriver.setExceptionsEnabled(true);
		cuInit(0);
		CUdevice device = new CUdevice();
		cuDeviceGet(device, 0);
		CUcontext context = new CUcontext();
		cuCtxCreate(context, 0, device);
		
		CUmodule module = new CUmodule();
		cuModuleLoad(module, "jcuda/Rule184.ptx");
		CUfunction function = new CUfunction();
		cuModuleGetFunction(function, module, "move");
		
		
		int MAX = 1024 * 1023 / 2;
		/*
		int hostArr[] = new int[MAX];
		float treshold = GlobalConfiguration.getInstance().getFloat(SettingsKeys.VEHICLE_DENSITY);
		for (int i = 0; i < hostArr.length; i++) {
			if (Math.random() < treshold) {
				hostArr[i] = 1;
			} else {
				hostArr[i] = 0;
			}
		}
		*/
		//logger.info(Arrays.toString(hostArr));
		CUdeviceptr devPtr = new CUdeviceptr();
		int deviceSize = MAX * Sizeof.INT;
		cuMemAlloc(devPtr, deviceSize);
		//cuMemcpyHtoD(devPtr, Pointer.to(hostArr), deviceSize);
		
		int[] time = {0};

		int blockSizeX = 256;
		int gridSizeX = (int)Math.ceil((double)MAX / 2 / blockSizeX);

		//boolean onlyFirstAndLast = !true;
		//StringBuffer sb = new StringBuffer();
		long start = System.currentTimeMillis();
		int iterations = 10 * 60 * 16 * 2; //16 m/s - ten minutes
		for (int i = 0; i < iterations; i++) {
			Pointer kernelParameters = Pointer.to(Pointer.to(new int[] {MAX}), 
					Pointer.to(time),
					Pointer.to(devPtr));
			cuLaunchKernel(function, 
					gridSizeX, 1, 1,
					blockSizeX, 1, 1,
					0, null,
					kernelParameters, null);
			cuCtxSynchronize();

			/*
			if (!onlyFirstAndLast || i == 0 || i == MAX / 2 - 1) {
				cuMemcpyDtoH(Pointer.to(hostArr), devPtr, deviceSize);
				for (int j = 0; j < MAX / 2; j++) {
					sb.append(hostArr[2 * j + time[0]] > 0 ? 'o' : '.');
				}
				logger.info(sb.toString());
				sb.setLength(0);
			}
			*/
			time[0] = 1 - time[0];
		}
		long stop = System.currentTimeMillis();
		logger.info((stop - start)+"ms vehicles: "+MAX+" iterations: "+iterations+" single iteration ms "+((double)stop-start) / iterations);
		cuMemFree(devPtr);

	}

	/**
	 * @param args
	 */
	public static void main2(String[] args) {
		Pointer devPtr = new Pointer();
		JCuda.cudaMalloc(devPtr, 1024 * 1024 * 1024);
		logger.info("Pointer: "+devPtr);
		JCuda.cudaFree(devPtr);
	}


	
}
