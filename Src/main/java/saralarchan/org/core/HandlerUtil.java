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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import saralarchan.org.enums.Actions;
import saralarchan.org.logger.LogManager;
import saralarchan.org.processors.GenericActionProcessor;

public class HandlerUtil {

	public static int handleBlock(ArrayList lines, String blockname, String clause, StringTokenizer verbToken,
			HashMap varmap, DocumentContext dc, Object requestDoc, int currentlineno) {

		if (lines.get(currentlineno).equals("@BLOCK:" + blockname + ":END")) {
			return 1;
		}
		if (varmap.get(blockname) != null && varmap.get(blockname).equals("1")) {
			return seekblkend(lines, blockname, currentlineno);
		}
		if (clause == null || clause.equals("")) {
			return 1;
		}
		if (clause.equals("IF") || clause.equals("ELSIF")) {
			if (verbToken.countTokens() == 2) { // block is conditional

				Boolean cond = evaluateCond(verbToken, varmap, dc, requestDoc);
				if (cond == true) {
					varmap.put(blockname, "1");
					return 1;
				}

				else
					return seeknxtblksegment(lines, blockname, currentlineno + 1);
			}

		} else if (clause.equals("ELSE")) {
			return 1;
		} else if (clause.equals("END")) {
			return 1;
		}
		LogManager.logError("Block Statement should have one of IF/ELSEIF/ELSE/END clause:", null);
		return 1;
	}

	public static int handleAsync(ArrayList lines, int i, StringTokenizer controlToken,
			String verb,HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
		//@ASYNC:TASK1:GROUP1->BLOCK:BLK1
		String taskname = controlToken.nextToken();
		String taskgroup = controlToken.nextToken();
		SaralAsyncTask task = new SaralAsyncTask(taskname,taskgroup);
		task.setCtx(varmap);
		task.setDc(dc);
		task.setRequestDoc(requestDoc);
		task.setIndex(index);
		
		if ( verb.startsWith("BLOCK")) {
			String[] arr = verb.split(":");
			
			
			Block block = new Block();
			block.blockname = arr[1];
			block.clause = null;
			block.lineno = i+1;
			
			
			
		}
		else
		{		
			
			
			
		}
		
		
	    TaskExecutionService.executeTask(task);
	
	return 0;
	}
	public static int seekblkend(ArrayList lines, String blockname, int index) {

		String pattern = "@BLOCK:" + blockname + ":" + "END";

		int skippedlines = 1;
		while (!((String) lines.get(index)).startsWith(pattern)) {
			index++;
			skippedlines++;
		}
		return skippedlines + 1;

	}

	public static int seeknxtblksegment(ArrayList lines, String blockname, int index) {
		String pattern = "@BLOCK:" + blockname;
		int skippedlines = 1;
		while (!((String) lines.get(index)).startsWith(pattern)) {
			index++;
			skippedlines++;
		}
		return skippedlines;
	}

	public static String processReturn(String rvFrom, HashMap varmap, DocumentContext dc, Object requestDoc,
			String value) {
		switch (rvFrom) {
		case "CONTEXT":
			if (value.startsWith("$VAR.")) {
				return (String) varmap.get(value.substring(5));
			}
			break;
		case "DC":
			return dc.read(value);

		case "REQUEST":
			return JsonPath.read(requestDoc, value);

		default:

		}
		return "";
	}

	public static int handleRepeat(ArrayList lines, int i, StringTokenizer controltoken, String verb, HashMap ctx,
			DocumentContext dc, Object reqDoc, String index) {
		// @REPEAT:<noOfLines>:FOR/WHILE:($VAR.?/$./X)/(COND)->I:?(SCRIPT/JAVA:ARG1:ARG2..)
		boolean rv = true;
		int noOfLines = 1;
		try {
			noOfLines = Integer.parseInt(controltoken.nextToken());
			String looptype = controltoken.nextToken();

			StringTokenizer indexer = new StringTokenizer(verb, ":");
			String indexvar = indexer.nextToken();
			GenericActionProcessor proccessor = null;
			if (indexer.hasMoreTokens()) {
				String functype = indexer.nextToken();

				switch (functype) {
				case "SCRIPT":
					proccessor = Actions.valueOf(functype).getProcessor();
					break;
				case "JAVA":
					proccessor = Actions.valueOf(functype).getProcessor();
					break;
				default:
					ctx.put(indexer, Integer.parseInt(indexer.nextToken()));

				}

			}
			rv = handleRepeatHelper(lines, i, noOfLines, looptype, controltoken, ctx, dc, reqDoc, proccessor, indexvar);
		} catch (Exception e) {
			LogManager.logError("Repeat clause not defined properly", null);
			return lines.size();
		}
		if (rv == false) {
			LogManager.logError("Repeat clause not defined properly", null);
			return lines.size();
		}
		return noOfLines;

	}

