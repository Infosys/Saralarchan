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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;

import org.springframework.stereotype.Service;

import saralarchan.org.logger.LogManager;


@Service
public class ProductService implements IProductService{
	
	
	private Properties hashTab;

	private String propFileName="resource.properties";
	
	private static final String packageName = "saralarchan.org.functions";
	
	public Properties getHashTab() {
		return hashTab;
	}
	public void setHashTab(Properties hashTab) {
		this.hashTab = hashTab;
	}
	

	public ProductService() {
		this.hashTab=new Properties();	
		try {
			loadProperties(this.hashTab);
		} catch (IOException e) {
			LogManager.logError("ProductService Init failed",e);
			
		}
		
	}
	public void loadProperties(Properties hashTable) throws IOException {
		
		ClassLoader loader = getClass().getClassLoader();
		
		try (InputStream resourceStream = loader.getResourceAsStream(this.propFileName)) {
            if (resourceStream == null) {
                System.out.println("Sorry, unable to find " + this.propFileName);
            }
            else {
            	hashTable.load(resourceStream);
            	LogManager.logInfo("Loaded templates");    				
            }
        }
		
	}
	
	public void saveProperties(ProductDto productDto) throws IOException{
		try (OutputStream output = new FileOutputStream(this.propFileName)) {

            this.hashTab.setProperty(productDto.getKey(),productDto.getData());
           
            this.hashTab.store(output, null);

            System.out.println(this.hashTab);

        } catch (IOException io) {
            io.printStackTrace();
        }
	}
	
	
	
	@Override
	public HashMap execute(String key, String request) {
		if ( key == "" || key == null) {
			return null;			
		}
		LogManager.logInfo("In ProductService with key:"+key);
		
		String responseTemplate = hashTab.getProperty(key);
		try {	
			if (hashTab.size() == 0) {
			        loadProperties(this.hashTab);
		    }		
			if ( responseTemplate != null) {
			Class<IRequestProcessor> cls = (Class<IRequestProcessor>) Class.forName(packageName+"."+key);			
			BaseRequestProcessor processor =  (BaseRequestProcessor) cls.newInstance();
			LogManager.logInfo("Got the processor handle");
			processor.currentMapping=key;
			return processor.execute(request,this.hashTab.getProperty(key));
			}
		} catch (ClassNotFoundException e) {
			LogManager.logInfo("Calling Default Processor for "+key);
			return new BaseRequestProcessor(key).execute(request, responseTemplate);		 
		} catch (IllegalAccessException e) {		
			LogManager.logError("Processor Class access error", e);
		} catch (InstantiationException e) {			
			LogManager.logError("Processor Class instantiation error", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogManager.logError("Processor Class IOException error", e);
		}
		
		
		return null;
	}
	
	
	
	
	
}
