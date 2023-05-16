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

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import saralarchan.org.adapter.db.QueryParser;

public class TestSqlParser {

	public static void main(String[] args) {
		
		 String selectSQL = "SELECT CI.ID, CI.NAME, C.COUNTRY, S.SALESDATE \n" +
	     "FROM CUSTOMERDB.CUSTOMERINFO CI,  CUSTOMERDB.COUNTRY C, CUSTOMERDB.SALES S " +
	     "WHERE  (CI.COUNTRYCODE='$.CUSTOMER.COUNTRYCODE'  " +
	     "OR CI.DESIGNATION='PPA')  OR ( C.COUNTRY!='$VAR.country' AND CI.XYZ IN('A','B','C')  OR CI.XYZ LIKE '%DUMMY');" ;
		 System.out.println(System.currentTimeMillis()+":"+selectSQL);
		 HashMap ctx = new HashMap();
         ctx.put("$.CUSTOMER.COUNTRYCODE", "IN");
         ctx.put("$VAR.country", "INDIA");
		 try {
			HashMap map = QueryParser.prepareSelect(ctx, selectSQL);
			Iterator<String> keyIter = map.keySet().iterator();
			while (keyIter.hasNext()) {
				String key = (String) keyIter.next();
				//System.out.println(key);
				selectSQL = selectSQL.replaceAll(Pattern.quote(key), (String) map.get(key));
			}
			System.out.println(System.currentTimeMillis()+":"+selectSQL);
			selectSQL = "INSERT INTO CUSTOMER (COUNTRY_CODE,B,country) VALUES ($.CUSTOMER.COUNTRYCODE,1,'$VAR.country');";
			map = QueryParser.prepareInsert(ctx, selectSQL);
			keyIter = map.keySet().iterator();
			while (keyIter.hasNext()) {
				String key = (String) keyIter.next();
				//System.out.println(key);
				selectSQL = selectSQL.replaceAll(Pattern.quote(key), (String) map.get(key));
			}
			System.out.println(selectSQL);
			
			selectSQL = "UPDATE CUSTOMER SET COUNTRY_CODE=$.CUSTOMER.COUNTRYCODE , country=$VAR.country where A='XYZ'";
			System.out.println(selectSQL);
			map = QueryParser.parseUpdate(ctx, selectSQL);
			keyIter = map.keySet().iterator();
		
			while (keyIter.hasNext()) {
				String key = (String) keyIter.next();
				
				selectSQL = selectSQL.replaceAll(Pattern.quote(key), (String) map.get(key));
			}
			System.out.println(selectSQL);
			
			selectSQL = "DELETE FROM  CUSTOMER  where COUNTERY_CODE='$.CUSTOMER.COUNTRYCODE' AND country='$VAR.country'";
			System.out.println(selectSQL);
			map = QueryParser.parseDelete(ctx, selectSQL);
			keyIter = map.keySet().iterator();
		
			while (keyIter.hasNext()) {
				String key = (String) keyIter.next();
				
				selectSQL = selectSQL.replaceAll(Pattern.quote(key), (String) map.get(key));
			}
			System.out.println(selectSQL);
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

}
