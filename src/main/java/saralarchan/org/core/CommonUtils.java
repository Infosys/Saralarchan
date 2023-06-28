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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import saralarchan.org.logger.LogManager;

public class CommonUtils {
	public static Properties runtimeProperties = null;
	public static String getInputDataFromRequest(HttpServletRequest request){
		StringBuilder stringBuilder = new StringBuilder();
		try{
			
        	BufferedReader bufferedReader = request.getReader();
        	char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
		} catch(Exception e){
       	 	LogManager.logError("Error occurred while getting data from Page Form : ",e); 
        }
		return stringBuilder.toString();
	}
	
	public static String getCorrelationID(HttpServletRequest request) {
		UUID uid ;
		String correlationID = "DUMMY";
		if(request != null){
			Cookie[] cookies = request.getCookies();
			if( cookies != null ) {
				for (int i = 0; i < cookies.length; i++) {
					Cookie cookie = cookies[i];
					if(cookie.getName().equalsIgnoreCase("correlationID")){
						correlationID = cookie.getValue();
					}
				}
				
			}
		}
		if ( correlationID.equalsIgnoreCase("DUMMY")) {
			uid = UUID.randomUUID();
			correlationID = uid.toString();
		}
		return correlationID;
	}	
	
	
public static String resolvespecifier(String token,HashMap ctx,DocumentContext respCtx,Object req) {
		
		String[] tokenparts = token.split("\\.");
		if ( tokenparts.length==1) {
			switch (tokenparts[0]) {
			case "CONTEXT" : return ctx.toString(); 
			case "DC" : return respCtx.jsonString();
			case "REQUEST": return JsonPath.read(req, "$").toString();
			default: return token;
			}
		}
		else if (tokenparts.length > 1) {
			String key = tokenparts[1];
			String logpart = "";
		
			for ( int i = 2;i < tokenparts.length; i++) {
				key = key + "."+tokenparts[i];
			}
			switch (tokenparts[0]) {
			case "$VAR" : 
				LogManager.logInfo("Resolving log token from CONTEXT "+key + "in "+tokenparts[0]);
				return  ctx.get(key).toString();
			case "$" : 
				try { 
					LogManager.logInfo("Resolving log token from REQUEST "+key + "in "+tokenparts[0]);
					
					logpart = JsonPath.read(req, key);
				}
				catch ( Exception requestReadException) {
					try {
						LogManager.logInfo("Resolving log token from DC :"+respCtx.jsonString()+" and key is "+key + "in "+tokenparts[0]);
						
					logpart = respCtx.read(key);
					}
					catch ( Exception responseReadException) {
						LogManager.logError("Logspecifier Error: "+logpart, responseReadException);
					}
				}
				return logpart;
			case "$SYSTEM" : 
				LogManager.logInfo("Resolving log token from SYSTEM  "+key + "in "+tokenparts[0]);
				
				return getGlobalProp(key);
				
			}
		}
		return null;
	}
public static boolean setTarget(String target,HashMap varmap,DocumentContext dc,String value) {
	boolean rv = false;
	if (target.contains("$VAR"))
	{
     
      String key = target.substring(5);
      varmap.put(key,value);
      return true;
	}
	else if (target.startsWith("$.")){
		if (target.equalsIgnoreCase("$.")) {
			 LogManager.logInfo("Setting value to dc:"+value);
			 dc.set("$.RESPONSE", (Object)value);
				 LogManager.logInfo("Setting value to dc:"+dc.toString());
		}
		else
		{
	    LogManager.logInfo("Value to be set to dc:"+value);
		dc.set(target,(Object)value);			
		LogManager.logInfo("Setting value to dc:"+dc.toString());
		}
		return true;
	}
	else if ( target.startsWith("$SYSTEM.")) {
		String[] vars = target.split("\\.");		
		System.setProperty(vars[1], value);
		return true;
	}
	
	return false;
	
	
}

public static String resolveSource(String value,HashMap varmap,Object req) {
	String rv = "";
	LogManager.logDebug("Resolving source value:"+value );
	if (value.contains("$VAR"))
	{
	  String key = value.substring(5);		      	      
      rv = (String)varmap.get(key);
      return rv;
	}
	else if (value.startsWith("$.")){
		if (value.equalsIgnoreCase("$.")) {
			 rv = req.toString();
		}
		else
		{
		    rv = JsonPath.read(req,value );
		}
		return rv;
	}
	else if ( value.startsWith("$SYSTEM.")) {
		
		String[] vars = value.split("\\.");
		LogManager.logDebug("Looking for System Property:"+vars[1]);
		rv = (String)getGlobalProp(vars[1]);
	}
	else
		rv = value;
	
	return rv;
	
	
}

public static String getGlobalProp(String key) {
	String value = null;
	if (initSystemProps())
	{
		
		value= runtimeProperties.getProperty(key);
		LogManager.logDebug("Global Property:"+key + "["+value+"]");
	}
	return value;
}

public static boolean initSystemProps(){
	if ( runtimeProperties == null) {
		LogManager.logInfo("Initialising global properties");
		runtimeProperties = System.getProperties();
		Map envVariables = System.getenv();
		Set keys = envVariables.keySet();
		Iterator iter = keys.iterator();
		while ( iter.hasNext()) {
			
			String key = (String)iter.next();
			runtimeProperties.setProperty(key, (String) envVariables.get(key));
			LogManager.logDebug("Setting env variable to Global props:"+key+"["+envVariables.get(key)+"]");
		}
	}
	
		
	return true;
}

public static HashMap prepareResponse(HashMap ctx,DocumentContext dc,String rv) {
	HashMap respMap = new HashMap();
	String override = (String)ctx.get("OVERRIDE_RESPONSE");
	if (  override != null && override.equalsIgnoreCase("true")) {
		respMap.put("RESPONSE", ctx.get("RESPONSE"));
		respMap.put("STATUS_CODE", Integer.parseInt((String)ctx.get("STATUS_CODE")));
		respMap.put("RESPONSE_HEADERS", (String)ctx.get("RESPONSE_HEADERS"));
	}
	else
	{
		respMap.put("RESPONSE", rv);
		respMap.put("STATUS_CODE", 200);
		
	}
	return respMap;
}



	
}
