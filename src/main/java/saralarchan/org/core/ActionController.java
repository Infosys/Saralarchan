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
package saralarchan.org.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import saralarchan.org.core.service.EndpointService;
import saralarchan.org.logger.LogManager;

@RestController
public class ActionController {

	ProductService service = new ProductService();

	@RequestMapping(value = "/performAction")
	public void takeAction(HttpServletRequest req, HttpServletResponse response) {

		Enumeration<String> parameterNames = req.getParameterNames();
		String paramValue = null;
		while (parameterNames.hasMoreElements()) {

			String paramName = parameterNames.nextElement();
			System.out.println(paramName);

			if (paramName.equalsIgnoreCase("name")) {
				String[] paramValues = req.getParameterValues(paramName);
				for (int i = 0; i < paramValues.length; i++) {
					paramValue = paramValues[i];
					System.out.println(paramValue);

				}
				String result = executeCommand(paramValue);
				System.out.println(result);
			}
		}

	}

	@RequestMapping(value = { "/API/**", "/open-api/**", "/global/**" }, method = { RequestMethod.POST }, consumes = {
			"application/json", "text/plain" }, produces = { "application/json" })
	public ResponseEntity process(@RequestBody String payload, HttpServletRequest request) throws Exception {
		LogManager.logInfo(request.getRequestURI());

		String requestType = "";
		//String baseUrl = ServletUriComponentsBuilder.fromRequestUri((HttpServletRequest) request).build().getPath();
		final String baseUrl = 
				ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		
		StringTokenizer st = new StringTokenizer(baseUrl, "/");
		StackTraceElement[] trace = new Throwable().getStackTrace();

		String key = trace[0].getClassName() + "." + trace[0].getMethodName();
		HashMap paramnameholder = (HashMap) EndpointService.paramTemplates.get(key);
		int position = 0;

		String extraparams = "";

		while (st.hasMoreTokens()) {
			requestType = st.nextToken();
			if (paramnameholder != null && paramnameholder.get(position) != null) {
				extraparams = extraparams + "," + "\"" + paramnameholder.get(position) + "\"" + ":" + "\"" + requestType
						+ "\"";

			}
			position++;

		}

		Enumeration e1 = request.getHeaderNames();
		JSONObject headerparams = new JSONObject();
		headerparams.put("URL", baseUrl);
		while (e1.hasMoreElements()) {
			String headername = e1.nextElement().toString();
			LogManager.logInfo(headername + "=" + request.getHeader(headername));
			headerparams.put(headername, request.getHeader(headername));
		}

		LogManager.logDebug(payload);
		if (!PatternFinder.loaded) {
			PatternFinder.loadPatterns();
		}
		if (payload.length() > 2) {
			String JSONRequest = payload.replaceAll("\\s", "");

			// int braceStart = JSONRequest.lastIndexOf("{");
			int braceStart = JSONRequest.indexOf("{");
			JSONRequest = JSONRequest.substring(braceStart);
			LogManager.logDebug(JSONRequest);

			JSONRequest = JSONRequest + extraparams;

			// JSONRequest = "{\""+requestType+"\":"+ JSONRequest + "}";
			JSONRequest = "{\"" + requestType + "\":" + JSONRequest + ",\"HEADERs\":" + headerparams.toString() + "}";

			String matchKey = PatternFinder.get(JSONRequest);

			LogManager.logInfo("Match Key is :[" + matchKey + "] and request is :[" + JSONRequest + "]");
			if (matchKey.equalsIgnoreCase("NOMATCH")) {
				matchKey = requestType;

			}
			HashMap resp = service.execute(matchKey, JSONRequest);
			LogManager.logInfo("Response sent :[" + resp + "]");

			return createResponse(resp);
		} else
			return ResponseEntity.status(400).body("{\"error\":\"Received a truncated request\"}");
	}

	private String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

