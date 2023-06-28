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
package saralarchan.org.adapter.redis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import saralarchan.org.core.BaseRequestProcessor;
import saralarchan.org.core.CommonUtils;
import saralarchan.org.logger.LogManager;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
public class RedisAdapter {
//Jedis jedis = new Jedis();


public static HashMap redisMap = new HashMap();
public static boolean initPool(String poolname)  {
 	
	String redispropFile = poolname+".properties";
	LogManager.logInfo(redispropFile);
	Properties prop = new Properties();
	try {
	//InputStream is = ClassLoader.class.getResourceAsStream(dbpropFile);
	InputStream is=RedisAdapter.class.getClassLoader().getResourceAsStream(redispropFile);
	prop.load(is);
	String host = (String)prop.get("host");
	int port = Integer.parseInt((String)prop.get("port"));
	
	JedisPoolConfig poolConfig = buildPoolConfig(prop);
	
    JedisPool jedisPool = new JedisPool(poolConfig,host,port); 
  
	redisMap.put(poolname, jedisPool);
	    	
	}
	catch (FileNotFoundException e) {
        LogManager.logError("Redis Pool property file not found", e);
        return false;
    } catch (Exception e ) {
    	LogManager.logError("Redis Pool init Failure", e);
    	return false;
    }
    return true;
}
private static JedisPoolConfig buildPoolConfig(Properties config) {
	JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(Integer.parseInt(config.getProperty("MaxTotal")));
    poolConfig.setMaxIdle(Integer.parseInt(config.getProperty("MaxIdle")));
    poolConfig.setMinIdle(Integer.parseInt(config.getProperty("MinIdle")));
    poolConfig.setTestOnBorrow(Boolean.parseBoolean(config.getProperty("TestOnBorrow")));
    poolConfig.setTestOnReturn(Boolean.parseBoolean(config.getProperty("TestOnReturn")));
    poolConfig.setTestWhileIdle(Boolean.parseBoolean(config.getProperty("TestWhileIdle")));
    poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(Integer.parseInt(config.getProperty("MinEvictableIdleTimeMillis"))).toMillis());
    poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(Integer.parseInt(config.getProperty("TimeBetweenEvictionRunsMillis"))).toMillis());
    poolConfig.setNumTestsPerEvictionRun(Integer.parseInt(config.getProperty("NumTestsPerEvictionRun")));
    poolConfig.setBlockWhenExhausted(Boolean.parseBoolean(config.getProperty("BlockWhenExhausted")));
    return poolConfig;
}

public static Jedis 	getConnection(String poolName){
	JedisPool pool = (JedisPool) redisMap.get(poolName);
	if ( pool == null){
		boolean rv = initPool(poolName);
		if ( rv == false)
			return null;
		else
			pool = (JedisPool)redisMap.get(poolName);
	}
	try {
		Jedis jedis = pool.getResource();
	
		if (!jedis.isConnected())
			jedis.connect();
	return jedis;
	} catch(Exception e) {
		LogManager.logError("Connection Failure Error", e);
	} finally {
	}
	return null;
	
}

public static boolean setHash(String poolname,String hashname,HashMap map) {
	Jedis jedis;
	if((jedis =getConnection(poolname)) == null )
		return false;
	Set keyset = map.keySet();
	map.getClass().getCanonicalName();
	Iterator iter = keyset.iterator();
	while ( iter.hasNext()) {
		String key = (String)iter.next();
		jedis.hset(hashname, key, (String) map.get(key));
		
	}
	jedis.close();
	return true;
	
}

public static HashMap getHash(String poolname,String hashname) {
	Jedis jedis;
	if((jedis =getConnection(poolname)) == null )
		return null;
	
	HashMap map = (HashMap)jedis.hgetAll(hashname);	
	jedis.close();
	return map;
	}
	
public static void  queue(Jedis jedis,String queuename,String  serializedObject) {

	jedis.lpush(queuename,serializedObject);
	
	}

public static String dequeue(Jedis jedis,String queuename) {
	return  (String)jedis.rpop(queuename);
		 
}

public static boolean set(Jedis jedis,String key,String value) {
	jedis.set(key, value);
	return true;
}

public static String get(Jedis jedis,String key) {
	return jedis.get(key);
}


public static boolean perform(String poolname, String ops, String hash, String keyholder,HashMap ctx,DocumentContext dc,Object reqDoc) {
	boolean rv = false;
	String key = getFromContext(keyholder,ctx,reqDoc);
	LogManager.logInfo("Key is "+key + " and ops is "+ ops +" and hash is "+hash);
	if ( hash != null) {
		if (ops.equalsIgnoreCase("GET")) {
			HashMap map = getHash(poolname,hash);
			ctx.put(key,map);
			if ( map != null) {
				rv = true;
			}
			
		}
		else if ( ops.equalsIgnoreCase("SET")) {
			HashMap map = (HashMap)ctx.get(key);
			if ( map != null ) {
			setHash(poolname,hash,map);
			rv = true;
			}
		}
	} else 
		{
		Jedis conn = getConnection(poolname);
		if ( ops.equalsIgnoreCase("GET")) {			
			String value = (String)get(conn,key);
			if ( value != null) {
			LogManager.logInfo("got value:"+value +"from redis key:"+key);
			setInContext(keyholder,value,ctx,dc);
			rv = true;
			}
		}
		else if ( ops.equalsIgnoreCase("SET"))
		{
			String value = getFromContext(keyholder,ctx,dc);
			if ( value != null) {
			LogManager.logInfo("Setting value:"+value +"in key:"+key);
			set(conn,key,value);
			rv = true;
			}
		}
    }
	return rv;
	
}

public static String getFromContext(String key,  HashMap ctx,Object req) {
	try {
		key = CommonUtils.resolveSource(key,ctx,req);
	
	}catch(Exception e) {
	LogManager.logError("Error getting value from context", e);
	}
	return key;	
}


public static String preprocessKey(String keyholder) {
	if ( keyholder.startsWith("$VAR.")) {
		String[] toks = keyholder.split("\\.");
		return toks[1];
	}else if (keyholder.startsWith("$.")) {
	
		String[] toks = keyholder.split("\\.");
		return toks[toks.length-1];	
}
	return keyholder;
}

public static boolean setInContext(String key,String value,HashMap ctx,DocumentContext dc) {
	try {
		   //BaseRequestProcessor.setTarget(key, ctx, dc, value);
		
		  if ( key.startsWith("$VAR.")) { 
			  String[] toks = key.split("\\.");
		      LogManager.logInfo("Setting ctx variable:"+toks[1]+" with :"+value);
		      ctx.put(toks[1], value); } 
		  else if (key.startsWith("$.")) {
		       String ctxkey = key.substring(2);
		       ctx.put(ctxkey, value); }
		 
	}catch(Exception e) {
	LogManager.logError("Error setting value from Redis", e);
}
	
	return true;
}



}
