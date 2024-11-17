package banduty.streq;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

final class ExpressionEvaluator {

    private ExpressionEvaluator() {}

    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "+", 1,
            "-", 1,
            "*", 2,
            "/", 2,
            "^", 3
    );

    static List<Tokenizer.Token> toPostfix(List<Tokenizer.Token> tokens, Map<String, Double> variables) {
        List<Tokenizer.Token> output = new ArrayList<>();
        Deque<Tokenizer.Token> operators = new ArrayDeque<>();
        variables.putIfAbsent("pi", Math.PI);

        for (Tokenizer.Token token : tokens) {
            switch (token.type()) {
                case NUMBER -> output.add(token);
                case VARIABLE -> {
                    String variableName = token.value();
                    Double value = variables.get(variableName);
                    if (value == null) {
                        throw new IllegalArgumentException("Unrecognized variable: " + variableName);
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
                    if (token.value().equals("(")) {
                        operators.push(token);
                    } else {
                        while (!operators.isEmpty() && !operators.peek().value().equals("(")) {
                            output.add(operators.pop());
                        }
                        if (!operators.isEmpty()) operators.pop();
                        if (!operators.isEmpty() && operators.peek().type() == Tokenizer.TokenType.FUNCTION) {
                            output.add(operators.pop());
                        }
                    }
                }
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return Collections.unmodifiableList(output);
    }

    static double evaluatePostfix(List<Tokenizer.Token> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        Map<String, BiFunction<Double, Double, Double>> operatorFunctions = Map.of(
                "+", Double::sum,
                "-", (a, b) -> a - b,
                "*", (a, b) -> a * b,
                "/", (a, b) -> a / b,
                "^", Math::pow
        );

        Map<String, Function<Double, Double>> functionOperations = Map.of(
                "sin", Math::sin,
                "cos", Math::cos,
                "tan", Math::tan,
                "arcsin", Math::asin,
                "arccos", Math::acos,
                "sqrt", Math::sqrt
        );

        for (Tokenizer.Token token : postfix) {
            switch (token.type()) {
                case NUMBER -> stack.push(Double.parseDouble(token.value()));
                case OPERATOR -> {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(operatorFunctions.get(token.value()).apply(a, b));
                }
                case FUNCTION -> {
                    double a = stack.pop();
                    stack.push(functionOperations.get(token.value()).apply(a));
                }
            }
        }

        return stack.pop();
    }
}