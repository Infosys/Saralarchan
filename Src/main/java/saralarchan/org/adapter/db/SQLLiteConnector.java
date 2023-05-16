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

package saralarchan.org.adapter.db;




import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import saralarchan.org.logger.LogManager;

public class SQLLiteConnector{
	private static SQLiteConnectionPoolDataSource connPool = null;
	public static  String hostname = null;
	public static String appName  = null;
	public static int runno = 0;
	
	public static Connection initialise(){
		//System.out.println("SQLLiteConnector Init called ");
		Connection con = null;
		if (connPool == null){
			connPool= new SQLiteConnectionPoolDataSource();		
			connPool.setUrl("jdbc:sqlite:DB.db");
			 org.sqlite.SQLiteConfig config = new org.sqlite.SQLiteConfig();
			 config.enforceForeignKeys(true);
			 config.enableLoadExtension(true);
			 
		
			 connPool.setConfig(config);
		}
		if ( connPool != null){
			try {
				 con = (Connection) connPool.getConnection();
				
			} catch (SQLException e) {
				LogManager.logError("Could not initialise the connection pool", e);
			}
		}
		//System.out.println("SQLLiteConnector returning connection");
		return con;
	}
	
public static void LogInfo(String appname, String locatorid,String narrative){
	    String dt=getDate();
	    String tm=getTime();
	    String host=getHost();
	 
	    long threadid = Thread.currentThread().getId();
	    Connection aCon = initialise();
	    try {
			aCon.setAutoCommit(false);
		} catch (SQLException e1) {
			
			e1.printStackTrace();
		}
        try {
        	
			 	
			PreparedStatement stmt = aCon.prepareStatement("INSERT INTO INFORMATION (narrative, logdate,logtime ,hostname, appname, locatorid, threadid) VALUES ('"+narrative+"','"+dt+"','"+tm+"','"+host+"','"+appname+"','"+locatorid+"',"+threadid+")") ; 	
			LogManager.logDebug(stmt.toString());  
			stmt.execute();
			stmt.close();
			aCon.commit();
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	 
}

public static void LogStat(String param,String value){
	PreparedStatement stmt = null;
    String dt=getDate();
    String tm=getTime();
    String host=getHost();
 
    int logpartno = runno;
    Connection aCon = initialise();
    try {
		aCon.setAutoCommit(false);
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    try {
    	
		//System.out.println("INSERT INTO SNAPSHOT (logpartno, logdate,logtime ,hostname,parametername,value) VALUES ("+logpartno+",'"+dt+"','"+tm+"','"+host+"','"+param+"',"+value+")");
		stmt = aCon.prepareStatement("INSERT INTO SNAPSHOT (logpartno, logdate,logtime ,hostname,parametername,value) VALUES ("+logpartno+",'"+dt+"','"+tm+"','"+host+"','"+param+"',"+value+")") ; 	
		//System.out.println(stmt.toString());  
		stmt.execute();
		stmt.close();
		aCon.commit();
		aCon.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		
	}
    
 
}

public static String getDate(){
	    Date date = Calendar.getInstance().getTime();
	    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
	    return sdf.format(date);
	}
	

public static String  getTime(){
	Date date = Calendar.getInstance().getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("hhmmss.SSS");
    return sdf.format(date);
	
}
public static String  getHost(){
	 if (hostname == null)
	 {

			try
			{
			    InetAddress addr;
			    addr = InetAddress.getLocalHost();
			    hostname = addr.getHostName();
			   
			}
			catch (UnknownHostException ex)
			{
			   LogManager.logError("Hostname can not be resolved", ex);
			}
	 }
	 return hostname;
	
}

public static String queryDB(String table,String colname,String condcol,String condval) {
	LogManager.logDebug(getTime());
	Connection con =  initialise();
	PreparedStatement statement;
	String response = null;
	//LogManager.logDebug("select "+colname+" from "+table+" where "+condcol+"="+condval);
	String selectQuery = "SELECT "+ colname + " from " + table + " where " + condcol + "=" + "'" + condval + "'";
	try {
		LogManager.logDebug(selectQuery);
		statement = con.prepareStatement(selectQuery);
		statement.setQueryTimeout(30);  // set timeout to 30 sec.
	
		ResultSet resultset = statement.executeQuery();

	     while(resultset.next())
	     {
	        response =  resultset.getString(colname);
	     }
	     statement.close();

	     con.close();
	     LogManager.logDebug(response);
	} catch (SQLException e) {
		
		LogManager.logError("SQL Exception while querying DB", e);
	}
	  return response;
}

}
