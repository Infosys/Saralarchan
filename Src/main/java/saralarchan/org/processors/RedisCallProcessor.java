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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import saralarchan.org.adapter.redis.RedisAdapter;
import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public class RedisCallProcessor extends GenericActionProcessor {

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
    // #REDIS supports SET/GET for a scalar key set or fetch and HSET/HGET for a hash setting or getting. HSET/HGET should specify the hashname with a ^
    // @ACTION:DO->$VAR.REDISRESP:REDISCALL:POOL1:SET:$VAR1.emailid
	  String hash = null;
		if ( st.countTokens() >= 3) {
			String poolname = st.nextToken();
			String ops = st.nextToken();
			String key = st.nextToken();
		
				if (ops.startsWith("HGET") || ops.startsWith("HSET")) {
					String[] toks = ops.split("^");
					hash = toks[1];
					ops = ops.substring(1,3);
				}
				
				Boolean rv = RedisAdapter.perform(poolname,ops,hash,key,varmap,dc,requestDoc);
				CommonUtils.setTarget(target,varmap,dc,rv.toString());
						
	}
  }
}
