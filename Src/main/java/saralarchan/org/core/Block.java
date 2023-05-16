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

public class Block {
public String  blockname;
public String clause;
public int lineno;
public ArrayList lines = null;
public String getBlockname() {
	return blockname;
}
public void setBlockname(String blockname) {
	this.blockname = blockname;
}
public String getClause() {
	return clause;
}
public void setClause(String clause) {
	this.clause = clause;
}
public ArrayList getLines() {
	return lines;
}
public void setLines(ArrayList lines) {
	this.lines = lines;
}
public int getLineno() {
	return lineno;
}
public void setLineno(int lineno) {
	this.lineno = lineno;
}
}
