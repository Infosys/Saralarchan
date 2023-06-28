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

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import com.jayway.jsonpath.DocumentContext;

public class SaralAsyncTask implements Callable{
	public String taskname = "";
	public String taskgroup = "";
    public HashMap ctx = null;
    public DocumentContext dc = null;
   
    public String verb = null;
    public Object requestDoc = null;
    public String index = null;
    public Block block = null;
    
    
    //public String verb = "";
	public Block getBlock() {
		return block;
	}
	public void setBlock(Block block) {
		this.block = block;
	}
	public SaralAsyncTask(String taskname, String taskgroup) {
		super();
		this.taskname = taskname;
		this.taskgroup = Thread.currentThread().getId()+"-"+taskgroup;
		
		
	}
	public HashMap getCtx() {
		return ctx;
	}
	public void setCtx(HashMap ctx) {
		this.ctx = ctx;
	}
	public DocumentContext getDc() {
		return dc;
	}
	public void setDc(DocumentContext dc) {
		this.dc = dc;
	}
	

	public String getVerb() {
		return verb;
	}
	public void setVerb(String verb) {
		this.verb = verb;
	}
	public Object getRequestDoc() {
		return requestDoc;
	}
	public void setRequestDoc(Object requestDoc) {
		this.requestDoc = requestDoc;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getTaskname() {
		return taskname;
	}
	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}
	public String getTaskgroup() {
		return Thread.currentThread().getId()+"-"+taskgroup;
	}
	public void setTaskgroup(String taskgroup) {
		this.taskgroup = taskgroup;
	}
	
	@Override
	public Object call() throws Exception {
		
		if (verb != null) {
			
			HandlerUtil.handleAction(verb, ctx, dc, requestDoc, index);
		}
		else if (block != null){
			StringTokenizer st = new StringTokenizer("NOP",":");
			
			HandlerUtil.handleBlock(block.getLines(),
					block.getBlockname() ,block.getClause(),
					st, ctx, dc, requestDoc, block.lineno);
			
		}
		return true;
	}
	
    
    
}
