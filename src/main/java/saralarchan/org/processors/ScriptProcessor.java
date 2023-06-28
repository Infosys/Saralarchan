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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.graalvm.polyglot.Context;

import com.jayway.jsonpath.DocumentContext;

import saralarchan.org.computeinterface.JScriptInterface;
import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public class ScriptProcessor extends GenericActionProcessor {

  protected static Context engine = null;

  static {
  
    Context context = Context.create();
    JScriptInterface.loadScript(context, "scriptLib.js");
    engine = context;
  }

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
    // $VAR.cond1=SCRIPT:check:arg1^arge2^arg3
    //String returnvar = st.nextToken();
	
			String methodname = st.nextToken();					
			String args = st.nextToken();
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
				val = CommonUtils.resolveSource(val,varmap,requestDoc);	
				arglist.set(x, val);			
			}
			LogManager.logInfo("ARGS"+arglist);
			String value = invokeScript(methodname, arglist);
			CommonUtils.setTarget(target,varmap,dc,value);
			
							
  }

  protected static String invokeScript(String func, ArrayList args) {
	if ( engine == null) {
		LogManager.logInfo("Javascriptg Engine not initialised");
		return "";
	}
	 
  	return JScriptInterface.invokeFunction(engine, func, args.toArray());
  }
}