	public static Boolean handleRepeatHelper(ArrayList<String> aList2, int i, int noOfLines, String looptype,
			StringTokenizer st, HashMap varmap, DocumentContext dc, Object requestDoc, GenericActionProcessor proc,
			String indexvar) {
		try {
			String loopController = st.nextToken();

			String controlvalue = null;
			ArrayList subList = new ArrayList();
			LogManager.logInfo("Handle Repeat called at line no " + i);
			for (int index = i + 1; index < i + 1 + noOfLines; index++) {
				subList.add(aList2.get(index));
			}
			LogManager.logInfo(subList.toString());
			if (st.hasMoreTokens()) {
				controlvalue = st.nextToken();
			}
			if (looptype.equalsIgnoreCase("FOR")) {
				String index = "";
				String val = CommonUtils.resolveSource(loopController, varmap, requestDoc);
				int times = Integer.parseInt(val);
				while (times > 0) {
					if (proc != null) {
						proc.handle(st, indexvar, varmap, dc, requestDoc, indexvar);
						BaseRequestProcessor.process(subList, varmap, dc, requestDoc, indexvar);
					} else {
						String ci = CommonUtils.resolveSource(indexvar, varmap, requestDoc);
						BaseRequestProcessor.process(subList, varmap, dc, requestDoc, ci);
						int k = Integer.parseInt(ci);
						k++;
						CommonUtils.setTarget(indexvar, varmap, dc, String.valueOf(k));
					}
					times--;
				}

			} else if (looptype.equalsIgnoreCase("WHILE")) {

				boolean cond = Condition(loopController, st.nextToken(), varmap, dc, requestDoc);
				while (cond == true) {
					if (proc != null) {
						proc.handle(st, indexvar, varmap, dc, requestDoc, indexvar);
						BaseRequestProcessor.process(subList, varmap, dc, requestDoc, indexvar);
					} else {
						String ci = CommonUtils.resolveSource(indexvar, varmap, requestDoc);
						BaseRequestProcessor.process(subList, varmap, dc, requestDoc, ci);
						int k = Integer.parseInt(ci);
						k++;
						CommonUtils.setTarget(indexvar, varmap, dc, String.valueOf(k));
					}
					cond = Condition(loopController, st.nextToken(), varmap, dc, requestDoc);
				}

			} else {
				return false;
			}
		} catch (Exception e) {
			LogManager.logError("Error handling Loop", e);
			return false;
		}

		return true;
	}

	public static void handleAction(String line, HashMap varmap, DocumentContext dc, Object requestDoc, String index) {
		LogManager.logInfo("Splitting " + line);
		String[] toks = line.split("\\|");
		String target = toks[0];
		// if (index > -1) {
		// target = target + "[" + index + "]";
		// }
		String src = toks[1];
		StringTokenizer st = new StringTokenizer(src, ":");
		String action = st.nextToken();
		LogManager.logInfo("Action called:" + action);

		Actions.valueOf(action).getProcessor().handle(st, target, varmap, dc, requestDoc, index);

	}

	public static Boolean evaluateCond(StringTokenizer st, HashMap varmap, DocumentContext dc, Object req) {
		LogManager.logDebug("Condition evaluator called");
		String pred = st.nextToken();
		String val = st.nextToken();
		LogManager.logInfo("predicate is:" + pred + " value is : " + val);
		Boolean evaluate = false;
		String resolvedPredicate = CommonUtils.resolveSource(pred, varmap, req);
		if (resolvedPredicate != null && resolvedPredicate.equals(val)) {
			evaluate = true;
			LogManager.logInfo("Codition evaluated to  true");
		}

		return evaluate;
	}

	public static Boolean Condition(String LS, String RS, HashMap varmap, DocumentContext dc, Object req) {
		LogManager.logDebug("Condition evaluator called");
		String pred = LS;
		String val = RS;
		LogManager.logInfo("predicate is:" + pred + " value is : " + val);
		Boolean evaluate = false;
		String resolvedPredicate = CommonUtils.resolveSource(pred, varmap, req);
		if (resolvedPredicate != null && resolvedPredicate.equals(val)) {
			evaluate = true;
			LogManager.logInfo("Codition evaluated to  true");
		}

		return evaluate;
	}

}
