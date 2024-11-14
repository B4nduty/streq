package banduty.streq;


import java.util.List;
import java.util.Map;

public class StrEq {
	/**
	 * Demonstrates how to use the {@link StrEq#evaluate} method.
	 * <p>
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * public double yourMethod() {
	 *     // Define the formula to evaluate
	 *     String formula = "2*x-3*y";
	 *
	 *     // Map containing variable names and their corresponding values
	 *     Map<String, Double> variables = new HashMap<>();
	 *     variables.put("x", 4d);
	 *     variables.put("y", 2d);
	 *
	 *     // Flag indicating whether the result can be negative
	 *     boolean resultCanBeNegative = true;
	 *
	 *     // Call StrEq.evaluate to evaluate the formula and return the result
	 *     return StrEq.evaluate(formula, variables, resultCanBeNegative);
	 * }
	 * }
	 * </pre>
	 *
	 * Compatible with Numbers (1, 2, 3, 4,...), Operators (+ - * /), Parenthesis (), Variable (x, y, ...)
	 * <p>
	 * Take in care that if you put 2x instead of 2*x won't work as expected.
	 */
	public void onInitialize() {
	}

	public static double evaluate(String expression, Map<String, Double> variables, boolean allowNeg) {
		List<Tokenizer.Token> tokens = Tokenizer.tokenize(expression);
		List<Tokenizer.Token> postfix = ExpressionEvaluator.toPostfix(tokens, variables);
		double result = ExpressionEvaluator.evaluatePostfix(postfix);

		if (!allowNeg && result < 0) {
			throw new IllegalArgumentException("StrEq says: The result is negative, which is not allowed.");
		}

		return result;
	}
}