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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import saralarchan.org.computeinterface.JScriptInterface;
import saralarchan.org.logger.LogManager;

import org.graalvm.polyglot.*;

public class BaseRequestProcessor implements IRequestProcessor {
	protected String suffix = "";

	protected static Hashtable<String, String> templateCache = new Hashtable<String, String>();
	protected static Hashtable<String, ArrayList> mappings = new Hashtable<String, ArrayList>();

	protected static ScriptEngine engine = null;

	protected String currentMapping = null;

	static {
		Context polyglot = Context.create();
		
		JScriptInterface.loadScript(polyglot, "scriptLib.js");
	}

	public BaseRequestProcessor(String mappingName) {
		this.currentMapping = mappingName;
	}

	public BaseRequestProcessor() {

	}

	protected void getMapping() {
		ArrayList aList = new ArrayList();
		LogManager.logInfo("Loading configuration");
		String mapFile = this.currentMapping + ".properties";
		try {
			LogManager.logInfo(System.getProperty("user.dir"));
			File f = new File(System.getProperty("user.dir") + "/" + mapFile);
			LogManager.logInfo("Mapping file to read is " + f.getAbsolutePath());
			FileReader fr;
			fr = new FileReader(f);
			BufferedReader bfr = new BufferedReader(fr);
			// ArrayList content = new ArrayList();
			String line;
			while ((line = bfr.readLine()) != null) {
				aList.add(line);

			}
			if (aList.isEmpty()) {
				LogManager.logInfo("No MAPPING found");
				aList.add("NO_MAP");
				mappings.put(this.currentMapping, aList);

			} else {
				mappings.put(this.currentMapping, aList);
			}

			fr.close();
			f = null;

		} catch (FileNotFoundException e1) {

			LogManager.logDebug("Mapping property file not defined..Will use default response");
			aList.add("NO_MAP");
			mappings.put(this.currentMapping, aList);
		} catch (IOException e) {
			LogManager.logError("Error while closing file reader :", e);
			aList.add("NO_MAP");
			mappings.put(this.currentMapping, aList);
		}

	}

	protected String checkCache(String responseTemplate) {
		String json = null;
		if (templateCache.get(responseTemplate) == null) {
			ClassLoader loader = getClass().getClassLoader();
			try (InputStream resourceStream = loader.getResourceAsStream(responseTemplate)) {
				if (resourceStream == null) {
					LogManager.logDebug("Sorry, unable to find " + responseTemplate);
				} else {
					int bufferSize = 1024;
					char[] buffer = new char[bufferSize];
					StringBuilder out = new StringBuilder();
					Reader in = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
					for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0;) {
						out.append(buffer, 0, numRead);
					}
					json = out.toString();
					templateCache.put(responseTemplate, json);
					LogManager.logInfo("Cached response template:" + responseTemplate);
					in.close();
				}

				resourceStream.close();
			} catch (IOException e) {

				LogManager.logError("Could not fetch template from cache", e);
			}

		} else {
			json = (String) templateCache.get(responseTemplate);
		}
		LogManager.logDebug("return json string from cache:" + json);
		return json;

	}

	public HashMap execute(String request, String responseTemplate) {
		if (this.suffix != "") {
			responseTemplate = responseTemplate + "-" + suffix;
		}
		LogManager.logInfo("ResponseTemplate is " + responseTemplate);
		HashMap response = null;
		LogManager.logInfo(request);
		Object requestDocument = Configuration.defaultConfiguration().jsonProvider().parse(request);
		LogManager.logInfo("Response template:" + checkCache(responseTemplate));

		DocumentContext dc = JsonPath.parse(checkCache(responseTemplate));

		return transform(dc, requestDocument);

	}

	protected HashMap transform(DocumentContext dc, Object requestDocument) {
		String rv = null;
		HashMap<String, String> localvar = new HashMap<String, String>();
		LogManager.logInfo("Trasnform called");
		ArrayList aList = (ArrayList) mappings.get(this.currentMapping);
		if (aList == null) {
			getMapping();
		}
		if (mappings.get(this.currentMapping) != null) {
			aList = (ArrayList<String>) mappings.get(this.currentMapping);
			if ((!aList.isEmpty()) && (aList.get(0) != "NO_MAP")) {
				rv = process(aList, localvar, dc, requestDocument, "-1");
				LogManager.logInfo("Final Transformed response:" + rv);
				

			}

		}

		if (rv == null) {
			rv = dc.jsonString();
		}

		return CommonUtils.prepareResponse(localvar, dc, rv);

	}

	protected String resppond(String request, String responseTemplate) {
		if (this.suffix != "") {
			responseTemplate = responseTemplate + "-" + suffix;
		}
		LogManager.logDebug("ResponseTemplate is " + responseTemplate);

		return checkCache(responseTemplate);

	}

	@Override
	public String extraInfo(String extra) {
		return " ";
	}

	public static String process(ArrayList lines, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
		boolean finished = false;
		String rv = null;
		for (int i = 0; i < lines.size(); i++) {
			String line = (String) lines.get(i);

			Boolean evaluate = true;
			if (line.startsWith("@")) {
				StringTokenizer controls = new StringTokenizer(line, "->");
				if (controls.countTokens() > 1) {
					String control = controls.nextToken();
					String verb = controls.nextToken();
					StringTokenizer controlToken = new StringTokenizer(control, ":");
					if (controlToken.hasMoreTokens()) {
						String command = controlToken.nextToken();

						LogManager.logInfo("command is" + command);
						switch (command) {
						case "@ACTION":

							HandlerUtil.handleAction(verb, varmap, dc, requestDoc, index);
							break;
						case "@CASE":
							Boolean cond = HandlerUtil.evaluateCond(controlToken, varmap, dc, requestDoc);
							LogManager.logInfo("Cond evaluated to" + cond);
							if (cond) {
								HandlerUtil.handleAction(verb, varmap, dc, requestDoc, index);
							}
							break;
						case "@REPEAT":

							int noOfLines = HandlerUtil.handleRepeat(lines, i, controlToken, verb, varmap, dc, requestDoc, index);
						
							i = i + noOfLines;
						

							break;
						case "@ASYNC":
							HandlerUtil.handleAsync(lines,i,controlToken,verb,varmap, dc, requestDoc, index);
							break;
						case "@BLOCK":
							StringTokenizer verbToken = new StringTokenizer(verb, ":");
							String blockname = controlToken.nextToken();
							LogManager.logInfo("Block Name:" + blockname);
							String clause = controlToken.nextToken();
							LogManager.logInfo("Clause Name:" + clause);
							int linecount = HandlerUtil.handleBlock(lines, blockname, clause, verbToken, varmap, dc, requestDoc, i);
							if (linecount > 0)
								i = i + linecount - 1;

							break;
						case "@RETURN":
							String rvFrom = controlToken.nextToken();
							rv = HandlerUtil.processReturn(rvFrom, varmap, dc, requestDoc, verb);
							finished = true;

							break;
						default:

						}
					}

				}
			}
			if (finished)
				break;
		}
		if (rv == null) {
			rv = dc.jsonString();
		}

		return rv;

	}

}
