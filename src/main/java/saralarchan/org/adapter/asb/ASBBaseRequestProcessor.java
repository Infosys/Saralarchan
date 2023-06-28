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
package saralarchan.org.adapter.asb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import saralarchan.org.adapter.db.SQLLiteConnector;
import saralarchan.org.logger.LogManager;

	public class ASBBaseRequestProcessor {
	 

		protected String suffix = "";

		protected static Hashtable<String, String> ASBCache = new Hashtable<String, String>();
		protected static Hashtable<String, ArrayList> asbmappings = new Hashtable<String, ArrayList>();
		protected  ArrayList<String>  aList = null;
		/*
		 * {
		 * 
		 * @Override public synchronized Enumeration<Object> keys() { return
		 * Collections.enumeration(new TreeSet<Object>(super.keySet())); } };
		 */
		
		
	    protected  void getMapping() {
	    	aList = new ArrayList();
	    	LogManager.logInfo("Loading configuration");
	    	String mapFile = this.getClass().getSimpleName()+".properties";	   	
			try {
				LogManager.logInfo(System.getProperty("user.dir"));
				File f = new File(System.getProperty("user.dir")+"/"+mapFile);
				LogManager.logInfo("Mapping file to read is "+f.getAbsolutePath());
		    	FileReader fr;
				fr = new FileReader(f);
				BufferedReader bfr = new BufferedReader(fr);
				//ArrayList content = new ArrayList();
				    String line;			    
				    while ((line = bfr.readLine()) != null) {
				        aList.add(line);
				       
				    }
	                if ( aList.isEmpty()) {
	                	LogManager.logInfo("No MAPPING found");
	                	aList.add("NO_MAP");
	                	asbmappings.put(this.getClass().getSimpleName(), aList);
	                	
	                }
	                else {
	                	asbmappings.put(this.getClass().getSimpleName(), aList);
	                }
				  
		    	
		    	fr.close();
		    	f = null;
		    	
			} catch (FileNotFoundException e1) {
				
				LogManager.logError("Could not Read Mapping File :",e1);
			} catch (IOException e) {
				LogManager.logError("Error while closing file reader :",e);
			}
			
	    }   
	    
	    
	    protected String checkCache(String responseTemplate) {
	    	String json = null;
	    	if (ASBCache.get(responseTemplate) == null) {
	    		ClassLoader loader = getClass().getClassLoader();
	    		 try (InputStream resourceStream = loader.getResourceAsStream(responseTemplate)) {
	                 if (resourceStream == null) {
	                     LogManager.logDebug("Sorry, unable to find " + responseTemplate);
	                 }
	                 else {
	                 	 int bufferSize = 1024;
	                 	 char[] buffer = new char[bufferSize];
	                 	 StringBuilder out = new StringBuilder();
	                 	 Reader in = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
	                 	 for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
	                 	     out.append(buffer, 0, numRead);
	                 	 }
	                 	 json = out.toString();               	                	
	                 	ASBCache.put(responseTemplate, json);
	                 	in.close();
	                 }
	                 
	                 resourceStream.close();
	    	     } catch (IOException e) {
					
					LogManager.logError("Could not fetch template from cache" , e);
				}
	    		
	    	}
	    	else
	    	{
	    		json =  (String)ASBCache.get(responseTemplate);
	    	}
	    	LogManager.logDebug("return json string from cache:"+json);
	    	return json;
	    	 
	  }
	 
		
		public String execute(String request, String responseTemplate) {
			if ( this.suffix != "") {
				responseTemplate = responseTemplate +"-"+suffix;
			}
			LogManager.logInfo("ResponseTemplate is "+responseTemplate);		
	        String response = null;    
	        LogManager.logDebug("Request called in ASB BaseRequest Processor is : "+request);
			try {
	        Object requestDocument = Configuration.defaultConfiguration().jsonProvider().parse(request);
	        LogManager.logDebug("Request parsed successfully"+requestDocument.toString());
	        DocumentContext dc = JsonPath.parse(checkCache(responseTemplate));
			response = transform(dc,requestDocument);
			}
			catch(Exception e) {
				LogManager.logError("Parsing Exception :", e);
			}
		    
			
	  		return response;
			
		}
		

		protected  String transform(DocumentContext dc, Object requestDocument) {
			HashMap<String, String> localvar = new HashMap<String, String>();
			String tablename ="";
			String columnname ="";
			String condcol="" ;
			String condvalue = "";
			aList =  (ArrayList) asbmappings.get(this.getClass().getSimpleName());
			if (aList == null) {
				getMapping();
				if ( asbmappings.get(this.getClass().getSimpleName() )!= null) {
					aList = (ArrayList<String>) asbmappings.get(this.getClass().getSimpleName());
				}
			}
				
			
			if ( (! aList.isEmpty() ) &&  (aList.get(0) != "NO_MAP" ) ) {
					String dbResponse;
				for (int i=0; i < aList.size(); i++)  {		
					String line = (String)aList.get(i);
					LogManager.logInfo("Splitting "+line);
					String[] toks = line.split("=");		
					String target = toks[0];			
					String src = toks[1];
					StringTokenizer st = new StringTokenizer(src,":");
					String action = st.nextToken();
					LogManager.logDebug("Action called:"+action);
					if ( "FROMREQ".equals(action) ){
						String reqField = st.nextToken();
						String value = null;
						try {
							 value= JsonPath.read(requestDocument, reqField);
							 if ( value != null) {
								 dc.set(target, value);
							 }
						}catch (Exception e) {
							value = null;
						}
						
					}
					else if ( "SETVARFROMREQ".equals(action) ){
						String reqField = st.nextToken();
						String value = null;
						try {
							 value= JsonPath.read(requestDocument, reqField);
							
						}catch (Exception e) {
							value = null;
						}
						//dc.set(target, JsonPath.read(requestDocument, reqField));
						if (target.contains("$VAR"))
						{
					      String[] vars = target.split("\\.");
						  localvar.put(vars[1],value);
						}
						LogManager.logDebug("Local Variables defined "+localvar.toString());
					}
					else if ("FROMJSONARRAY".equals(action)) {
						boolean arrayResponse = false;
						String reqField = st.nextToken();
						LogManager.logInfo("Looking for JSONPath "+reqField);
						JSONArray value = null;
						try {
							 value= JsonPath.read(requestDocument, reqField);
							 LogManager.logInfo("ARRAY Data "+value);
							 dc.set(target,value );
							 if ( value != null) {
								arrayResponse = true;
							 }
						}catch (Exception e) {
							e.printStackTrace();
							LogManager.logInfo("EXCEPTION RECEIVED : ARRAY Data set to null "+e.getMessage());
							value = null;
						}
						
					}
					else if ("FROMDB".equals(action) ){
						try {
						
						String dbdetails = st.nextToken();
						StringTokenizer st1 = new StringTokenizer(dbdetails,"^");
						if ( st1.countTokens() == 3) {
						String inputcond =  st.nextToken();
						if ( st.hasMoreTokens()) {
							// check if the variable is not null 
							// if the variable is null then skip this DB call
							String condVar = st.nextToken();
							if (condVar.contains("$VAR"))
							{
						      String[] vars = condVar.split("\\.");
							  String flag = localvar.get(vars[1]);
							  if (flag == null) {
								  continue;
							  }
							}
						}
						if ( inputcond.contains("$VAR")) {
							  String[] arr = inputcond.split("\\.");
							  condvalue = (String) localvar.get(arr[1]);
						}
						else
						{
						      condvalue = JsonPath.read(requestDocument, inputcond);
						}
						 tablename = st1.nextToken();
						 columnname = st1.nextToken();
						 condcol = st1.nextToken();	
						}
						dbResponse = SQLLiteConnector.queryDB(tablename,columnname,condcol,condvalue);
						if (dbResponse != null && dbResponse.charAt(0) == '[')
						{
							LogManager.logDebug("Db Response "+dbResponse);
							JsonArray convertedObject = new Gson().fromJson(dbResponse, JsonArray.class);
							LogManager.logDebug(convertedObject.toString());
							
								  Iterator<?> iter1 = convertedObject.iterator(); 
								  int Counter = 0;
								  while (iter1.hasNext()) 
								  { 
							     
							      JsonObject obj = (JsonObject) iter1.next();
							      
							      dc.add(target, obj);
							     
								  String arrayTarget =target + "[" + Counter + "]"; 
								 
								      Map<String, String> model = new HashMap<String, String>();
							        
									  Set<Entry<String, JsonElement>> es = obj.entrySet(); 
									  Iterator<Entry<String, JsonElement>> iter2 =
									  es.iterator();
									  while ( iter2.hasNext()) {
									  
									  Entry<String, JsonElement> jsE = (Entry<String, JsonElement>) iter2.next();
									  String key = jsE.getKey();
									  JsonElement el = jsE.getValue();
									  
									  model.put(key, el.getAsString());
							
								      
								  
								  }
									  LogManager.logDebug(arrayTarget+"="+model.toString());
									  dc.set(arrayTarget,model );
									  Counter++; 
									  LogManager.logDebug(dc.jsonString());
								  }
							
						}
						else if ( dbResponse != null) {
							 LogManager.logDebug("Db Response "+dbResponse);
						dc.set(target,dbResponse);}
						
						}catch(Exception e) {
							LogManager.logError("Likely faulty mapping for request received "+target+" "+src, e);
						}
					}
					
					else if ("SETVARFROMDB".equals(action)) {
						String dbdetails = st.nextToken();
						StringTokenizer st1 = new StringTokenizer(dbdetails,"^");
						if ( st1.countTokens() == 3) {
						 String inputcond =  st.nextToken();
						 condvalue = JsonPath.read(requestDocument, inputcond);
						 tablename = st1.nextToken();
						 columnname = st1.nextToken();
						 condcol = st1.nextToken();	
						dbResponse = SQLLiteConnector.queryDB(tablename,columnname,condcol,condvalue);
						if ( target.contains("$VAR")) {
							String[] tk = target.split("\\.");
							localvar.put(tk[1], dbResponse);
						}
					  }
					}
					else if ("FROMVAR".equals(action)) {
						String var = st.nextToken();
						dc.set(target,localvar.get(var) );
					}
					else if ("SETVAR".equals(action)) {
						String value = st.nextToken();
						if (target.contains("$VAR"))
						{
					      String[] vars = target.split("\\.");
						  localvar.put(vars[1],value);
						}
						LogManager.logDebug("Local Variables defined "+localvar.toString());
					}
					else if ("FROMDEFAULT".equals(action) ){ 
						
					}
					
				}
				
			}
		
		  	LogManager.logDebug(dc.jsonString());
	     	return dc.jsonString();
		}


		protected  String resppond(String request, String responseTemplate) {
			if ( this.suffix != "") {
				responseTemplate = responseTemplate +"-"+suffix;
			}
			LogManager.logDebug("ResponseTemplate is "+responseTemplate);		
	          
			
	  		return checkCache(responseTemplate);
			
		}
		
		public String extraInfo(String resp) {
			String rv = null;
			return rv;
		}
		
	}


