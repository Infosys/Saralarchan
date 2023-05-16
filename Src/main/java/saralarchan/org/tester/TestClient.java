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
package saralarchan.org.tester;

import java.io.IOException;

import saralarchan.org.adapter.rest.HttpsClient;



public class TestClient  {

	public static void main(String[] args) {
		// Example args
		String server = "https://192.168.1.7:8443/API/CheckCustomerBlackList";
		String request = "{\r\n" +
				"   \"CUSTOMER_ID\": \"334323126654\",\r\n" +
				"   \"CUSTOMER_TYPE\": \"AADHAR\"\r\n" +
				" }";

		try {
			run( server, request );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	public static void run(String server, String request)  {
		HttpsClient client = new HttpsClient();
		try {

			client.CallServer(server, request);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

}