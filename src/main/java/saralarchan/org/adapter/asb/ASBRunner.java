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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saralarchan.org.logger.LogManager;

public class ASBRunner implements Runnable {
	private static Pattern pattern = Pattern.compile("\\{\"externalId\":.*\\}");
	private static final String packageName = "saralarchan.org.functions";

	    String mesg;
	    String ID;
	    long SEQ;

	 public ASBRunner(String msg,String id,long SeqNo)
	    {
	    	this.mesg = msg;
	    	this.ID=id;
	    	this.SEQ=SeqNo;
			//new Thread(this);

	    }



	    public void run()
	    {
	    try {


	    	    Matcher matcher =  pattern.matcher(mesg);
	    	    if (matcher.find()) {
	    	    	String matchKey = "REQNAME";

	    		LogManager.logInfo("RECEIVED MSG ID="+ ID+"|SEQ="+SEQ+"|MSG="+matchKey+"}");
	    		LogManager.logInfo(this.packageName+"."+matchKey);
		    	Class<ASBBaseRequestProcessor> proc = (Class<ASBBaseRequestProcessor>) Class.forName(this.packageName+"."+matchKey);
		    	ASBBaseRequestProcessor ip = proc.newInstance();
		    	String resp = ip.execute(mesg,matchKey);
		    	//String resp = ip.execute(matchKey,mesg);
				ip.extraInfo(resp);
	    	    }
	    }catch (Exception e) {
	    	LogManager.logError("Thread Encounterd error while processing ASB message", e);
	    }
	}
}
