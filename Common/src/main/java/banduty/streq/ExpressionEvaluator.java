package banduty.streq;

import java.util.*;

class ExpressionEvaluator {
    static List<Tokenizer.Token> toPostfix(List<Tokenizer.Token> tokens, Map<String, Double> variables) {
        List<Tokenizer.Token> output = new ArrayList<>();
        Stack<Tokenizer.Token> operators = new Stack<>();

        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("+", 1);
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);

        for (Tokenizer.Token token : tokens) {
            switch (token.type()) {
                case NUMBER:
                    output.add(token);
                    break;
                case VARIABLE:
                    String variableName = token.value();
                    Double value = variables.get(variableName);
                    if (value == null) {
                        throw new IllegalArgumentException("Unrecognized variable: " + variableName);
                    }
                    output.add(new Tokenizer.Token(Tokenizer.TokenType.NUMBER, String.valueOf(value)));
                    break;
                case OPERATOR:
                    while (!operators.isEmpty() && operators.peek().type() == Tokenizer.TokenType.OPERATOR &&
                            precedence.get(token.value()) <= precedence.get(operators.peek().value())) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                    break;
                case PARENTHESIS:
                    if (token.value().equals("(")) {
                        operators.push(token);
                    } else if (token.value().equals(")")) {
                        while (!operators.isEmpty() && !operators.peek().value().equals("(")) {
                            output.add(operators.pop());
                        }
                        operators.pop();
                    }
                    break;
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    static double evaluatePostfix(List<Tokenizer.Token> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        for (Tokenizer.Token token : postfix) {
            if (token.type() == Tokenizer.TokenType.NUMBER) {
                stack.push(Double.parseDouble(token.value()));
            } else if (token.type() == Tokenizer.TokenType.OPERATOR) {
                double b = stack.pop();
                double a = stack.pop();

                switch (token.value()) {
                    case "+":
                        stack.push(a + b);
                        break;
                    case "-":
                        stack.push(a - b);
                        break;
                    case "*":
                        stack.push(a * b);
                        break;
                    case "/":
                        stack.push(a / b);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + token.value());
                }
            }
        }

        return stack.pop();
    }
}