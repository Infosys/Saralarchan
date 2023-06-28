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
package saralarchan.org.logger;

import org.apache.logging.log4j.Level;
///
import org.apache.logging.log4j.Logger;

import saralarchan.org.core.constants.GlobalConstants;




public class LogManager {


	private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(LogManager.class);
	final static Level INTERNAL = Level.forName("INTERNAL", 250);
	public LogManager(){

		super();
	}



	public static void logDebug(String message){

			LOGGER.debug(message);

	}
	public static void logInfo(String message){

		LOGGER.info(message);

}

	public static void logError(String message, Throwable errorObj ){
		LOGGER.error(message, errorObj);

	}



	public static void logInternal(String logLine) {
		StackTraceElement[] caller=new Throwable().getStackTrace();
		StackTraceElement callingClass = caller[1];
		String callerClassdtls = callingClass.getClassName()+"."+callingClass.getClassName()+"["+callingClass.getLineNumber()+"]";
		
		LOGGER.log(INTERNAL,callerClassdtls+GlobalConstants.LogLineSeperator+logLine);
		
	}
}

