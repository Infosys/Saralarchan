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



public class AzureConnectionServiceConfigurationsImpl  {
	
	
	private String azureServiceDomain;
	private String azureServiceEndpoint;
	private String azureXFuntionHeader;
	private String orderEligibilityEndpoint;

	
	
	protected final void activate() {
	
		this.azureServiceDomain = getAzureServiceDomain();
		this.azureServiceEndpoint = getAzureServiceEndpoint();
		this.azureXFuntionHeader = getAzureXFuntionHeader();
		this.orderEligibilityEndpoint = getFunctionEndpoint();
	}
	
		// The below returned values are templates
	    // replace them with actual implementation specific values.
		
		//@AttributeDefinition(name = "Azure Service Domain", description = "Azure Service Domain Name")
	    public String getAzureServiceDomain() { return "https://zstest01.azurewebsites.net";}
		
		//@AttributeDefinition(name = "Azure Service functions endpoint", description = "Azure Service functions endpoint (should begin with /)")
	    public String getAzureServiceEndpoint() { return "/api/functions";}
		
		//@AttributeDefinition(name = "Azure Service functions header key", description = "Azure Service functions header key")
	    public String getAzureXFuntionHeader() { return "token********==";}
		
		//@AttributeDefinition(name = "Preorder Eligibility Endpoint", description = "Preorder Eligibility Endpoint (should begin with /)")
	    public String getFunctionEndpoint() { return "/orderfunc/function1?id={ID}";}

	

}
