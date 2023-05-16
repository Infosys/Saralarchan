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
package saralarchan.org.adapter.asb;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import saralarchan.org.logger.LogManager;

public class MessagingServiceBus {
	static String connectionString="Endpoint=sb://zstest01.servicebus.windows.net/;SharedAccessKeyName=COMP1_BUS_TEST;SharedAccessKey=KeyValue****"; 

	static String queueName="saral.test.function1.order";
	 
    static ExecutorService executor = null;
    static int  threads = 100;
    static int engineOn  = 0;
    static long engineDelay = 250;
   
	static {
		String numofthreads = System.getProperty("MAXTHREADS");
		String tmpengineDelay = System.getProperty("ENGINEDELAY");
		if ( numofthreads != null ) {
			try {
				int tmp = Integer.parseInt(numofthreads);
				threads=tmp;
				LogManager.logInfo("Thread Runners for ASB set to"+threads);				
			}
			catch(Exception e)
			{
				threads = 100;
			}
		}
		if ( tmpengineDelay != null ) {
			try {
				long tmp = Long.parseLong(tmpengineDelay);
				engineDelay=tmp;
				LogManager.logInfo("Engine Delay for message pickup set to "+engineDelay);				
			}
			catch(Exception e)
			{
				engineDelay = 250;
			}
		}
	} 
    static void dummyStart() throws InterruptedException
	{
    	// Please ensure a System Property ASBENABLED=Y is set in the launch.sh script
    	if ( engineOn != 1) {
 	    LogManager.logInfo("Checking Messaging Engine Startup Required");
	    String flag = System.getProperty("ASBENABLED");
	     if ( (flag != null) && flag.equals("Y") ) {
	    	receiveMessages();
	    	engineOn = 1;
	    }
	    else
	    {
	    	LogManager.logInfo("No Messaging Engine configured");
	    }
    	}
	
	}  
    
	static void receiveMessages() throws InterruptedException
	{
	    CountDownLatch countdownLatch = new CountDownLatch(1);
	    ServiceBusProcessorClient processor = new ServiceBusClientBuilder()
	    	     .connectionString(connectionString)
	    	     .processor()
	    	     .queueName(queueName)
	    	     .processMessage(MessagingServiceBus::processMessage)
	    	     .processError(context -> processError(context, countdownLatch))
	    	     .buildProcessorClient();
	    processor.start();
	   
	    executor = Executors.newFixedThreadPool(threads);
	    LogManager.logInfo("Started the ASB Client processor");
	
	}  
	 
	
	private static void processMessage(ServiceBusReceivedMessageContext context)  {
	    ServiceBusReceivedMessage message = context.getMessage();
	    if ( message != null) {	   
	    	LogManager.logInfo("Received Message :" + message.getMessageId());
	    ASBRunner workerThread = new ASBRunner(message.getBody().toString().replaceAll("\\s", ""),message.getMessageId(),message.getSequenceNumber());	    
	    executor.execute(workerThread);	  
	    try {
			Thread.sleep(engineDelay);
		} catch (InterruptedException e) {
			
			LogManager.logError("Error while sleep call in processmessaage method (engineDelay): "+ engineDelay,e);
		}
	    }
	    
	}

	private static void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
	    String errmesg = String.format("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
	        context.getFullyQualifiedNamespace(), context.getEntityPath());
             LogManager.logError(errmesg,null);
	    if (!(context.getException() instanceof ServiceBusException)) {
	    	errmesg = String.format("Non-ServiceBusException occurred: %s%n", context.getException());
	    	 LogManager.logError(errmesg,null);
	        return;
	    }

	    ServiceBusException exception = (ServiceBusException) context.getException();
	    ServiceBusFailureReason reason = exception.getReason();

	    if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
	        || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
	        || reason == ServiceBusFailureReason.UNAUTHORIZED) {
	    	errmesg = String.format("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
	            reason, exception.getMessage());
	    	LogManager.logError(errmesg,null);
	        countdownLatch.countDown();
	    } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
	    	errmesg = String.format("Message lock lost for message: %s%n", context.getException());
	    	LogManager.logError(errmesg,null);
	    } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
	        try {
	            // Choosing an arbitrary amount of time to wait until trying again.
	            TimeUnit.SECONDS.sleep(1);
	        } catch (InterruptedException e) {
	            LogManager.logError("Unable to sleep for period of time",e);
	        }
	    } else {
	    	errmesg = String.format("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
	            reason, context.getException());
	    	LogManager.logError(errmesg,null);
	    }
	}

	
	
}

