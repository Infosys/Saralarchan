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
package saralarchan.org.tester;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.graalvm.polyglot.Context;

import saralarchan.org.computeinterface.JScriptInterface;

// This code is retained for testing javascript in older versions of SDK : prior to jdk 11
public class TestScript {

	public static void main(String[] args) throws Exception {
		//Java hands of a scripting engine handle vai its ecript enggine manager
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		Context context = Context.create();
		JScriptInterface.loadScript(context, "trial.js");
		//Bindings bindings = sc.getBindings(ScriptContext.ENGINE_SCOPE);	
		
		//JScriptInterface.setBinding(engine, bindings, "a", "6.5");
		//JScriptInterface.setBinding(engine, bindings, "b", "7.2");
		Invocable inv1 = (Invocable) engine;
		Boolean cond = (Boolean) inv1.invokeFunction("matchString","Horse","horse");
		System.out.println("RV: "+cond);
	
		
		//Invocable inv = (Invocable) engine;
		//JScriptInterface.invokeFunction(inv, "simpleInt", "1000","8","6");
		/* engine.getContext().setAttribute("javainterface", new JScriptInterface(), ScriptContext.GLOBAL_SCOPE);
		engine.getContext().setAttribute("engine", engine, ScriptContext.GLOBAL_SCOPE);
		putInfoInInstance(engine);
	
		System.out.println("before clearing");
		JScriptInterface.invokeFunction(inv1, "helloTran", "This is a javascript call",
				"This is argument 2", "This is argument 3");
		JScriptInterface.clearTran(engine);
		
		System.out.println("after clearing");
		//JScriptInterface.invokeFunction(inv1, "helloInstance", "This is a javascript call",
			//	"This is argument 2", "This is argument 3");
		JScriptInterface.invokeFunction(inv1, "helloInstance", "This is a javascript call",
				"This is argument 2", "This is argument 3");
		//JScriptInterface.listBindings(engine,ScriptContext.ENGINE_SCOPE);
		//JScriptInterface.clearScriptContext(engine, ScriptContext.ENGINE_SCOPE);
        */
	}
	
	private static void  putInfoInInstance(ScriptEngine eng){
		
		JScriptInterface.setInstanceAttribute(eng, "KEY1", "TRIAL VALUE");
		JScriptInterface.setInstanceAttribute(eng, "KEY2", "KEY 2 is FANTASTIC");
		
		Human person = new Human();
		person.age = 65;
		person.sex = "Male";
		JScriptInterface.setInstanceAttribute(eng, "aperson",person);

		Human person2 = new Human();
		person2.age = 61;
		person2.sex = "Female";
		JScriptInterface.setInstanceAttribute(eng,"nextperson", person2);
		
		
	}
	
	
private static void  putInfoInTran(ScriptEngine eng){
		
		JScriptInterface.setTranAttribute(eng, "KEY1", "TRIAL VALUE");
		JScriptInterface.setTranAttribute(eng, "KEY2", "KEY 2 is FANTASTIC");
		
		Human person = new Human();
		person.age = 65;
		person.sex = "Male";
		JScriptInterface.setTranAttribute(eng, "aperson",person);

		Human person2 = new Human();
		person2.age = 61;
		person2.sex = "Female";
		JScriptInterface.setTranAttribute(eng,"nextperson", person2);
		
		
	}
	
     
}
