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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.yaml.snakeyaml.Yaml;

import saralarchan.org.core.service.EndpointService;
import saralarchan.org.logger.LogManager;

@Configuration
public class HandlerMappingConfig {

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Bean
	public HandlerMapping handleMapping() throws IOException {
		LogManager.logInfo("Setting up URLs");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("saralarchan.org.core.service");
		context.refresh();
		EndpointService epS = context.getBean(EndpointService.class);
		Map urlMap = loadUrlMap(epS);
		return null;
	}

	private Map<String, Object> loadUrlMap(EndpointService service) throws IOException {
		Yaml yaml = new Yaml();
		Map<String, Object> urlMap = new LinkedHashMap<>();
		Resource resource = resourceLoader.getResource("classpath:mappings.yml");
		Map<String, Map<String, Object>> mappings = yaml.load(resource.getInputStream());
		Set set = mappings.keySet();
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			LogManager.logInfo("Key from mappings:" + key);
			Map<String, Object> pathMap = mappings.get(key);

			for (String urls : pathMap.keySet()) {
				String[] urlparts = null;
				String newUrl = "";
				urlparts = urls.split("/");
				Map<String, String> mappingSpec = (Map<String, String>) pathMap.get(urls);
				LogManager.logInfo("Urls:" + urls + " and spec is :" + mappingSpec.toString());

				String produceString = null;
				String consumeString;
				String requestmethods = null;
				String controllerClassName = null;
				String controllerClassMethodName = null;
				String[] consumeParts = null;
				String[] produceParts = null;
				String[] reqMethods = null;
				ArrayList methods = null;
				HashMap params = new HashMap();
				String paramList = null;

				for (String details : mappingSpec.keySet()) {
					LogManager.logInfo("url mapping detail:" + details);
					switch (details) {

					case "SupportedMethods":
						requestmethods = mappingSpec.get(details);
						LogManager.logInfo("Methods:" + requestmethods);
						reqMethods = requestmethods.split(",");
						methods = checkSupportedMethods(reqMethods);
						LogManager.logInfo("Method list for url:" + urls + " is " + methods.toString());
						break;
					case "ControllerClass":
						controllerClassName = mappingSpec.get(details);
						LogManager.logInfo("Controller class  for url:" + urls + " is " + controllerClassName);
						break;
					case "ControllerMethodSignature":
						controllerClassMethodName = mappingSpec.get(details);
						LogManager.logInfo("Method  for url:" + urls + " is " + controllerClassMethodName);
						break;
					case "Consumes":
						consumeString = mappingSpec.get(details);
						if (consumeString != null)
							consumeParts = consumeString.split(",");
						for (int i = 0; i < consumeParts.length; i++) {
							LogManager.logInfo("Consumes spec  for url:" + urls + " is " + consumeParts[i]);
						}

						break;
					case "Produces":
						produceString = mappingSpec.get(details);
						if (produceString != null)
							produceParts = produceString.split(",");
						for (int i = 0; i < consumeParts.length; i++) {
							LogManager.logInfo("Produces spec  for url:" + urls + " is " + produceParts[i]);
						}
						break;
					default:
						break;
					}

				}

				for (int i = 0; i < urlparts.length; i++) {
					if (urlparts[i].startsWith("{") && urlparts[i].endsWith("}")) {
						String paramName = urlparts[i].substring(1, urlparts[i].length() - 1);
						params.put(i, paramName);
					}

				}

				RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(urls).consumes(consumeParts)
						.produces(produceParts).methods(RequestMethod.GET).build();

				try {
					service.addMapping(requestMappingHandlerMapping, requestMappingInfo, controllerClassName,
							controllerClassMethodName, params);
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} // Loop End for setting url detail to the map

		} // End of mapping spec

		return urlMap;
	}

	private ArrayList checkSupportedMethods(String[] reqMethods) {
		ArrayList<RequestMethod> methods = new ArrayList<RequestMethod>();
		for (int i = 0; i < reqMethods.length; i++) {
			LogManager.logInfo("Supported methods:" + reqMethods[i]);
			switch (reqMethods[i]) {

			case "GET":
				methods.add(RequestMethod.GET);
				break;
			case "POST":
				methods.add(RequestMethod.POST);
				break;
			case "OPTIONS":
				methods.add(RequestMethod.OPTIONS);
				break;
			case "PATCH":
				methods.add(RequestMethod.PATCH);
				break;
			case "PUT":
				methods.add(RequestMethod.PUT);
				break;
			case "HEAD":
				methods.add(RequestMethod.HEAD);
				break;

			}
		}
		LogManager.logInfo("returning method details:" + methods.toString());
		return methods;
	}

}