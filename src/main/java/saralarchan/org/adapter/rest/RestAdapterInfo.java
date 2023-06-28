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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest.Builder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import saralarchan.org.adapter.AdapterInfo;
import saralarchan.org.core.BaseRequestProcessor;
import saralarchan.org.logger.LogManager;

public class RestAdapterInfo extends AdapterInfo {

	public static HashMap transformerCache = new HashMap();
	public static HashMap bodyTmplCache = new HashMap();

	public RestAdapterInfo() {
//	@URIDOMIAN=https://domain/
//	@URIPATH=part1/$VAR.var1/part2
//  @METHOD=GET
//	@QSPARAM=xyz:$VAR.var1
//	@QSPARAM=abc:$VAR.var2
//	@QSPARAM=def:$.RequestName.path
//	@HEADER=Content-Type:application/json
//	@HEADER=authToken:tokenforauthorizationinsamlformat oranyotherformat
//  @BODYTMLP=jsonfilename
//  @TRANSFORM @ACTION:DO->
//  @TRANSFORM @CASE @ACTION:DO->

	}
	
    public static String getMethod(String mappingFile) {
    	HashMap mappings = loadTransformer(mappingFile);
    	return (String) mappings.get("@METHOD");
    }
    
	public static Builder setUri(String mappingFile, Builder builder, String queryString, HashMap ctx) {
		HashMap mappings = loadTransformer(mappingFile);
		String domain = (String) mappings.get("@URIDOMAIN");
		String uripath = "";

		ArrayList resourceList = (ArrayList) mappings.get("@URIPATH");
		for (int i = 0; i < resourceList.size(); i++) {
			String part = (String) resourceList.get(i);
			if (part.startsWith("$VAR.")) {
				String[] arr = part.split("\\.");
				part = (String) ctx.get(arr[1]);
			}
			uripath = uripath + part;
		}
		String fulluri = domain + uripath;
		if (queryString.length() > 1)
			fulluri = fulluri + "?" + queryString;
		URI u;
		try {
			u = new URI(fulluri);
			builder.uri(u);
		} catch (URISyntaxException e) {
			LogManager.logError("URI Error in " + adapterName, e);
		}
		return builder;
	}

	public static String getUri(String mappingFile, String queryString, HashMap ctx) {
		HashMap mappings = loadTransformer(mappingFile);
		String domain = (String) mappings.get("@URIDOMAIN");
		LogManager.logInfo("@URIDOMAIN"+domain);
		String uripath = "";

		ArrayList resourceList = (ArrayList) mappings.get("@URIPATH");
		for (int i = 0; i < resourceList.size(); i++) {
			String part = (String) resourceList.get(i);
			if (part.startsWith("$VAR.")) {
				String[] arr = part.split("\\.");
				part = (String) ctx.get(arr[1]);
			}
			uripath = uripath +"/"+ part;
		}
		String fulluri = domain + uripath;
		if (queryString.length() > 1)
			fulluri = fulluri + "?" + queryString;
        LogManager.logInfo("Full URL: "+fulluri);
		return fulluri;
	}

