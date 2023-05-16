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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.jayway.jsonpath.DocumentContext;

import saralarchan.org.adapter.db.DBAdapter;
import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

public class SetVarsFromRsProcessor extends GenericActionProcessor {

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
	  int row = 0;
	  //if (index > -1 )	
		//  row = index;
	 
	 String rsdetails = st.nextToken();
		StringTokenizer st1 = new StringTokenizer(rsdetails,"^");
		if ( st1.countTokens() == 3) {
			String rsname = st1.nextToken();
			String type = st1.nextToken();
			int position = Integer.parseInt((String)st1.nextToken());
			ResultSet rs = (ResultSet)varmap.get(rsname);
			try {
				rs.absolute(row);
				String value = DBAdapter.readValue(position, type, rs);
				CommonUtils.setTarget(target,varmap,dc,value);
				
			} catch (SQLException e) {
				LogManager.logError("Error Reading from result set", e);
			}			
			
		}
		
  }
}
