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

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("SomeBeanFilter")
public class ResponseDto {

	private String response;

	private String productJson;

	private Properties hashTable;

	public ResponseDto(String response, String productJson, Properties hashTable2) {
		super();
		this.response = response;
		this.hashTable = hashTable2;
		this.productJson = productJson;
	}

	public Properties getHashTable() {
		return hashTable;
	}

	public void setHashTable(Properties hashTable) {
		this.hashTable = hashTable;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public ResponseDto(String response, Properties hashTable) {
		super();
		this.response = response;
		this.hashTable = hashTable;
	}

	public ResponseDto(Properties hashTable) {
		super();
		this.hashTable = hashTable;
	}

	public ResponseDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getProductJson() {
		return productJson;
	}

	public void setProductJson(String productJson) {
		this.productJson = productJson;
	}

}
