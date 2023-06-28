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
package saralarchan.org.functions;
import java.util.ArrayList;
import java.util.HashMap;

import com.jayway.jsonpath.DocumentContext;


import net.minidev.json.JSONArray;
import saralarchan.org.computeinterface.JavaFunction;

import saralarchan.org.logger.LogManager;


public class TestClass extends JavaFunction {

	public TestClass() {
		super();
		
	}
	
    public String Function1(ArrayList args,String target,HashMap ctx,DocumentContext dc,Object req,String index) {
    	
		  LogManager.logInfo("argument list size:" + args.size());
		  LogManager.logInfo("Response object so far:" +dc.jsonString()); 
		  LogManager.logInfo("Context variables are:" +ctx.toString());	  
		  JSONArray msisdn = dc.read("$.RESPONSE.BANs[*].Lines[?(@.isPrimary==true)].MSISDN");
		  JSONArray bansArray = dc.read("$.RESPONSE.BANs[*]"); JSONArray lines =
		  dc.read("RESPONSE.BANs[*].Lines[*]"); String primaryContactNumber =
		  dc.read("$.RESPONSE.primaryContactNumber"); String preferredName =
		  dc.read("$.RESPONSE.name"); 
		  String b2cguid = dc.read("RESPONSE.B2C_GUID"); 
		  boolean isCobrand = dc.read("RESPONSE.isCobrand"); 
		  String primaryEmailAddress =dc.read("$.RESPONSE.primaryEmailAddress"); 
		  JSONArray lineprovisioningStatusList = dc.read("$.RESPONSE.BANs[*].Lines[*].provisioningStatus");
		  
		  LogManager.logInfo(msisdn +":" + primaryContactNumber + ":" + preferredName + ":" + b2cguid + ":" +isCobrand + ":" + primaryEmailAddress); 
		  LogManager.logInfo("BANs:" +bansArray.toJSONString()); 
		  LogManager.logInfo("Lines:" +lines.toJSONString());
		  LogManager.logInfo("provisioningStatus:" +lineprovisioningStatusList.toJSONString());
		return "DUMMY";
    	
    }

	
}
