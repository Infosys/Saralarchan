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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import saralarchan.org.logger.LogManager;

public class DBAdapter {
	
    private static HashMap dsMap = new HashMap();

   

    private DBAdapter() {}
    public static synchronized  HikariDataSource  initPool(String poolname) throws SQLException {
    	HikariDataSource ds = null;   	
    	String dbpropFile = poolname+".properties";
    	LogManager.logInfo(dbpropFile);
    	Properties prop = new Properties();
    	try {
    	//InputStream is = ClassLoader.class.getResourceAsStream(dbpropFile);
    	InputStream is=DBAdapter.class.getClassLoader().getResourceAsStream(dbpropFile);
    	prop.load(is);
    	HikariConfig config = new HikariConfig();   	
    	config.setJdbcUrl(prop.getProperty("JdbcUrl"));
    	config.setUsername(prop.getProperty("username"));
    	config.setPassword(prop.getProperty("username") );
        config.addDataSourceProperty( "cachePrepStmts" , prop.getProperty("cachePrepStmts") );
        config.addDataSourceProperty( "prepStmtCacheSize" , Integer.parseInt(prop.getProperty("prepStmtCacheSize")) );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , Integer.parseInt(prop.getProperty("prepStmtCacheSqlLimit")));
        config.addDataSourceProperty("autoCommit",prop.getProperty("autoCommit"));
        config.addDataSourceProperty("connectionTimeout",Integer.parseInt(prop.getProperty("connectionTimeout")));
        config.addDataSourceProperty("idleTimeout",Integer.parseInt(prop.getProperty("idleTimeout")));
        config.addDataSourceProperty("maxLifetime",Integer.parseInt(prop.getProperty("maxLifetime")));
        config.addDataSourceProperty("connectionTestQuery",Boolean.parseBoolean(prop.getProperty("connectionTestQuery")));
        config.addDataSourceProperty("connectionInitSql",prop.getProperty("connectionInitSql"));
        config.addDataSourceProperty("validationTimeout",Integer.parseInt(prop.getProperty("validationTimeout")));
        config.addDataSourceProperty("maximumPoolSize",Integer.parseInt(prop.getProperty("maximumPoolSize")));
        config.addDataSourceProperty("poolName",prop.getProperty("poolName"));
        config.addDataSourceProperty("allowPoolSuspension",prop.getProperty("allowPoolSuspension"));    
        config.addDataSourceProperty("leakDetectionThreshold",Integer.parseInt(prop.getProperty("leakDetectionThreshold")));
        ds = new HikariDataSource( config );
        dsMap.put(poolname, ds);    	
    	}
    	catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        
    }
		return ds;
    	        
    }
    public static Connection getConnection(String poolname) throws SQLException {
    	HikariDataSource ds = (HikariDataSource) dsMap.get(poolname);
    	if (ds == null) {
    		ds = initPool(poolname);
    	}
        return ds.getConnection();
    }
    
    public static ResultSet executeQuery(String sql,String poolname) { 
    	LogManager.logInfo("SQL to execute"+sql);
		PreparedStatement pst = null;
		ResultSet rs = null;
		Connection conn = null;
    	try {    		
    		conn = getConnection(poolname);
			pst = conn.prepareStatement(sql);
		    boolean rv = pst.execute();
		    if ( rv == true) {		    	
		    rs = pst.getResultSet();		   
		    }

	        } catch (SQLException ex) {
	            LogManager.logError("Query Execution Error", ex);	           
	        } 
		
    	 return rs;
    }
    
    public static boolean executeUpdate(String sql,String poolname) { 
    	LogManager.logInfo("update sql="+sql);
		PreparedStatement pst = null;
		boolean  rv = false;;
		Connection conn = null;
    	try {    		
    		conn = getConnection(poolname);
			pst = conn.prepareStatement(sql);
		    pst.execute();
		    if ( pst.getUpdateCount() > 0)
		    	rv = true;

	        } catch (SQLException ex) {
	            LogManager.logError("DML  Execution Error", ex);	           
	        } 
		
    	 return rv;
    }
    
    public static String readValue(int position,String type ,ResultSet r) {
    	String value = null;
    	try {
    	switch(type) {
    	case "INT": value = String.valueOf(r.getInt(position));
    	break;
    	case "LONG": value = String.valueOf(r.getLong(position));
    	break;
    	case "DOUBLE": value = String.valueOf(r.getDouble(position));
    	break;
    	case "FLOAT" : value = String.valueOf(r.getFloat(position));
    	break;
    	case "STRING" : value = String.valueOf(r.getString(position));
    	break;
    	case "DATE" : value = String.valueOf(r.getDate(position));
    	break;
    	case "TIMESTAMP" : value = String.valueOf(r.getTimestamp(position));
    	break;
    	case "BOOL" : value = String.valueOf(r.getBoolean(position));
    	default:
    		value=String.valueOf(r.getString(position));
    	}
    	
    }catch (Exception e) {
    	LogManager.logError("Could not get value from resultset", e);
    	return null;
    }
		return value;
    
    
    }
   
}
