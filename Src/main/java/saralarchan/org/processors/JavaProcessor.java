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
package saralarchan.org.processors;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.jayway.jsonpath.DocumentContext;

import saralarchan.org.computeinterface.JavaFunction;
import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public class JavaProcessor extends GenericActionProcessor {

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
    // $VAR.cond1=SCRIPT:check:arg1^arge2^arg3
		// $VAR.cond1=SCRIPT:check:arg1^arge2^arg3
		String classname = st.nextToken();
		String methodname = st.nextToken();					
		String args = st.nextToken();
		LogManager.logInfo("Java invocation details:"+classname+":"+methodname+":"+args);
		ArrayList arglist  = new ArrayList();
		if ( args.contains("^")) {
		StringTokenizer stargs = new StringTokenizer(args,"^");							
		while (stargs.hasMoreTokens()) {
			String token = stargs.nextToken();
			arglist.add(token);
		}}
		else
		{			
			arglist.add(args);			
		}
		int arrlen = arglist.size();
		for ( int x = 0; x < arrlen ; x++) {
			String val = (String)arglist.get(x);
			val=CommonUtils.resolveSource(val,varmap,requestDoc);
			
			arglist.set(x, val);
		}
			LogManager.logInfo("ARGS"+arglist);
			HashMap hm = new HashMap();
			
			hm.put("target", target);
			hm.put("ctx", varmap);
			hm.put("reqDoc", requestDoc);
			hm.put("dc", dc);
			hm.put("index", index);
			hm.put("methodname", methodname);
			hm.put("ARGS", arglist);
		
		String value = invokeJava(classname, hm);
		CommonUtils.setTarget(target,varmap,dc,value);
  }

  protected String invokeJava(String classname, HashMap args) {
	  String rv = "";
		try {					
		Class<?> c = Class.forName(classname);
		Constructor<?> cons = c.getConstructor();
		JavaFunction function = (JavaFunction)cons.newInstance();
		rv = function.call(args);		
		}
		catch (Exception e) {	
			LogManager.logError("Error executing Java function", e);
		}
		return rv;		
  }
}
