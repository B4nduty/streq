package banduty.streq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StrEq {
	/**
	 * Container for function metadata and logic.
	 * @param argCount Number of arguments the function expects.
	 * @param logic The functional implementation using a List of Doubles as input.
	 */
	public record MathFunction(int argCount, Function<List<Double>, Double> logic) {}

	private final Map<String, MathFunction> functions = new HashMap<>();

	/**
	 * Initializes the engine with standard mathematical functions.
	 */
	public StrEq() {
		functions.put("sin", new MathFunction(1, a -> Math.sin(a.get(0))));
		functions.put("cos", new MathFunction(1, a -> Math.cos(a.get(0))));
		functions.put("abs", new MathFunction(1, a -> Math.abs(a.get(0))));
		functions.put("sqrt", new MathFunction(1, a -> Math.sqrt(a.get(0))));

		functions.put("logb", new MathFunction(2, a -> Math.log(a.get(1)) / Math.log(a.get(0))));
		functions.put("gcd", new MathFunction(2, a -> (double) gcd(Math.round(a.get(0)), Math.round(a.get(1)))));
		functions.put("lcm", new MathFunction(2, a -> {
			long n1 = Math.round(a.get(0));
			long n2 = Math.round(a.get(1));
			return (n1 == 0 || n2 == 0) ? 0.0 : (double) Math.abs(n1 * n2) / gcd(n1, n2);
		}));

		// Iterative placeholders (Logic is handled inside ExpressionEvaluator)
		functions.put("sum", new MathFunction(4, a -> 0.0));
		functions.put("prod", new MathFunction(4, a -> 0.0));

		// Ternary logic: if(condition, true_val, false_val)
		functions.put("if", new MathFunction(3, a -> a.get(0) > 0 ? a.get(1) : a.get(2)));
	}

	/**
	 * Recursive implementation of the Greatest Common Divisor.
	 */
	private long gcd(long a, long b) {
		return b == 0 ? Math.abs(a) : gcd(b, a % b);
	}

	/**
	 * Evaluates an expression without adding default constants.
	 * Used primarily for internal sub-expression evaluation (e.g., inside loops).
	 */
	public double evaluateInternal(String expr, Map<String, Double> vars) {
		return CacheExpression.evaluate(expr, vars, functions, this);
	}

	/**
	 * Allows users to register new functions to the engine dynamically.
	 */
	public void addCustomFunction(String name, int argCount, Function<List<Double>, Double> logic) {
		functions.put(name, new MathFunction(argCount, logic));
	}

	/**
	 * The primary method to calculate a result from a string.
	 * Injects default constants (pi, e, phi) before processing.
	 * * @param expression The math formula.
	 * @param variables A map of user-defined variable values.
	 * @return The calculated numerical result.
	 */
	public double evaluate(String expression, Map<String, Double> variables) {
		Map<String, Double> contextVars = new HashMap<>(variables);
		contextVars.putIfAbsent("pi", Math.PI);
		contextVars.putIfAbsent("e", Math.E);
		contextVars.putIfAbsent("phi", (1 + Math.sqrt(5)) / 2.0);

		return CacheExpression.evaluate(expression, contextVars, functions, this);
	}
}