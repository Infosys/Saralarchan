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

// Sample  class

public class Human {
	public String sex= "";
	public int age= 18;
	
	public String grow(){
		System.out.println("I am growing");
		return "I grew";
	}
	public String learn(){
		System.out.println("I am learning");
		return "I learnt";
	}
	
	public void getAge(){
		System.out.println("AGE is " + age);
	}
	
	public void setAge(int hisage){
		age = hisage; 
	}
}
