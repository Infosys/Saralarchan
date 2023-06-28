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

import java.util.HashMap;
import java.util.StringTokenizer;

import com.jayway.jsonpath.DocumentContext;

import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public class SetVarProcessor extends GenericActionProcessor {

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
	  String value = st.nextToken();
		value = CommonUtils.resolveSource(value,varmap,requestDoc);
		CommonUtils.setTarget(target,varmap,dc,value);	
		LogManager.logInfo("Local Variables defined "+varmap.toString());	
  }
}
