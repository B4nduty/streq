package banduty.streq;


import java.util.List;
import java.util.Map;

public class StrEq {
	public static final String MOD_ID = "streq";

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