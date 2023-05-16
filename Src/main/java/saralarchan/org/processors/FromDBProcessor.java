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
import java.util.HashMap;
import java.util.StringTokenizer;

import com.jayway.jsonpath.DocumentContext;

import saralarchan.org.adapter.db.DBAdapter;
import saralarchan.org.adapter.db.QueryParser;
import saralarchan.org.logger.LogManager;

public class FromDBProcessor extends GenericActionProcessor {

  @Override
  public void handle(StringTokenizer st, String target, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
    //FROMDB:POOLNAME:INSTANCENAME:INSERT/SELECT/UPDATE/DELETE:<sqluery>
	//FROMDB:POOLNAME:INSTANCENAME:INSERT/SELECT/UPDATE/DELETE:<sqluery>
			String[] rsvar = target.split("\\.");
			HashMap rsMap = (HashMap) varmap.get(rsvar[1]);		
			String poolname = st.nextToken();
			String key = st.nextToken();
			String dbaction = st.nextToken();
			String sqlQuery = st.nextToken();
			HashMap replacemetData = null;
			Boolean rv=false;
			switch(dbaction) {
			case "SELECT":
				replacemetData = QueryParser.prepareSelect(varmap, sqlQuery);
				sqlQuery = QueryParser.prepareSQL(replacemetData, sqlQuery);
				ResultSet rs = DBAdapter.executeQuery(sqlQuery,poolname);
				try {
				rs.next(); 
				LogManager.logInfo(rs.getString(1));
				rs.previous();
				} catch (Exception e) {}
				if ( rsMap == null) {
					rsMap = new HashMap();
				}
				
				rsMap.put(key, rs);
				
				varmap.put(rsvar[1], rsMap);			
			break;
			case "INSERT":
				replacemetData = QueryParser.prepareInsert(varmap, sqlQuery);
				sqlQuery = QueryParser.prepareSQL(replacemetData, sqlQuery);
				varmap.put(key,DBAdapter.executeUpdate(sqlQuery,poolname));
			break;
			case "UPDATE":
				replacemetData = QueryParser.parseUpdate(varmap, sqlQuery);
				sqlQuery = QueryParser.prepareSQL(replacemetData, sqlQuery);
				rv = DBAdapter.executeUpdate(sqlQuery,poolname);
				varmap.put(key,rv.toString());
			break;
			case "DELETE":
				replacemetData =  QueryParser.parseDelete(varmap, sqlQuery);
				sqlQuery = QueryParser.prepareSQL(replacemetData, sqlQuery);
				rv = DBAdapter.executeUpdate(sqlQuery,poolname);
				varmap.put(key,rv.toString());
			break;
			default:
			}
			
			LogManager.logDebug("Local Variables defined "+varmap.toString());	
  }
}
