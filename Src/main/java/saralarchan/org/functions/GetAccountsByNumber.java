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
package saralarchan.org.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import saralarchan.org.core.BaseRequestProcessor;
import saralarchan.org.logger.LogManager;

// This is a sample for extending a base processor 
// The preTransform gets the request object allowing for customization of the request object
// The postTranbsform allows the response preparation after service has trabsformed the
// request using the transformation rules

public class GetAccountsByNumber extends BaseRequestProcessor{
	
	public GetAccountsByNumber() {}
	
	public GetAccountsByNumber(String currentMapping) {
		super(currentMapping);
		
	}
	private String preTransform(String request) {
		
		LogManager.logInfo("preTrasform of "+this.getClass().getName()+" called");
		
		return request;
	}
	public final HashMap execute(String request, String responseTemplate) {
		request = preTransform(request)	;	
        HashMap response = super.execute(request, responseTemplate)  ;
        LogManager.logInfo("Response in execute method of Account:"+response);
        response = postTransform(response)	;	
        return response;
	}
	
	private HashMap postTransform( HashMap response) {
		LogManager.logInfo("postTrasform of "+this.getClass().getName()+" called");
		//TODO any extra code needed for this specific response goes here
		JSONObject jsonResponse = new JSONObject(response);
		
		JSONArray bansArray = new JSONArray();
		bansArray = jsonResponse.getJSONArray("BANs");
		
		String primaryMSISDN = "";
		
		
		JSONObject banObject = new JSONObject();
		banObject = bansArray.getJSONObject(0);
		
		JSONArray linesArray = new JSONArray();
		linesArray = banObject.getJSONArray("Lines");
		
		
		for(int i = 0; i < linesArray.length(); i++) {
			JSONObject line = linesArray.getJSONObject(i);
			boolean isPrimary = line.getBoolean("isPrimary");
			if(isPrimary) {
				primaryMSISDN = line.getString("MSISDN");
			}
		}
		
		String primarycontactNumber = jsonResponse.getString("primaryContactNumber");
		String currMSISDN = primarycontactNumber;
		boolean isPrim = (currMSISDN != null && currMSISDN.equals(primarycontactNumber)) ? true : false;
		
		// creating actual json response.
		JSONObject actualJsonResponse = new JSONObject();
		actualJsonResponse.put("primaryContactNumber", primarycontactNumber);
		actualJsonResponse.put("primaryMSISDN", primaryMSISDN);
		
		String name = jsonResponse.getString("name");
		actualJsonResponse.put("primaryPreferredName", name);// need to apply condition
		
		List<String> provisioningStatusList = new ArrayList<>();
		
		for(int i=0; i<linesArray.length(); i++) {
			JSONObject line = linesArray.getJSONObject(i);
			String provisioningStatus = line.getString("provisioningStatus")!= null ? line.getString("provisioningStatus") : "";
			provisioningStatusList.add(provisioningStatus.toLowerCase());
		}
		boolean areAllLinesTerminated = (provisioningStatusList.contains("active") || provisioningStatusList.contains("suspended")) ? false: true;
		actualJsonResponse.put("areAllLinesTerminated", areAllLinesTerminated);
		
		
		
		
		actualJsonResponse.put("b2cGUID", jsonResponse.getString("B2C_GUID"));
		actualJsonResponse.put("isCobrand", jsonResponse.getBoolean("isCobrand"));
		
		if(isPrim) {
			String primaryEmailAddress = jsonResponse.getString("primaryEmailAddress");
			primaryEmailAddress =  primaryEmailAddress !=null ? (primaryEmailAddress.contains("@dummy.m1.com.sg") ? null : primaryEmailAddress) : null;
			actualJsonResponse.put("primaryEmailAddress", primaryEmailAddress);
		}
		
		JSONArray actualBansArray = new JSONArray();
		JSONObject actualBanOject = new JSONObject();
		if(isPrim) {
			actualBanOject.put("BAN", banObject.getString("BAN"));
			int lineCount = 0;
			for(int i=0; i<linesArray.length();i++) {
				JSONObject line = linesArray.getJSONObject(i);
				if(line.getString("provisioningStatus") != null && line.getString("assetStatus") != null) {
					lineCount++;
				}
			}
			actualBanOject.put("lineCount", lineCount);
			
			actualBanOject.put("areAllLinesUnderBANTerminated", areAllLinesTerminated);
			
			List<JSONObject> linesList = new ArrayList<>();
			
			for(int i=0; i<linesArray.length();i++) {
				JSONObject line = linesArray.getJSONObject(i);
				String msisdn = line.getString("MSISDN")!= null ? line.getString("MSISDN") : "";
				String hasSimActivate = line.getString("hasSIMActivated") != null ? line.getString("hasSIMActivated").toUpperCase() : "";
				
				//String migrationDateString = line.getString("migrationDate") != null ? line.getString("migrationDate") : "";
				String migrationDateString = "2023-02-07T04:52:14.698716Z";
				boolean migrationDateFlag = false;
				try {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
					Date migrationDate = simpleDateFormat.parse(migrationDateString);
					Date nowDate = new Date();
					migrationDateFlag = migrationDate.before(nowDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				String provisioningStatusStr = line.getString("provisioningStatus");
				String assetStatus = line.getString("assetStatus");
				
				if(msisdn.equals(currMSISDN)
						&& !hasSimActivate.equals("COLD")
						&& migrationDateFlag
						&& provisioningStatusStr != null
						&& assetStatus != null) {
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MSISDN",line.getString("MSISDN"));
					jsonObject.put("preferredName",line.getString("PreferredName"));
					jsonObject.put("isWorkSIM",line.getBoolean("isWorkSIM"));
					jsonObject.put("serviceAccountID",line.getString("serviceAccountID"));
					jsonObject.put("lineStatus",line.getString("provisioningStatus"));
					jsonObject.put("lineType",line.getString("lineType"));
					
//					if(line.getString("groupSubscriptionId")!= null) {
//						jsonObject.put("groupSubscriptionId",line.getString("groupSubscriptionId"));
//					}
					linesList.add(jsonObject);
				}
			}
			actualBanOject.put("lines", linesList);
			actualBansArray.put(actualBanOject);
		}else {
			actualBanOject.put("BAN", banObject.getString("BAN"));
			int lineCount = 0;
			for(int i=0; i<linesArray.length();i++) {
				JSONObject line = linesArray.getJSONObject(i);
				if(line.getString("provisioningStatus") != null && line.getString("assetStatus") != null) {
					lineCount++;
				}
			}
			actualBanOject.put("lineCount", lineCount);
			
			actualBanOject.put("areAllLinesUnderBANTerminated", areAllLinesTerminated);
			
			List<JSONObject> linesList = new ArrayList<>();
			
			for(int i=0; i<linesArray.length();i++) {
				JSONObject line = linesArray.getJSONObject(i);
				String msisdn = line.getString("MSISDN")!= null ? line.getString("MSISDN") : "";
				String hasSimActivate = line.getString("hasSIMActivated") != null ? line.getString("hasSIMActivated").toUpperCase() : "";
				
				//String migrationDateString = line.getString("migrationDate") != null ? line.getString("migrationDate") : "";
				String migrationDateString = "2023-02-07T04:52:14.698716Z";
				boolean migrationDateFlag = false;
				try {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
					Date migrationDate = simpleDateFormat.parse(migrationDateString);
					Date nowDate = new Date();
					migrationDateFlag = migrationDate.before(nowDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				String provisioningStatusStr = line.getString("provisioningStatus");
				String assetStatus = line.getString("assetStatus");
				
				if(msisdn.equals(currMSISDN)
						&& !hasSimActivate.equals("COLD")
						&& migrationDateFlag
						&& provisioningStatusStr != null
						&& assetStatus != null) {
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MSISDN",line.getString("MSISDN"));
					jsonObject.put("preferredName",line.getString("PreferredName"));
					jsonObject.put("isWorkSIM",line.getBoolean("isWorkSIM"));
					jsonObject.put("serviceAccountID",line.getString("serviceAccountID"));
					jsonObject.put("lineStatus",line.getString("provisioningStatus"));
					jsonObject.put("lineType",line.getString("lineType"));
					
				}
			}
			
			actualBanOject.put("lines", linesList);
			actualBansArray.put(actualBanOject);
		}
		actualJsonResponse.put("BANs",actualBansArray);
		response.put("RESPONSE",actualJsonResponse.toString());
	    return response;
	}

}