	@RequestMapping(value = { "/app/api/**" }, method = { RequestMethod.OPTIONS, RequestMethod.POST,
			RequestMethod.GET }, consumes = { MediaType.ALL_VALUE, "application/json",
					"text/plain" }, produces = { "application/json", "text/plain", MediaType.TEXT_HTML_VALUE })
	public ResponseEntity simpleservice(@RequestBody String payload, HttpServletRequest request) throws Exception {
		LogManager.logInfo(request.getRequestURI());

		String requestType = "";
		//String baseUrl = ServletUriComponentsBuilder.fromRequestUri((HttpServletRequest) request).build().getPath();
				final String baseUrl = 
						ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

		StringTokenizer st = new StringTokenizer(baseUrl, "/");
		while (st.hasMoreTokens()) {
			requestType = st.nextToken();
		}

		LogManager.logDebug(payload);
		if (!PatternFinder.loaded) {
			PatternFinder.loadPatterns();
		}
		String JSONRequest;
		if (payload == null) {
			JSONRequest = "{\"" + requestType + "\":" + "{\"DUMMY\":\"DUMMY\"}" + "}";
			String matchKey = PatternFinder.get(JSONRequest);
			LogManager.logInfo("Match Key is :[" + matchKey + "] and request is :[" + JSONRequest + "]");
			HashMap resp = service.execute(matchKey, JSONRequest);
			LogManager.logInfo("Response sent :[" + resp + "]");
			return createResponse(resp);
		} else if (payload.length() >= 2) {
			JSONRequest = payload.replaceAll("\\s", "");

			// int braceStart = JSONRequest.lastIndexOf("{");
			int braceStart = JSONRequest.indexOf("{");
			JSONRequest = JSONRequest.substring(braceStart);
			LogManager.logDebug(JSONRequest);

			JSONRequest = "{\"" + requestType + "\":" + JSONRequest + "}";

			String matchKey = PatternFinder.get(JSONRequest);

			LogManager.logInfo("Match Key is :[" + matchKey + "] and request is :[" + JSONRequest + "]");
			if (matchKey.equalsIgnoreCase("NOMATCH")) {
				matchKey = requestType;

			}
			HashMap resp = service.execute(matchKey, JSONRequest);
			LogManager.logInfo("Response sent :[" + resp + "]");

			return createResponse(resp);
		} else
			return ResponseEntity.status(400).body("{\"error\":\"Received a truncated request\"}");

	}

	// Used for fetching pages from resources folder
	// as well as serving all
	// app/ms/** mapping
	// @RequestMapping(value =
	// {"/open/service/feature/sample.json","/oauth2/v2.0/authorize/**","/xxxx.onmicrosoft.com/**"},
	// method = { RequestMethod.OPTIONS,RequestMethod.GET },
	// consumes = {MediaType.ALL_VALUE}, produces =
	// {"application/json","text/plain","text/html"})
	public ResponseEntity performCheck(HttpServletRequest request, @RequestParam Map<String, String> allParams) {

		// String reqName = request.getRequestURI();
		String reqName = request.getRequestURL().toString();
		LogManager.logInfo(reqName);
		Enumeration e1 = request.getHeaderNames();

		JSONObject headerparams = new JSONObject();

		while (e1.hasMoreElements()) {
			String headername = e1.nextElement().toString();
			LogManager.logInfo(headername + "=" + request.getHeader(headername));
			headerparams.put(headername, request.getHeader(headername));
		}

		String requestType = "";
		String JSONRequest = "";
		String baseUrl  = reqName;
		//String baseUrl = ServletUriComponentsBuilder.fromRequestUri((HttpServletRequest) request).build().getPath();
			//	String baseUrl = 
			//			ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		StringTokenizer st = new StringTokenizer(baseUrl, "/");
		StackTraceElement[] trace = new Throwable().getStackTrace();

		String classAndMethod = trace[0].getClassName() + "." + trace[0].getMethodName();
		LogManager.logInfo("Looking for classAndMethod: "+classAndMethod +"in paramTemplates" );
		LogManager.logInfo("HandlerConfig.paramTemplates is:"+EndpointService.paramTemplates.toString());
		HashMap paramnameholder = (HashMap) EndpointService.paramTemplates.get(classAndMethod);
		LogManager.logInfo("patramHolder obtained from gloabl param map:"+paramnameholder.toString());
		int position = 1;
		HashMap extraparams = new HashMap();
		while (st.hasMoreTokens()) {
			
			requestType = st.nextToken();
			LogManager.logInfo("url token is:"+requestType+" and position in url is : "+position);
			if (paramnameholder != null && paramnameholder.get(position) != null) {
				extraparams.put((String)paramnameholder.get(position),requestType);
			}
			position++;
		}

		headerparams.put("URL", baseUrl);
		while (st.hasMoreTokens()) {
			requestType = st.nextToken();
		}

		JSONObject obj = new JSONObject();

		if (allParams.size() == 0) {
			obj.put("DUMMY", "DUMMY");
		} else {
			allParams.forEach((key, value) -> {
				obj.put(key, request.getParameter(key));
			});		
			
		}
		extraparams.forEach((key,value) -> {
			obj.put((String) key, value);
		});
		
		if (!PatternFinder.loaded) {
			PatternFinder.loadPatterns();
		}
		JSONRequest = "{\"" + requestType + "\":" + obj.toString() +",\"HEADERs\":" + headerparams.toString() + "}";
		LogManager.logInfo("JSONRequest :" + JSONRequest);
		String matchKey = PatternFinder.get(JSONRequest);
		if (matchKey.equalsIgnoreCase("NOMATCH")) {
			matchKey = requestType;

		}
		LogManager.logInfo("Match Key is :[" + matchKey + "] and request is :[" + JSONRequest + "]");

		HashMap resp = service.execute(matchKey, JSONRequest);
		LogManager.logInfo("Response sent :[" + resp + "]");
		return createResponse(resp);

	}

