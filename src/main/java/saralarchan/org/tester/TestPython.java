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

import java.util.ArrayList;

import javax.script.ScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import saralarchan.org.computeinterface.PyInterface;

// This code is retained for testing javascript in older versions of SDK : prior to jdk 11
public class TestPython {

	public static void main(String[] args) throws Exception {
		//Java hands of a scripting engine handle vai its ecript enggine manager
		
		    // 2 temporary workarounds for running truffle on windows - unsupported yet
		   // System.getProperties().put("python.home", "x");
		   // System.getProperties().put("regex.home", "x");
		  
		String venvExePath;
		try {
			
			 //ClassLoader loader = TestPython.class.getClassLoader();
			 //URL u =loader.getResource("venv/Scripts/python.cmd");
			 //System.out.println(u.toURI().toString());
			
			 //venvExePath = TestPython.class. getClassLoader().
			 //getResource(Paths.get("venv","bin", "graalpy").toString()). getPath();
			 Engine engine = Engine.newBuilder().build();
			 Context context = Context.newBuilder("python")
             .allowAllAccess(true)
             .engine(engine)
             .build();
			 //Context context = Context.newBuilder("python"). allowIO(true).
			 //option("python.Executable", u.toURI().toString()). build();
			 
			 //context.eval("python", "import site");
			 PyInterface.loadScript(context, "scriptLib.py");
				
				String func = "add_numbers" ;
				ArrayList params = new ArrayList();
				params.add(10);
				params.add(15);
				
				String result =  PyInterface.invokeFunction(context, func, params.toArray());
				System.out.println("RV: "+result);
				int num = Integer.parseInt(result);
				params.clear();
				params.add(num);
				result =  PyInterface.invokeFunction(context, "get_root", params.toArray());		
				System.out.println("RV: "+result);
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Context context = Context.create();
		

	}
	
	private static void  putInfoInInstance(ScriptEngine eng){
		
		PyInterface.setInstanceAttribute(eng, "KEY1", "TRIAL VALUE");
		PyInterface.setInstanceAttribute(eng, "KEY2", "KEY 2 is FANTASTIC");
		
		Human person = new Human();
		person.age = 65;
		person.sex = "Male";
		PyInterface.setInstanceAttribute(eng, "aperson",person);

		Human person2 = new Human();
		person2.age = 61;
		person2.sex = "Female";
		PyInterface.setInstanceAttribute(eng,"nextperson", person2);
		
		
	}
	
	
private static void  putInfoInTran(ScriptEngine eng){
		
	PyInterface.setTranAttribute(eng, "KEY1", "TRIAL VALUE");
	PyInterface.setTranAttribute(eng, "KEY2", "KEY 2 is FANTASTIC");
		
		Human person = new Human();
		person.age = 65;
		person.sex = "Male";
		PyInterface.setTranAttribute(eng, "aperson",person);

		Human person2 = new Human();
		person2.age = 61;
		person2.sex = "Female";
		PyInterface.setTranAttribute(eng,"nextperson", person2);
		
		
	}
	
     
}
