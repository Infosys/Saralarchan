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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import saralarchan.org.logger.LogManager;

public class QueryParser {

	public static HashMap prepareSelect(HashMap ctx, String sql) {
		HashMap replacementData = new HashMap();
		try {					
		Statement select = (Statement) CCJSqlParserUtil.parse(sql);
		Select stmt = (Select) select;
		PlainSelect ps = (PlainSelect) stmt.getSelectBody();
		Expression whereClause = ps.getWhere();
		if (whereClause == null)
			return replacementData;

		Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause.toString());

		ExpressionResolvingAdapter era = new ExpressionResolvingAdapter(ctx, replacementData);
		expr.accept(era);
		}
		catch (JSQLParserException e ) {
			LogManager.logError("Select Statement Could not be parsed", e);
		}
		LogManager.logInfo(replacementData.toString());
		return replacementData;
	}

	public static HashMap prepareInsert(HashMap ctx, String sql) {
		HashMap replacementData = new HashMap();
		Statement insert;
		try {
			insert = (Statement) CCJSqlParserUtil.parse(sql);
			Insert stmt = (Insert) insert;
			ExpressionList exprList = (ExpressionList) stmt.getItemsList();
			List<Expression> l = exprList.getExpressions();
			Iterator iter = l.iterator();
			while (iter.hasNext()) {
				Expression expr = (Expression) iter.next();
				String placeholder = expr.toString();

				if (placeholder.startsWith("$VAR.") || placeholder.startsWith("$.")) {
					String value = getContextValue(placeholder, ctx)	;					
					replacementData.put(placeholder, value);
				}
				if (placeholder.startsWith("'$VAR.") || placeholder.startsWith("'$.")) {
					placeholder = placeholder.substring(1, placeholder.length() - 1);
					String value = getContextValue(placeholder, ctx)	;					
					replacementData.put(placeholder, value);
				}
			}

		} catch (JSQLParserException e) {
			LogManager.logError("Insert Statement Could not be parsed", e);
		}

		return replacementData;
	}

	public static HashMap parseUpdate(HashMap ctx, String sql) {

		Statement statement;
		HashMap replacementData = new HashMap();
		try {
			statement = CCJSqlParserUtil.parse(sql);
			Update update = (Update) statement;
			ArrayList<UpdateSet> updateSets = update.getUpdateSets();

			Iterator<UpdateSet> iter = updateSets.iterator();
			while (iter.hasNext()) {
				UpdateSet uset = iter.next();
				List<Expression> updateExpr = uset.getExpressions();
				String placeholder = updateExpr.toString();

				if (placeholder.startsWith("[$VAR.") || placeholder.startsWith("[$.")) {
					String value = getContextValue(placeholder, ctx)	;
					
					replacementData.put(placeholder, value);
				}
				if (placeholder.startsWith("['$VAR.") || placeholder.startsWith("['$.")) {
					placeholder = placeholder.substring(2, placeholder.length() - 2);
					String value = getContextValue(placeholder, ctx)	;					
					replacementData.put(placeholder, value);
				}

			}

			Expression updateWhereClause = update.getWhere();
			if (updateWhereClause != null) {

				Expression expr = CCJSqlParserUtil.parseCondExpression(updateWhereClause.toString());
				ExpressionResolvingAdapter era = new ExpressionResolvingAdapter(ctx, replacementData);
				expr.accept(era);
			}
		} catch (JSQLParserException e) {
			LogManager.logError("Update Statement Could not be parsed", e);
		}
		return replacementData;

	}

	public static HashMap parseDelete(HashMap ctx, String sql) {
		Statement statement;
		HashMap replacementData = new HashMap();
		try {
			statement = CCJSqlParserUtil.parse(sql);
			Delete delete = (Delete) statement;
			Expression deleteWhereClause = delete.getWhere();
			if (deleteWhereClause != null) {
				Expression expr = CCJSqlParserUtil.parseCondExpression(deleteWhereClause.toString());
				ExpressionResolvingAdapter era = new ExpressionResolvingAdapter(ctx, replacementData);
				expr.accept(era);
			}
		} catch (JSQLParserException e) {
			LogManager.logError("Delete Statement Could not be parsed", e);
		}
		return replacementData;
	}
	
	public static String prepareSQL(HashMap map,String sql) {
		Iterator<String> keyIter = map.keySet().iterator();
	
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			
			sql = sql.replaceAll(Pattern.quote(key), (String) map.get(key));
		}
		return sql;
	}

	private static String getContextValue(String placeholder,HashMap ctx) {
		String[] toks = placeholder.split("\\.");		
		return (String) ctx.get(toks[1]);
	}

}