	private String getDocument(HttpServletRequest req) {
		String resp = "";
		String requestType = "";
		String baseUrl = req.getRequestURI();
		try {
			if (baseUrl.startsWith("/a/b/c") || baseUrl.startsWith("/a/b/d")) {
				requestType = "xxxx";
			}
			if (baseUrl.startsWith("/oauth2/v2.0")) {
				requestType = "yyyy";
			}
			if (baseUrl.contains("onmicrosoft")) {
				requestType = "authsuccess";
			}

			LogManager.logDebug(System.getProperty("user.dir"));
			File f = new File(System.getProperty("user.dir") + "\\" + requestType + ".html");
			FileReader fr;
			fr = new FileReader(f);
			BufferedReader bfr = new BufferedReader(fr);

			Stream<String> lines = bfr.lines();
			Iterator iter = lines.iterator();
			while (iter.hasNext()) {
				resp = resp + iter.next();
			}
			LogManager.logInfo(resp);

		} catch (FileNotFoundException e1) {

			LogManager.logError("Could not Read Mapping File :", e1);
		}

		return resp;
	}
	public ResponseEntity test(HttpServletRequest request, @RequestParam Map<String, String> allParams) {
		LogManager.logInfo(request.getRequestURI());
		Enumeration e1 = request.getHeaderNames();
		JSONObject headerparams = new JSONObject();
		while (e1.hasMoreElements()) {
			String headername = e1.nextElement().toString();
			LogManager.logInfo(headername + "=" + request.getHeader(headername));
			headerparams.put(headername, request.getHeader(headername));
		}

		String requestType = "";
		String JSONRequest = "";
		String baseUrl = request.getRequestURI().toString();
		//String baseUrl = ServletUriComponentsBuilder.fromRequestUri((HttpServletRequest) request).build().getPath();
			//	final String baseUrl = 
			//			ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		StackTraceElement[] trace = new Throwable().getStackTrace();

		String classAndMethod = trace[0].getClassName() + "." + trace[0].getMethodName();
		HashMap paramnameholder = (HashMap) EndpointService.paramTemplates.get(classAndMethod);
		LogManager.logInfo("patramHolder obtained from gloabl param map:"+paramnameholder.toString());
	
		
		StringTokenizer st = new StringTokenizer(baseUrl, "/");
		int position = 1;
		HashMap extraparams = new HashMap();
		while (st.hasMoreTokens()) {
			
			requestType = st.nextToken();
			if (paramnameholder != null && paramnameholder.get(position) != null) {
				extraparams.put((String)paramnameholder.get(position),requestType);
			}
			position++;
		}
		
		JSONObject obj = new JSONObject();
		if (allParams.size() == 0) {
			obj.put("DUMMY", "DUMMY");
		} else {
			allParams.forEach((key, value) -> {

				obj.put(key, request.getParameter(key));
			});
		}
	
		
		extraparams.forEach((key,value) -> {
			obj.put((String) key, value);
		});
		
		
		if (!PatternFinder.loaded) {
			PatternFinder.loadPatterns();
		}
		JSONRequest = "{\"" + requestType + "\":" + obj.toString() + ",\"HEADERs\":" + headerparams.toString() + "}";
		String matchKey = PatternFinder.get(JSONRequest);
		LogManager.logInfo("JSONRequest :" + JSONRequest + " Match Key :" + matchKey);
		if (matchKey.equalsIgnoreCase("NOMATCH")) {
			matchKey = requestType;

		}
		LogManager.logInfo("Match Key is :[" + matchKey + "] and request is :[" + JSONRequest + "]");

		HashMap respMap = service.execute(matchKey, JSONRequest);

		return createResponse(respMap);

	}

	public ResponseEntity testFunction(HttpServletRequest request, @RequestParam Map<String, String> allParams) {
		LogManager.logInfo("Inside test Function");
		return performCheck(request, allParams);

	}

	private ResponseEntity createResponse(HashMap resp) {

		HttpHeaders responseHeaders = new HttpHeaders();
		HashMap headers = (HashMap) resp.get("RESPONSE_HEADERS");
		if (headers != null) {
			Set keys = headers.keySet();
			Iterator iter = keys.iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				String value = (String) headers.get(key);
				responseHeaders.set(key, value);
			}
		}

		int statuscode = 200;
		if (resp.get("STATUS_CODE") != null) {
			statuscode = (int) resp.get("STATUS_CODE");
		}
		LogManager.logInfo("Response sent :[" + resp.get("RESPONSE") + "]");
		return ResponseEntity.status(statuscode).headers(responseHeaders).body(resp.get("RESPONSE"));

	}

}
