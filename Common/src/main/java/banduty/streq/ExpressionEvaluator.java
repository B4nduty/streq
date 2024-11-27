package banduty.streq;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

final class ExpressionEvaluator {

    private ExpressionEvaluator() {}

    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "+", 1, "-", 1, "*", 2, "/", 2, "^", 3, "!", 4
    );

    private static final Map<String, Double> CONSTANTS = Map.of(
            "pi", Math.PI, "e", Math.E, "phi", (1 + Math.sqrt(5)) / 2
    );

    private static final Map<String, BiFunction<Double, Double, Double>> OPERATOR_FUNCTIONS = Map.of(
            "+", Double::sum, "-", (a, b) -> a - b, "*", (a, b) -> a * b, "/", (a, b) -> a / b, "^", Math::pow
    );

    private static final Map<String, Function<Double, Double>> FUNCTION_OPERATIONS = Map.of(
            "sin", Math::sin, "cos", Math::cos, "tan", Math::tan,
            "arcsin", Math::asin, "arccos", Math::acos, "arctan", Math::atan,
            "sqrt", Math::sqrt, "log", Math::log10, "ln", Math::log, "abs", Math::abs
    );

    private static final Map<String, BiFunction<Double, Double, Double>> COMMA_FUNCTION_OPERATIONS = Map.of(
            "logb", ExpressionEvaluator::logBase, "gcd", ExpressionEvaluator::gcd, "lcm", ExpressionEvaluator::lcm
    );

    static List<Tokenizer.Token> toPostfix(List<Tokenizer.Token> tokens, Map<String, Double> variables) {
        List<Tokenizer.Token> output = new ArrayList<>(tokens.size());
        Deque<Tokenizer.Token> operators = new ArrayDeque<>(tokens.size());

        variables.putAll(CONSTANTS);

        for (Tokenizer.Token token : tokens) {
            switch (token.type()) {
                case NUMBER -> output.add(token);
                case VARIABLE -> {
                    Double value = variables.get(token.value());
                    if (value == null) {
                        throw new IllegalArgumentException("Unrecognized variable: " + token.value());
                    }
                    output.add(new Tokenizer.Token(Tokenizer.TokenType.NUMBER, String.valueOf(value)));
                }
                case FUNCTION -> operators.push(token);
                case OPERATOR -> {
                    while (!operators.isEmpty() &&
                            operators.peek().type() == Tokenizer.TokenType.OPERATOR &&
                            PRECEDENCE.get(token.value()) <= PRECEDENCE.get(operators.peek().value())) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                }
                case PARENTHESIS -> {
                    if ("(".equals(token.value())) {
                        operators.push(token);
                    } else {
                        while (!operators.isEmpty() && !"(".equals(operators.peek().value())) {
                            output.add(operators.pop());
                        }
                        operators.pop(); // Remove '('
                        if (!operators.isEmpty() && operators.peek().type() == Tokenizer.TokenType.FUNCTION) {
                            output.add(operators.pop()); // Pop function
                        }
                    }
                }
                case COMMA -> {
                    while (!operators.isEmpty() && !"(".equals(operators.peek().value())) {
                        output.add(operators.pop());
                    }
                }
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    static double evaluatePostfix(List<Tokenizer.Token> postfix) {
        Deque<Double> stack = new ArrayDeque<>(postfix.size());

        for (Tokenizer.Token token : postfix) {
            switch (token.type()) {
                case NUMBER -> stack.push(Double.parseDouble(token.value()));
                case OPERATOR -> {
                    String operator = token.value();
                    if ("!".equals(operator)) {
                        stack.push(factorial(stack.pop().intValue()));
                    } else {
                        double b = stack.pop(), a = stack.pop();
                        stack.push(OPERATOR_FUNCTIONS.get(operator).apply(a, b));
                    }
                }
                case FUNCTION -> {
                    String function = token.value();
                    if (COMMA_FUNCTION_OPERATIONS.containsKey(function)) {
                        double number = stack.pop(), base = stack.pop();
                        stack.push(COMMA_FUNCTION_OPERATIONS.get(function).apply(base, number));
                    } else {
                        stack.push(FUNCTION_OPERATIONS.get(function).apply(stack.pop()));
                    }
                }
            }
        }

        if (stack.size() != 1) throw new IllegalStateException("Invalid postfix expression.");
        return stack.pop();
    }

    static double logBase(double base, double number) {
        return (base <= 0 || number <= 0 || base == 1) ? Double.NaN : Math.log(number) / Math.log(base);
    }

    private static double gcd(double a, double b) {
        while (b != 0) {
            double temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private static double lcm(double a, double b) {
        return (a * b) / gcd(a, b);
    }

    private static double factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Factorial is only defined for non-negative integers.");
        double result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
}
