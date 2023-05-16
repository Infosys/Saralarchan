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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PatternFinder {

private static ArrayList patterns = new ArrayList();
public static  boolean loaded = false;

public static void loadPatterns(){


    // Add as many patterns as needed 
	
	patterns.add(Pattern.compile("\\{\"CheckCustomerBlacklist\":.*\\}"));
	//patterns.add(Pattern.compile("\\{\"accounts\":.*\\}"));
    loaded = true;

}

public static  String get(String candidate) {

String key = "NOMATCH";
for (int i = 0; i < patterns.size(); i++) {
	Pattern pattern = (Pattern)patterns.get(i);
	Matcher matcher =  pattern.matcher(candidate);

		if ( matcher.find()) {
			int startInd = matcher.start();
			int endInd = matcher.end();
			String region = candidate.substring(startInd, endInd);
			//System.out.println(matcher.start()+":"+candidate);
			switch(i) {
            // for each pattern added in the patterns list add a case key to 
			// handle that pattern in the request
			
			case 0: key = "CheckCustomerBlackListRequestService"; break;
			

			}
}


}


return key;

}


public static String handleRequest(String key,String candidate) {
	String response = "";


	return response;
}
public static void searchAndReplace(ArrayList content,int index,HashMap stubs){
	String candidate = "";
	Matcher matcher = null;

	Marker marker = null;

	Pattern pattern = (Pattern)patterns.get(index);
	for (int i = 0; i < content.size(); i++) {
		candidate = (String) content.get(i);
		matcher =  pattern.matcher(candidate);

		ArrayList markerList = new ArrayList();
		while ( matcher.find()) {

			int startInd = matcher.start();
			int endInd = matcher.end();
			marker = new Marker(startInd,endInd);
			markerList.add(marker);


}

		if (markerList.size() > 0){

			String candidateClone = new String(candidate.toString());
			for ( int j= 0; j < markerList.size(); j++){
				Marker marked = (Marker)markerList.get(j);
				String subSequence = candidate.substring(marked.getStart(),marked.getEnd());

				String stubbedValue = (String)stubs.get(subSequence);
				candidateClone = candidateClone.replace(subSequence,stubbedValue.toString());

			}
			content.set(i, candidateClone);
		}


	}
}




public static HashMap search(ArrayList content,int index){
	String candidate = "";
	Matcher matcher = null;
    HashMap patternMap = new HashMap();
	Marker marker = null;

	Pattern pattern = (Pattern)patterns.get(index);
	for (int i = 0; i < content.size(); i++) {
		candidate = (String) content.get(i);
		matcher =  pattern.matcher(candidate);

		ArrayList markerList = new ArrayList();
		while ( matcher.find()) {

			int startInd = matcher.start();
			int endInd = matcher.end();
			marker = new Marker(startInd,endInd);
			markerList.add(marker);

}

		if (markerList.size() > 0){


			for ( int j= 0; j < markerList.size(); j++){
				Marker marked = (Marker)markerList.get(j);
				String subSequence = candidate.substring(marked.getStart(),marked.getEnd());

				patternMap.put(subSequence, null);

			}

		}


	}
	return patternMap;
}

public static void searchAndComment(ArrayList content,int index){
	String candidate = "";
	Matcher matcher = null;

	Marker marker = null;

	Pattern pattern = (Pattern)patterns.get(index);
	for (int i = 0; i < content.size(); i++) {
		candidate = (String) content.get(i);
		matcher =  pattern.matcher(candidate);

		ArrayList markerList = new ArrayList();
		while ( matcher.find()) {

			int startInd = matcher.start();
			int endInd = matcher.end();
			marker = new Marker(startInd,endInd);
			markerList.add(marker);


}

		if (markerList.size() > 0){
			//System.out.println("Orig:["+candidate+"]");
			String candidateClone = new String(candidate.toString());
			for ( int j= 0; j < markerList.size(); j++){
				Marker marked = (Marker)markerList.get(j);
				String subSequence = candidate.substring(marked.getStart(),marked.getEnd());
				//System.out.println("Matched:["+subSequence+"]");
				//System.out.println(subSequence);
				//System.out.println(stubs.get(subSequence));
				String stubbedValue = "<!-- "+subSequence.toUpperCase()+" -->";
				candidateClone = candidateClone.replace(subSequence,stubbedValue.toString());
				//System.out.println("changed:["+candidateClone+"]");
			}
			content.set(i, candidateClone);
		}


	}
}


public static void searchAndRemoveBlock(ArrayList content,int blockStart, int blockEnd ){
	String candidate = "";
	Matcher matcher = null;

    int startCount = 0;


	Pattern patternStart = (Pattern)patterns.get(blockStart);
	Pattern patternEnd = (Pattern)patterns.get(blockEnd);
	String candidateClone = new String(candidate.toString());
	for (int i = 0; i < content.size(); i++) {
		candidate = (String) content.get(i);

		matcher =  patternStart.matcher(candidate);
		if ( matcher.find()) {
				startCount = startCount + 1;

		}
		else{

				matcher =  patternEnd.matcher(candidate);
				if ( matcher.find()){

				startCount = startCount - 1;
				if (startCount == 0){
					content.set(i,"<!-- "+candidate.toUpperCase()+" -->");
				}
				}


			}
		if (startCount > 0){
			content.set(i,"<!-- "+candidate.toUpperCase()+" -->");
		}
	}
}


public static void searchAndRepeatBlock(ArrayList content,int blockMarkerStart, int blockMarkerEnd, int repeatNo ){
	String candidate = "";
	String oneLine = "";
	Matcher matcher = null;
    ArrayList blockArray = new ArrayList();
    int startCount = 0;
    boolean justFinished = false;
    ArrayList tempArray = new ArrayList();

	Pattern patternStart = (Pattern)patterns.get(blockMarkerStart);
	Pattern patternEnd = (Pattern)patterns.get(blockMarkerEnd);

	for (int i = 0; i < content.size(); i++) {
		candidate = (String) content.get(i);

		matcher =  patternStart.matcher(candidate);
		if ( matcher.find()) {
				startCount = startCount + 1;
				justFinished=false;
				//System.out.println(candidate);
		}
		else{

				matcher =  patternEnd.matcher(candidate);
				if ( matcher.find()){
					//System.out.println(candidate);
				startCount = startCount - 1;
				if (startCount == 0){
					tempArray.add("<!-- EXPANDED FOR BLOCK -->");
					for (int k =0 ; k < repeatNo ; k++){

					for ( int j = 1 ; j < blockArray.size() ; j++){
					   oneLine = (String)blockArray.get(j);
						tempArray.add(oneLine);
//						System.out.println(oneLine);
					}
					}
					blockArray.clear();
					justFinished = true;
					tempArray.add("<!-- END OF FOR -->");
				}
				}


			}
		if (startCount > 0){

			    blockArray.add(candidate);

		}
		else
		{
                 if (justFinished ==true)
                	 justFinished = false;
                 else
			     	 tempArray.add(candidate);
		}
	}

	content.clear();
	content.addAll(tempArray);
	}

public static String getFromJson(String mesg) {
	// placeholder methos
	return null;
}

}


