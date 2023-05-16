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

package saralarchan.org.adapter.db;

import java.util.HashMap;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;

import net.sf.jsqlparser.expression.operators.relational.LikeExpression;

public class ExpressionResolvingAdapter extends ExpressionVisitorAdapter {
	public HashMap<String, String> placeholdermap = null;
	public HashMap<String, String> cache = null;;

	public ExpressionResolvingAdapter(HashMap<String, String> ctx, HashMap<String, String> placeholdermap) {
		super();
		this.cache = ctx;
		this.placeholdermap = placeholdermap;
	}

	public HashMap<String, String> getPlaceholderMap() {
		return placeholdermap;
	}

	public void visitBinaryExpression(BinaryExpression expr) {
		if (expr instanceof ComparisonOperator) {

			String placeholder = expr.getRightExpression().toString();

			if (placeholder.startsWith("$VAR.") || placeholder.startsWith("$.")) {
                String value = getContextValue(placeholder)	;			
				this.placeholdermap.put(placeholder,value);
			}
			if (placeholder.startsWith("'$VAR.") || placeholder.startsWith("'$.")) {
				// System.out.println(placeholder);
				placeholder = placeholder.substring(1, placeholder.length() - 1);
				 String value = getContextValue(placeholder)	;	
				this.placeholdermap.put(placeholder, value);
			}
		}
		super.visitBinaryExpression(expr);
	}

	private String getContextValue(String placeholder) {
		String[] toks = placeholder.split("\\.");		
		return this.cache.get(toks[1]);
	}

	public void visit(LikeExpression expr) {
		if (expr instanceof LikeExpression) {
			String placeholder = expr.getRightExpression().toString();

			if (placeholder.contains("$VAR.") || (placeholder.contains("$."))) {
				String value = getContextValue(placeholder)	;
				this.placeholdermap.put(placeholder,value);
			}
		}
		super.visit(expr);
	}

	/*
	 * public void visit(InExpression expr) {
	 * 
	 * 
	 * if (expr instanceof InExpression) { String placeholder =
	 * expr.getRightExpression().toString(); } super.visit(expr); }
	 */

}
