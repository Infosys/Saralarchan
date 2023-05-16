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
package saralarchan.org.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import saralarchan.org.logger.LogManager;


public class AdapterManager {
	 protected  void getMapping() {
		 HashMap mappings = new HashMap();
	    	ArrayList aList = new ArrayList();
	    	LogManager.logInfo("Loading adapter configuration");
	    	String mapFile = "adapters"+".conf";	   	
			try {
				LogManager.logInfo(System.getProperty("user.dir"));
				File f = new File(System.getProperty("user.dir")+"/"+mapFile);
				LogManager.logInfo("Adapter Mapping file to read is "+f.getAbsolutePath());
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
	                	mappings.put(this.getClass().getSimpleName(), aList);
	                	
	                }
	                else {
	                	mappings.put(this.getClass().getSimpleName(), aList);
	                }
				  
		    	
		    	fr.close();
		    	f = null;
		    	
			} catch (FileNotFoundException e1) {
				
				LogManager.logError("Could not Read Mapping File :",e1);
			} catch (IOException e) {
				LogManager.logError("Error while closing file reader :",e);
			}
			
	    }   
	    
}
