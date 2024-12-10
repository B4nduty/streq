package banduty.streq;

import java.util.List;
import java.util.Map;

public class StrEq {
	public static final String MOD_ID = "streq";

	public static double evaluate(String expression, Map<String, Double> variables) {
		return CacheExpression.evaluate(expression, variables);
	}

	public boolean areEqual(List<String> expressions, List<Map<String, Double>> variables, boolean approximated) {
		if (expressions.size() != variables.size()) {
			throw new IllegalArgumentException("Each expression must have a corresponding variable map.");
		}

		if (expressions.isEmpty()) {
			return true;
		}

		double firstResult = evaluate(expressions.getFirst(), variables.getFirst());

		for (int i = 1; i < expressions.size(); i++) {
			double currentResult = evaluate(expressions.get(i), variables.get(i));
			if (!areApproxEqual(firstResult, currentResult, approximated)) {
				return false;
			}
		}

		return true;
	}

	private static boolean areApproxEqual(double firstResult, double currentResult, boolean approximated) {
		if (approximated) {
			return Math.abs(firstResult - currentResult) <= 1e-9;
		} else {
			return firstResult == currentResult;
		}
	}

	public double getDifference(String expr1, String expr2, Map<String, Double> variables) {
		return getDifference(expr1, expr2, variables, variables);
	}

	public double getDifference(String expr1, String expr2, Map<String, Double> variables, Map<String, Double> variables2) {
		try {
			double result1 = evaluate(expr1, variables);
			double result2 = evaluate(expr2, variables2);
			return result1 - result2;
		} catch (Exception e) {
			System.err.println("An error occurred while calculating the difference: " + e.getMessage());
			return Double.NaN;
		}
	}
}