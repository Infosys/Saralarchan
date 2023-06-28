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
package saralarchan.org.computeinterface;
import java.util.Iterator;

import com.fathzer.soft.javaluator.*;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;

public class SimpleBooleanEvaluator extends AbstractEvaluator<Boolean> {
	  /** The negate unary operator.*/
	  public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);
	  /** The logical AND operator.*/
	  private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
	  /** The logical OR operator.*/
	  public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);
	  /** equality check **/
	  public final static Operator EQUALS = new Operator("==", 2, Operator.Associativity.LEFT, 4);
	  private static final Parameters PARAMETERS;
	 
	  static {
	    // Create the evaluator's parameters
	    PARAMETERS = new Parameters();
	    // Add the supported operators
	    PARAMETERS.add(AND);
	    PARAMETERS.add(OR);
	    PARAMETERS.add(NEGATE);
	    PARAMETERS.add(EQUALS);
	  }
	 
	  public SimpleBooleanEvaluator() {
	    super(PARAMETERS);
	  }
	 
	  @Override
	  protected Boolean toValue(String literal, Object evaluationContext) {
	    return Boolean.valueOf(literal);
	  }
	  
	  @Override
	  protected Boolean evaluate(Operator operator, Iterator<Boolean> operands, Object evaluationContext) {
	    if (operator == NEGATE) {
	    	System.out.println("Negating");
	      return !operands.next();
	    } else if (operator == OR) {
	      Boolean o1 = operands.next();
	      Boolean o2 = operands.next();
	      System.out.println("ORing");
	      return o1 || o2;
	    } else if (operator == AND) {
	      Boolean o1 = operands.next();
	      Boolean o2 = operands.next();
	      System.out.println("ANDing");
	      return o1 && o2;
	    } else if (operator == EQUALS) {
	    	  Boolean o1 = operands.next();
		      Boolean o2 = operands.next();
		      System.out.println("Equality Check");
		      return o1 == o2;
	    }
	    
	    else {
	    	System.out.println("ELSE"+operator.getSymbol());
	    	
	      return super.evaluate(operator, operands, evaluationContext);
	    }
	  }
	 
	  public static void main(String[] args) {
	    SimpleBooleanEvaluator evaluator = new SimpleBooleanEvaluator();
	    StaticVariableSet<Boolean> variables = new StaticVariableSet<Boolean>();
	    DoubleEvaluator deval = new DoubleEvaluator();
	    String expr1 = "(2^3-1)*sin(pi/4)/ln(pi^2)";
	    String expr = "Math.sqrt(100)";
	    // Evaluate an expression
	    Double result = deval.evaluate(expr);
	    // Ouput the result
	    System.out.println(expr + " = " + result);
	  
	    
	    Boolean question = ( Math.sqrt(100) > 11);
	    Boolean answer = true;
	    variables.set("x", question);
	    variables.set("y", answer);
	    String expression = "x==y";
	    System.out.println (expression+" = "+evaluator.evaluate(expression,variables));
	    expression = "true || false";
	    System.out.println (expression+" = "+evaluator.evaluate(expression));
	    expression = "!true";
	    System.out.println (expression+" = "+evaluator.evaluate(expression));
	  }
	  
	  public static void evalVariableExpr(String expr , String variable, String value) {
		    final String expression = "sin(x)"; // Here is the expression to evaluate
		    // Create the evaluator
		    final DoubleEvaluator eval = new DoubleEvaluator();
		    // Create a new empty variable set
		    final StaticVariableSet<Double> variables = new StaticVariableSet<Double>();
		    double x = 0;
		    final double step = Math.PI/8;
		    while (x<=Math.PI/2) {
		      // Set the value of x
		      variables.set("x", x);
		      // Evaluate the expression
		      Double result = eval.evaluate(expression, variables);
		      // Ouput the result
		      System.out.println("x="+x+" -> "+expression+" = "+result);
		      x += step;
		    }
		  }
	  
	  
	}