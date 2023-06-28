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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public abstract class GenericActionProcessor {

  abstract public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc,
      Object requestDoc, String index);

  protected String resolveSource(String value, Map varmap, Object req) {
    String rv = "";
    LogManager.logDebug("Resolving source value:" + value);
    if (value.contains("$VAR")) {
      String[] vars = value.split("\\.");
      rv = (String) varmap.get(vars[1]);
      return rv;
    } else if (value.startsWith("$.")) {
      if (value.equalsIgnoreCase("$.")) {
        rv = req.toString();
      } else {
        rv = JsonPath.read(req, value);
      }
      return rv;
    } else if (value.startsWith("$SYSTEM.")) {

      String[] vars = value.split("\\.");
      LogManager.logDebug("Looking for System Property:" + vars[1]);
      rv = (String) getGlobalProp(vars[1]);
    } else {
      rv = value;
    }
    return rv;
  }

  protected String getGlobalProp(String key) {
    String value = null;
    if (initSystemProps()) {

      value = CommonUtils.runtimeProperties.getProperty(key);
      LogManager.logDebug("Global Property:" + key + "[" + value + "]");
    }
    return value;
  }

  protected boolean initSystemProps() {
    if (CommonUtils.runtimeProperties == null) {
      LogManager.logInfo("Initialising global properties");
      CommonUtils.runtimeProperties = System.getProperties();
      Map envVariables = System.getenv();
      Set keys = envVariables.keySet();
      Iterator iter = keys.iterator();
      while (iter.hasNext()) {

        String key = (String) iter.next();
        CommonUtils.runtimeProperties.setProperty(key, (String) envVariables.get(key));
        LogManager.logDebug("Setting env variable to Global props:" + key + "[" + envVariables.get(key) + "]");
      }
    }
    return true;
  }

  protected boolean setTarget(String target, Map varmap, DocumentContext dc, String value) {
    boolean rv = false;
    if (target.contains("$VAR")) {
      String[] vars = target.split("\\.");
      varmap.put(vars[1], value);
      return true;
    } else if (target.startsWith("$.")) {
      if (target.equalsIgnoreCase("$.")) {
        dc = JsonPath.parse(value);
      } else {
        dc.set(target, value);
      }
      return true;
    } else if (target.startsWith("$SYSTEM.")) {
      String[] vars = target.split("\\.");

      System.setProperty(vars[1], value);
      return true;
    }
    return false;
  }

}
