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
package saralarchan.org.computeinterface;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import saralarchan.org.logger.LogManager;

public abstract class JavaFunction implements JavaRunner {
	
	public  String call(HashMap args) {
		String rv = "";
	    String target = (String) args.get("target");
	    HashMap varmap = (HashMap) args.get("ctx");
	    Object reqDoc  = args.get("reqDoc");
		 DocumentContext dc  = (DocumentContext) args.get("dc");
		 String newdcString = dc.jsonString().
		 replaceAll("\\\\", "").
		 replaceAll("\\\"\\{", "{").
		 replaceAll("\\}\\\"", "}");
		DocumentContext sanitisedDc = JsonPath.parse(newdcString);
		String index = (String) args.get("index");
		String methodname = (String)args.get("methodname");
		ArrayList funcArgs = (ArrayList)args.get("ARGS");
		
		// target,varmap,dc,requestDoc,index
		
	    Class[] clsArray = new Class[6];
	    clsArray[0] = ArrayList.class;
	    clsArray[1] = String.class;
	    clsArray[2] = HashMap.class;
	    clsArray[3] = DocumentContext.class;    
	    clsArray[4] = Object.class;
	    clsArray[5] = String.class;
	   	   
		try {
			Method method = this.getClass().getDeclaredMethod(methodname, clsArray);			   
			rv =  (String) method.invoke(this,funcArgs,target,varmap,sanitisedDc,reqDoc,index);
			
		} catch (NoSuchMethodException | SecurityException e) {
			LogManager.logError("Java method invocation error- methodname:"+methodname+" with required args..."+clsArray.toString(), e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rv;
		
}
	
	
	
}
