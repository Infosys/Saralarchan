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

//import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProductDto {
	
	private String key;
	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "ProductDto [key=" + key + ", data=" + data + "]";
	}

	public void setKey(String key) {
		this.key = key;
	}
	private String data;

	public ProductDto(String data) {
		super();
		this.data = data;
	}
	public ProductDto(Object object, Object object2) {
		super();
		this.key = (String) object;
		this.data = (String) object2;
	}
	public ProductDto() {
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

}
