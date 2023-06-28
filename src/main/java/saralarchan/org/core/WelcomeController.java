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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import saralarchan.org.adapter.db.SQLLiteConnector;

@RestController
@RequestMapping("/")

public class WelcomeController {

	// inject via application.properties
	@Value("${welcome.message:test}")
	private String message = "Hello World";

	@GetMapping("/test")
	@ResponseStatus(code = HttpStatus.OK, reason = "OK")
	public String welcome1() {
		System.out.println("WelcomeController called");
		return "{'value':'Test controller is working'}";
	}

	@PostMapping("/testDB")
	// @ResponseStatus(code = HttpStatus.OK, reason = "OK")
	public String testDB(HttpServletRequest req, HttpServletResponse response) {
		String name = req.getParameter("empname");
		if (name != null)
			return SQLLiteConnector.queryDB("Employee", "designation", "name", name);
		else
			return "No name specuified";

	}

}
