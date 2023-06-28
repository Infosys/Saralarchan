/**
 * Copyright 2023 Infosys Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package saralarchan.org.adapter.rest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import saralarchan.org.logger.LogManager;
public class HttpsClient {

	private static Set hostNamesSet = new HashSet();
	private static String hostnameStr=null;


	static {

		try {
		FileInputStream fse = new FileInputStream(new File("./resources/hostname.txt"));
			BufferedReader reader = new BufferedReader(
									new InputStreamReader(fse, "UTF-8"));
				while ((hostnameStr = reader.readLine()) != null) {
					hostNamesSet.add(hostnameStr);
				}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {

				if(hostNamesSet.contains(hostname)) {
					return true;
				}

	            return false;
	        }
	    });
	}


public String CallServer(String uri,String request) throws IOException{
	boolean HTTPS = true;
	String streamValue = null;
	HttpURLConnection connection = null;
	InputStream is = null;
	try {
		URL u;

		u = new URL(uri);
		URLConnection uc = u.openConnection();

		if(HTTPS){
			System.out.println("HTTPS");
			connection = (HttpsURLConnection) uc;
		} else {
			System.out.println("HTTP");
			connection = (HttpURLConnection) uc;
		}

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.addRequestProperty("content-type", "application/json");
		connection.setRequestProperty("Client_id", "XXXXXXXX");
        connection.setRequestProperty("Client_secret", "YYYYYYYY");
		connection.connect();
		OutputStream out = connection.getOutputStream();
		out.write(request.getBytes("UTF-8"));
		out.flush();
		out.close();
		is = connection.getInputStream();
		LogManager.logInfo(connection.getHeaderFields().entrySet().toString());
		streamValue = getStreamData(is);
			connection.disconnect();
			is.close();
		LogManager.logInfo("MESSAGESENT:URI="+uri+" RESPONSE: "+streamValue);
	} catch (MalformedURLException e) {

		LogManager.logError("MESSAGESENDERROR:", e);
	}
	catch ( IOException e) {
		if (connection != null) {
			connection.disconnect();

		}
		if (   is != null) {
			is.close();
		}
		LogManager.logError("IOEXCEPTION:", e);
	}

	return streamValue;

}

private static String getStreamData(InputStream is) throws IOException {
	String S = null;
	if (is != null) {
		StringBuilder sb = new StringBuilder();
		String line;

		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			//is.close();
		}
		S = sb.toString();
	}
	return S;
}
}


