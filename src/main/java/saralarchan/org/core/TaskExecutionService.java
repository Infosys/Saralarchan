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
package saralarchan.org.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import saralarchan.org.logger.LogManager;

public class TaskExecutionService {
	public static ThreadPoolExecutor executorService = null;
    public static HashMap<String,HashMap> tasklist = new HashMap();
	private TaskExecutionService() {
	}

	public static synchronized ThreadPoolExecutor initPool(String threadpoolname) {
		// ThreadPoolExecutor(int corePoolSize,
		// int maximumPoolSize,
		// long keepAliveTime,
		// TimeUnit unit,
		// BlockingQueue<Runnable> workQueue,
		// ThreadFactory threadFactory,
		// RejectedExecutionHandler handler)  // Block when thread pool is full
		try {
			if (executorService == null) {
				String poolpropFile = threadpoolname + ".properties";
				LogManager.logInfo(poolpropFile);
				Properties prop = new Properties();
				InputStream is = TaskExecutionService.class.getClassLoader().getResourceAsStream(poolpropFile);

				prop.load(is);
				ThreadFactory tf = Executors.defaultThreadFactory();
				int corepoolsize = Integer.parseInt(prop.getProperty("CORETHREADPOOLSIZE"));
				int maxpoolsize = Integer.parseInt(prop.getProperty("MAXTHREADPOOLSIZE"));
				int keepAliveTime = Integer.parseInt(prop.getProperty("THREADIDLETIMEOUT"));
				int taskqueuedepth = Integer.parseInt(prop.getProperty("TASKQUEUEDEPTH"));
				ThreadPoolExecutor executorService = new ThreadPoolExecutor(corepoolsize, maxpoolsize, keepAliveTime, TimeUnit.SECONDS,
						new ArrayBlockingQueue<Runnable>(taskqueuedepth), tf, new ThreadPoolExecutor.CallerRunsPolicy());
			
			}
		} catch (IOException e) {			
			LogManager.logError("Problem initialising thread pool ", e);			
		}
		return executorService;

	}
	
	public static Future executeTask(SaralAsyncTask task) {
		
		Future atask =  executorService.submit(task);
		HashMap tasks = tasklist.get(task.taskgroup);
		if ( tasks ==null)
		{
			tasks = new HashMap();
		}
		
		tasks.put(task.taskname, atask);
		tasklist.put(task.taskgroup, tasks);
		return atask;
	}

}
