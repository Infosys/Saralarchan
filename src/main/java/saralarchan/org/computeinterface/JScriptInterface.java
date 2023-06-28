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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import saralarchan.org.logger.LogManager;

public class JScriptInterface {
	
     
     // Reads a script file and evaluates all javascrit functions from it
	public static void loadScript(Context engine, String filename) {
		try {
			String scriptcontent = new String(Files.readAllBytes(Paths.get(filename)));
			//File f = new File(filename);
		
			//FileReader fread = new FileReader(f);
			
	
			engine.eval("js",scriptcontent);
			
			//fread.close();
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	public static String invokeFunction(Context inv, String function,
			Object... arguments) {
		String  rv = "";
		try {
			LogManager.logInfo("Function is "+function +" and arguments" + arguments.toString());
			Value func = inv.getBindings("js").getMember(function);
			rv = (String) func.execute(arguments).asString();
	
		}
		catch(Exception e) {
			LogManager.logError("Error executing function "+function+" in javascript",e);
		}
		return rv;
		
	}

	public static void invokeObjectMethod(Invocable inv, String objectName,
			String methodName, Object... arguments) {
		ScriptEngine eng = (ScriptEngine) inv;
		Object obj = eng.get(objectName);

		try {
			inv.invokeMethod(obj, methodName, arguments);
		} catch (NoSuchMethodException | ScriptException e) {
			System.out.println("Exception in script " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static void clearScriptContext(ScriptEngine eng, int scope) {
		Bindings bind = eng.getBindings(scope);
		bind.clear();
		System.out.println(bind.size());
	}

	public static void listBindings(ScriptEngine eng, int scope) {
		Bindings bind = eng.getBindings(scope);
		Iterator iter = bind.keySet().iterator();
		while (iter.hasNext()) {
			Object obj = bind.get(iter.next());
			System.out.println(obj.toString());
		}
		System.out.println(bind.size());
	}
	
	public static void setTranAttribute(ScriptEngine eng, String key,Object value){
        HashMap trhold = (HashMap)eng.getContext().getAttribute("TRANHOLDER");
        if (trhold == null){
        	trhold = new HashMap();
        }
        trhold.put(key, value);
		eng.getContext().setAttribute("TRANHOLDER", trhold, ScriptContext.ENGINE_SCOPE);
		
	}
	public static void clearTran(ScriptEngine eng){
		HashMap trhold = (HashMap) eng.getContext().getAttribute("TRANHOLDER", ScriptContext.ENGINE_SCOPE);
		if (trhold != null){
			trhold.clear();
		}
		
	}
	public static void setInstanceAttribute(ScriptEngine eng, String key,Object value){
		HashMap inst = (HashMap)eng.getContext().getAttribute("INSTANCE");
		if ( inst == null){
			inst = new HashMap();
		}
		inst.put(key, value);
		eng.getContext().setAttribute("INSTANCE", inst, ScriptContext.ENGINE_SCOPE);
		
	}
	public static void clearInstance(ScriptEngine eng){
		HashMap trhold = (HashMap) eng.getContext().getAttribute("TRANHOLDER", ScriptContext.ENGINE_SCOPE);
		if (trhold != null){
			trhold.clear();
		}
		HashMap inst = (HashMap) eng.getContext().getAttribute("INSTANCE", ScriptContext.ENGINE_SCOPE);
		if (inst != null){
			inst.clear();
		}
	}
	public static Object getInstanceAttribute(ScriptEngine eng, String key){
		Object obj = null;
		HashMap inst = (HashMap) eng.getContext().getAttribute("INSTANCE", ScriptContext.ENGINE_SCOPE);
		if (inst != null){
			 obj = inst.get(key);
		}
		return obj;
		
	}
	public static Object getTranAttribute(ScriptEngine eng, String key){
		Object obj = null;
		HashMap trhold = (HashMap) eng.getContext().getAttribute("TRANHOLDER", ScriptContext.ENGINE_SCOPE);
		if (trhold != null){
			 obj = trhold.get(key);
		}
		return obj;
	}
	
	public static void setBinding(ScriptEngine eng, Bindings bind,String var,String value){
		bind.put(var, value);
		
	}
	
}
