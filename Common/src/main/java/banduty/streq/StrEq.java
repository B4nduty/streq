package banduty.streq;

import java.util.List;
import java.util.Map;

public class StrEq {
	public static final String MOD_ID = "streq";

	public static double evaluate(String expression, Map<String, Double> variables, boolean allowNeg, String modID) {
		try {
			List<Tokenizer.Token> tokens = Tokenizer.tokenize(expression);
			List<Tokenizer.Token> postfix = ExpressionEvaluator.toPostfix(tokens, variables);
			double result = ExpressionEvaluator.evaluatePostfix(postfix);

			if (!allowNeg && result < 0) {
				throw new IllegalArgumentException(modID + " says: The result is negative, which is not allowed.");
			}

			return result;
		} catch (Exception e) {
			System.err.println("An error occurred during evaluation: " + e.getMessage());
			throw new RuntimeException("Error evaluating expression: " + expression, e);
		}
	}

	public boolean areEqual(List<String> expressions, List<Map<String, Double>> variables, boolean approximated) {
		try {
			if (expressions.size() != variables.size()) {
				throw new IllegalArgumentException("Each expression must have a corresponding variable map.");
			}

			if (expressions.isEmpty()) {
				return true;
			}

			double firstResult = evaluate(expressions.getFirst(), variables.getFirst(), true, null);

			for (int i = 1; i < expressions.size(); i++) {
				double currentResult = evaluate(expressions.get(i), variables.get(i), true, null);
				if (!areApproxEqual(firstResult, currentResult, approximated)) {
					return false;
				}
			}

			return true;
		} catch (Exception e) {
			System.err.println("An error occurred during equality check: " + e.getMessage());
			return false;
		}
	}

	private static boolean areApproxEqual(double firstResult, double currentResult, boolean approximated) {
		try {
			if (approximated) {
				return Math.abs(firstResult - currentResult) <= 1e-9;
			} else {
				return firstResult == currentResult;
			}
		} catch (Exception e) {
			System.err.println("An error occurred during approximation check: " + e.getMessage());
			return false;
		}
	}

	public double getDifference(String expr1, String expr2, Map<String, Double> variables) {
		try {
			return getDifference(expr1, expr2, variables, variables);
		} catch (Exception e) {
			System.err.println("An error occurred while calculating the difference: " + e.getMessage());
			return Double.NaN;
		}
	}

	public double getDifference(String expr1, String expr2, Map<String, Double> variables, Map<String, Double> variables2) {
		try {
			double result1 = evaluate(expr1, variables, true, null);
			double result2 = evaluate(expr2, variables2, true, null);
			return result1 - result2;
		} catch (Exception e) {
			System.err.println("An error occurred while calculating the difference: " + e.getMessage());
			return Double.NaN;
		}
	}
}