	public static HashMap loadTransformer(String mappingfile) {
		HashMap mappings = (HashMap) transformerCache.get(mappingfile);
		
		if (mappings == null) {
			LogManager.logInfo("MappingFile for RESTCALL"+mappingfile);
			mappings = new HashMap();
			File f = new File(System.getProperty("user.dir") + "/" + mappingfile);
			FileReader fr;
			try {
				fr = new FileReader(f);
				BufferedReader bfr = new BufferedReader(fr);
				String line;
				while ((line = bfr.readLine()) != null) {
					LogManager.logInfo("line"+line);
					if ( line.startsWith("@TRANSFORM->")) {
						ArrayList trasformationList = (ArrayList) mappings.get("@TRANSFORM");
						if (trasformationList == null) {
							trasformationList = new ArrayList();
						}
						line=line.replaceAll("@TRANSFORM->",""); // jump the keywork @TRANSFORM
						trasformationList.add(line);  // add the mapping line for processing
						mappings.put("@TRANSFORM", trasformationList);
						continue;
					}
					StringTokenizer st = new StringTokenizer(line, "=");
					String urlpart = null;
					String key = null;
					if (st.countTokens() == 2) {
						 key = st.nextToken();
						 urlpart = st.nextToken();
						 LogManager.logInfo("key:"+key+" "+"value: "+urlpart);
						switch (key) {
						case "@URIDOMAIN":
							
							mappings.put(key, urlpart);
							
							break;
						case "@URIPATH":
							String[] parts = urlpart.split("/");
							ArrayList resource = new ArrayList();
							for (int i = 0; i < parts.length; i++) {
								resource.add(parts[i]);
							}
							mappings.put(key, resource);
							break;
						case "@METHOD":
							mappings.put(key, urlpart);
							break;
						case "@QSPARAM":
							String[] params = urlpart.split(":");
							HashMap qsMap = (HashMap) mappings.get(key);
							if (qsMap == null) {
								qsMap = new HashMap();

							}
							qsMap.put(params[0], params[1]);
							mappings.put(key, qsMap);

						case "@HEADER":
							String[] headers = urlpart.split(":");
							HashMap headerMap = (HashMap) mappings.get(key);
							if (headerMap == null) {
								headerMap = new HashMap();

							}
							headerMap.put(headers[0], headers[1]);
							mappings.put(key, headerMap);
							break;
						case "@BODYTMPL":
							getBodyTmpl(urlpart);
							mappings.put("@BODYTMPL", urlpart);
							break;
						}
					}
				}

				if (mappings.size() > 0) {
					LogManager.logInfo("Putting REST spec to cache");
					transformerCache.put(mappingfile, mappings);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return mappings;
	}

	public static String transform(String mappingFile, HashMap ctx, DocumentContext req) {
		String json = null;
		HashMap mappings = loadTransformer(mappingFile);
		String template = (String) mappings.get("@BODYTML");
		ArrayList lines = (ArrayList) mappings.get("@TRANSFORM");
		DocumentContext dc = getBodyTmpl(template);
		BaseRequestProcessor rp = new BaseRequestProcessor(mappingFile);
		rp.process(lines, ctx, dc, req, "-1");
		return dc.jsonString();
	}

	public static String parseQS(String mappingFile, HashMap ctx) {
		HashMap mappings = loadTransformer(mappingFile);
		String qs = "";
		HashMap qsMap = (HashMap) mappings.get("@QSPARAM");
		if (qsMap == null)
			return "";

		Set keys = qsMap.keySet();
		Iterator iter = keys.iterator();
		String specifier = "";

		while (iter.hasNext()) {

			String key = (String) iter.next();
			String [] parts = null;
			String keyspec = (String) ctx.get(key);
			if (keyspec != null) {
				if (keyspec.startsWith("$VAR.")) {
				 parts = keyspec.split("\\.");
				 key = parts[1];
				
			}
			}
			specifier = (String) qsMap.get(key);
			String value = (String) ctx.get(specifier);

			if (qs == "") {
				qs = key+"="+value;
			} else {
				qs = qs+"&"+key+"="+value;
			}

		}
        LogManager.logInfo("QS:"+qs);
		return qs;
	}

	public static ArrayList<BasicHeader> parseHeaders(String mappingFile, HashMap<String, String> ctx) {
		HashMap mappings = loadTransformer(mappingFile);
		ArrayList<BasicHeader> requestHeaders = new ArrayList();
		HashMap headerMap = (HashMap) mappings.get("@HEADER");

		Set<String> keys = headerMap.keySet();
		Iterator<String> iter = keys.iterator();
		String specifier = "";
		
		while (iter.hasNext()) {
			String key = (String) iter.next();
			//specifier = (String) headerMap.get(key);
			
			String [] parts = null;
			String keyspec = (String) headerMap.get(key);
			if (keyspec != null) {
				if (keyspec.startsWith("$VAR.")) {
				 parts = keyspec.split("\\.");
				 key = parts[1];	
				 String value = (String) ctx.get(key);
				 requestHeaders.add(new BasicHeader(key, value));
			}
				else
				{
					requestHeaders.add(new BasicHeader(key, (String)headerMap.get(key)));
				}
			}
			
			
		}
		
        LogManager.logInfo("Headers: "+requestHeaders.toString());
		return requestHeaders;
	}

	public static String sendPOST(String mappingFile, HashMap ctx, DocumentContext req) {

		String result = "";

		try {
			String uri = getUri(mappingFile, "", ctx);
			HttpPost post = new HttpPost(uri);
			ArrayList<BasicHeader> headers = parseHeaders(mappingFile, ctx);
			post.setHeaders((BasicHeader[]) headers.toArray());
			String postbody = transform(mappingFile, ctx, req);

			post.setEntity(new StringEntity(postbody));
			CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = httpClient.execute(post);
			result = EntityUtils.toString(response.getEntity());

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public static String doGet(String mappingFile, HashMap ctx,DocumentContext reqDc) {

		String result = null;
		String fullurl = null;
		String qs = null;
		HttpGet request = null;
		ArrayList<BasicHeader> headers = null;
		try {

			qs = parseQS(mappingFile, ctx);
			fullurl = getUri(mappingFile, qs, ctx);
			request = new HttpGet(fullurl);
			headers = parseHeaders(mappingFile, ctx);
			for ( int i = 0; i < headers.size(); i++) {
				request.setHeader(headers.get(i));
			}
			

			CloseableHttpClient httpClient = HttpClients.createDefault();
			LogManager.logInfo("Calling service "+request.toString());
			CloseableHttpResponse response = httpClient.execute(request);
			int returncode = response.getStatusLine().getStatusCode();
			LogManager.logInfo("Response Code:"+returncode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
			}

		} catch (IOException e) {
			LogManager.logError("Http Get method encountered error:" + fullurl, e);
		}
		return result;
	}

	protected static DocumentContext getBodyTmpl(String bodyTemplate) {
		String json = null;
		DocumentContext dc = null;
		if (bodyTmplCache.get(bodyTemplate) == null) {
			try {
				File f = new File(bodyTemplate);
				LogManager.logInfo("Mapping file to read is " + f.getAbsolutePath());
				FileReader fr;
				fr = new FileReader(f);
				BufferedReader bfr = new BufferedReader(fr);
				int bufferSize = 1024;
				char[] buffer = new char[bufferSize];
				StringBuilder out = new StringBuilder();
				for (int numRead; (numRead = bfr.read(buffer, 0, buffer.length)) > 0;) {
					out.append(buffer, 0, numRead);
				}
				json = out.toString();
				dc = JsonPath.parse(json);
				bodyTmplCache.put(bodyTemplate, dc);
			} catch (IOException e) {
				LogManager.logError("Body Template Fetch Error:", e);
			}
		} else {
			dc = (DocumentContext) bodyTmplCache.get(bodyTemplate);
		}
		LogManager.logDebug("Returnig parsed body template");
		return dc;

	}

}
