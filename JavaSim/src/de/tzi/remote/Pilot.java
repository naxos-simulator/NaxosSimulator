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
 * 2014-10-12
 */
package de.tzi.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.sf.json.JSONObject;

/**
 * @author Michal Markiewicz
 *
 */
public class Pilot {
	
	final static String BASE_URL = "http://YOUR-IIS-SERWER-ADDRESS.pl";
	
	public static String FETCH_REQUEST_URL_EXT = "";
	
	final static String FETCH_REQUEST_URL = BASE_URL+"/tzi/R";
	final static String RESULTS_REQUEST_URL = BASE_URL+"/tzi/W";
	
	final static boolean ECHO = true;
	
	static class StreamWrapper extends Thread {
		InputStream is;
		boolean buffered;
		StringBuffer sb = new StringBuffer();

		public StreamWrapper(InputStream is) {
			this.is = is;
			buffered = true;
		}

		public StreamWrapper(InputStream is, boolean buffered) {
			this.is = is;
			this.buffered = buffered;
		}
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				if (!buffered) {
					int c;
					while ((c = isr.read()) != -1) {
						sb.append((char)c);
					}
				} else {
					BufferedReader br = new BufferedReader(isr);
					String line = null;
					while ((line = br.readLine()) != null) {
						sb.append(line).append('\n');
						if (ECHO) {
							System.out.println(line);
						}
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		public static StreamWrapper createStreamWrapper(InputStream is, boolean buffered) {
			StreamWrapper sw = new StreamWrapper(is, buffered);
			sw.start();
			return sw;
		}
		
		public static StreamWrapper createStreamWrapper(InputStream is) {
			return createStreamWrapper(is, true);
		}

		public String toString() {
			return sb.toString();
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		boolean first = true; 
		while (true) {
			try {
				if (first) {
					first = false;
					if (args.length > 0) {
						FETCH_REQUEST_URL_EXT = args[0];
					}
				} else {
					FETCH_REQUEST_URL_EXT = "";
				}
				action();
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(35 * 1000);
			}
			Thread.sleep(5 * 1000);
		}
	}
	public static void action() throws IOException, InterruptedException {
		
		//Connect to remote site
		String response = fetchTask();
		System.out.println("JSON: "+response.trim());
		
		JSONObject json = JSONObject.fromObject(response);
		int code = json.getInt("code");
		String desc = json.getString("desc");
		if (code != 0) {
			System.out.println("Code: "+code+" Desc: "+desc);
		}
		
		JSONObject task = json.getJSONObject("task");
		String jar = task.getString("jar");
		String cmd = task.getString("cmd");
		
		//Create temporary directory
		File dir = createTempDirectory();

		System.out.println("Temporary directory: " + dir.getAbsolutePath());
		//Download current version
		System.out.println("Downloading: "+jar);
		String fileName = download(jar, dir);

		System.out.println("Unzipping: "+fileName);
		UnZip.unzip(dir.getAbsolutePath()+File.separator+fileName, dir.getAbsolutePath());

		System.out.println("Executing: ");
		System.out.println(cmd);
		long startTime = System.currentTimeMillis();	
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd, null, dir);
		StreamWrapper errorStream = StreamWrapper.createStreamWrapper(pr.getErrorStream());
		StreamWrapper inputStream = StreamWrapper.createStreamWrapper(pr.getInputStream());
		int errorCode = pr.waitFor();
		System.out.println(errorCode);
		System.out.println(errorStream);
		if (!ECHO) {
			System.out.println(inputStream);
		}
		long duration = System.currentTimeMillis() - startTime;
		task.put("duration", duration);
		String result = inputStream.toString() + errorStream.toString(); 
		task.put("result", result);
		System.out.println("Sending results"); 
		String status = sendResults(task);
		
		System.out.println(status.trim());

		System.out.println("Cleaning up...");
		deleteDirectory(dir);
		System.out.println("Done.");
	}

	private static String sendResults(JSONObject task) throws IOException, InterruptedException {
		String body = "t="+task.toString();
		String request = RESULTS_REQUEST_URL;
		URL url = new URL(request); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod("POST"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
		connection.setUseCaches(false);
		connection.getOutputStream().write(body.getBytes());
		connection.getOutputStream().close();
		StreamWrapper requestStream = StreamWrapper.createStreamWrapper(connection.getInputStream(), false);
		requestStream.join();
		String response = requestStream.toString();
		return response;
	}
	
	private static String fetchTask() throws IOException, InterruptedException {
		String[] paramsNames = {"os.name", "os.arch", "os.version", "java.vendor", "java.version" };
		StringBuffer params = new StringBuffer();
		int max = paramsNames.length;
		for (String p : paramsNames) {
			params.append(p.replace('.', '_'));
			params.append('=');
			params.append(URLEncoder.encode(System.getProperty(p), "utf-8"));
			if (--max > 0)
				params.append('&');
		}
		String urlParameters = params.toString();
//		System.out.println(urlParameters);
		String request = FETCH_REQUEST_URL + FETCH_REQUEST_URL_EXT;
		URL url = new URL(request); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod("POST"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
		connection.setUseCaches(false);
		connection.getOutputStream().write(urlParameters.getBytes());
		connection.getOutputStream().close();
		StreamWrapper requestStream = StreamWrapper.createStreamWrapper(connection.getInputStream(), false);
		requestStream.join();
		String response = requestStream.toString();		
		return response;
	}

	private static String download(String jar, File dir) throws IOException {
		URL website = new URL(jar);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		String fileName = jar.substring(jar.lastIndexOf('/')+1);
		FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		return fileName;
	}

	/**
	 * @see http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java
	 * @return
	 * @throws IOException
	 */
	public static File createTempDirectory() throws IOException {
	    final File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
	    if(!(temp.delete())) {
	        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	    }
	    if(!(temp.mkdir())) {
	        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	    }
	    return temp;
	}
	
	static public void deleteDirectory(File path) {
		if (path == null)
			return;
		if (path.exists()) {
			for (File f : path.listFiles()) {
				System.out.println("Deleting: "+f.getAbsolutePath());
				if (f.isDirectory()) {
					deleteDirectory(f);
				}
				f.delete();
			}
			path.delete();
		}
	}
	
}
