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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import saralarchan.org.adapter.rest.RestAdapterInfo;
import saralarchan.org.core.BaseRequestProcessor;
import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public class RestCallProcessor extends GenericActionProcessor {

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {

	  String specFile = st.nextToken();
		HashMap spec = RestAdapterInfo.loadTransformer(specFile);
		String methodtype = RestAdapterInfo.getMethod(specFile);
		DocumentContext reqDc=JsonPath.parse(requestDoc);
		String response = null;
		ArrayList transformer = (ArrayList)spec.get("@TRANSFORM");
		switch( methodtype)
		{		
		case "POST":			
			response = RestAdapterInfo.sendPOST(specFile, varmap, reqDc);
			break;
		case "GET":
			response = RestAdapterInfo.doGet(specFile, varmap,reqDc);			
			break;			
		}
		CommonUtils.setTarget(target,varmap,dc,response);
		LogManager.logInfo("Before response processing:"+dc.jsonString());
		BaseRequestProcessor.process(transformer,varmap,dc,response,"-1");
		LogManager.logInfo("After response processing:"+dc.jsonString());

  }
}
