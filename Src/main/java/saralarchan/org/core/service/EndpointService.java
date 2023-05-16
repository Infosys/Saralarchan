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
package saralarchan.org.core.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;

import org.springframework.stereotype.Service;

import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;



import saralarchan.org.logger.LogManager;


@Service
public class EndpointService {
	
	
    private static Hashtable<String,Object> beanMap = new Hashtable<String,Object>();
    public static HashMap paramTemplates = new HashMap(); 
	public void addMapping(RequestMappingHandlerMapping requestMappingHandlerMapping,RequestMappingInfo requestMappingInfo,String classname,String methodsignature,HashMap params) throws NoSuchMethodException {
		Object bean = getHandlerInstance(classname);
		String[] methodSig = methodsignature.split(",");
		Class[] clsArray = new Class[methodSig.length-1];
		String methodname = methodsignature;;
		for ( int i = 0; i < clsArray.length; i++) {
			try {
				if ( i == 0) {
					methodname = methodSig[0];
				}
				LogManager.logInfo("Argument specifier"+methodSig[i+1]);
				clsArray[i] = Class.forName((String)methodSig[i+1]);
			} catch (ClassNotFoundException e) {
				
				LogManager.logError("Incorect argument specified for mapped Controller class method",e);
			}
	   
		}
	    
			Method m =  bean.getClass().getDeclaredMethod(methodname,clsArray);
			requestMappingHandlerMapping.
	            registerMapping(requestMappingInfo, bean, m
	            );		
	    	paramTemplates.put(classname+"."+methodname,params);
		
	  }
	
	public Object getHandlerInstance(String beanName) {
		Object bean =  beanMap.get(beanName);
		if ( bean== null) {
			try {
				 bean =  Class.forName(beanName).newInstance();
				 beanMap.put(beanName, bean);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				
				LogManager.logError("Error getting bean instance", e);
			}
		}		
		return bean;	
	}
}