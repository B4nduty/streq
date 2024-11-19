package banduty.streq;

import java.util.*;
import java.util.function.BiFunction;

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
        variables.put("pi", Math.PI);
        variables.put("e", Math.E);
        variables.put("phi", (1 + Math.sqrt(5)) / 2);

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
                case COMMA -> {
                    while (!operators.isEmpty() && !operators.peek().value().equals("(")) {
                        output.add(operators.pop());
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

        Map<String, BiFunction<Double, Double, Double>> functionOperations = Map.of(
                "sin", (a, b) -> Math.sin(a),
                "cos", (a, b) -> Math.cos(a),
                "tan", (a, b) -> Math.tan(a),
                "arcsin", (a, b) -> Math.asin(a),
                "arccos", (a, b) -> Math.acos(a),
                "arctan", (a, b) -> Math.atan(a),
                "sqrt", (a, b) -> Math.sqrt(a),
                "log", (a, b) -> Math.log10(a),
                "ln", (a, b) -> Math.log(a),
                "logb", ExpressionEvaluator::logBase
        );

        for (Tokenizer.Token token : postfix) {
            switch (token.type()) {
                case NUMBER -> stack.push(Double.parseDouble(token.value()));
                case OPERATOR -> {
                    if (stack.size() < 2) {
                        throw new IllegalStateException("Not enough operands for operator: " + token.value());
                    }
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(operatorFunctions.get(token.value()).apply(a, b));
                }
                case FUNCTION -> {
                    if ("logb".equals(token.value())) {
                        if (stack.size() < 2) {
                            throw new IllegalStateException("Not enough operands for logb function: " + stack);
                        }
                        double number = stack.pop();
                        double base = stack.pop();
                        if (number <= 0 || base <= 0) {
                            throw new IllegalArgumentException("Invalid arguments for logb function: base=" + number + ", number=" + base);
                        }
                        stack.push(functionOperations.get(token.value()).apply(number, base));
                    } else {
                        if (stack.isEmpty()) {
                            throw new IllegalStateException("Not enough operands for function: " + token.value());
                        }
                        double a = stack.pop();
                        stack.push(functionOperations.get(token.value()).apply(a, 0.0));
                    }
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid postfix expression, stack should contain exactly one result.");
        }

        return stack.pop();
    }

    static double logBase(double number, double base) {
        if (base <= 0 || number <= 0 || base == 1) {
            return Double.NaN;
        }
        return Math.log(number) / Math.log(base);
    }
}