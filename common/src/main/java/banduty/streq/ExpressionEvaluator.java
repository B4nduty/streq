package banduty.streq;

import java.util.*;
import java.util.function.BinaryOperator;

/**
 * Orchestrates the conversion to Reverse Polish Notation (RPN) and its evaluation.
 */
final class ExpressionEvaluator {
    // Defines operator priority
    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "==", 0,
            "+", 1, "-", 1,
            "*", 2, "/", 2, "%", 2,
            "^", 3,
            "!", 4
    );

    // Maps operator symbols to functional logic
    private static final Map<String, BinaryOperator<Double>> BINARY_OPS = Map.of(
            "+", Double::sum, "-", (a, b) -> a - b,
            "*", (a, b) -> a * b, "/", (a, b) -> a / b,
            "%", (a, b) -> a % b, "^", Math::pow,
            "==", (a, b) -> a.equals(b) ? 1.0 : 0.0
    );

    /**
     * Evaluates a list of tokens in Postfix (RPN) order using a stack-based approach.
     */
    static double evaluatePostfix(List<Tokenizer.Token> postfix, Map<String, StrEq.MathFunction> customFunctions,
                                  StrEq strEqInstance, Map<String, Double> variables) {
        Deque<Double> stack = new ArrayDeque<>();
        Deque<String> stringStack = new ArrayDeque<>();

        for (var token : postfix) {
            switch (token.type()) {
                case NUMBER -> stack.push(Double.parseDouble(token.value()));
                case VARIABLE -> {
                    Double value = variables.get(token.value());
                    if (value == null) {
                        throw new IllegalArgumentException("Unknown variable: " + token.value());
                    }
                    stack.push(value);
                }
                case STRING -> stringStack.push(token.value());
                case OPERATOR -> {
                    if ("!".equals(token.value())) {
                        if (stack.isEmpty()) throw new IllegalStateException("Stack empty for !");
                        stack.push(factorial(stack.pop()));
                    } else {
                        if (stack.size() < 2) throw new IllegalStateException("Stack underflow for " + token.value());
                        double b = stack.pop();
                        double a = stack.pop();
                        stack.push(BINARY_OPS.get(token.value()).apply(a, b));
                    }
                }
                case FUNCTION -> {
                    StrEq.MathFunction func = customFunctions.get(token.value());
                    if (func == null) throw new IllegalArgumentException("Unknown function: " + token.value());

                    // Handle special iterative functions
                    if (token.value().equals("sum") || token.value().equals("prod")) {
                        String subExpr = stringStack.pop();
                        String varName = stringStack.pop();
                        double end = stack.pop();
                        double start = stack.pop();

                        double result = token.value().equals("sum")
                                ? summation(varName, start, end, subExpr, variables, strEqInstance)
                                : product(varName, start, end, subExpr, variables, strEqInstance);
                        stack.push(result);
                    } else {
                        // Standard function logic execution
                        StrEq.MathFunction mFunc = customFunctions.get(token.value());
                        if (mFunc == null) throw new IllegalArgumentException("Undefined function: " + token.value());

                        List<Double> args = new ArrayList<>();
                        for (int i = 0; i < mFunc.argCount(); i++) {
                            if (stack.isEmpty())
                                throw new IllegalStateException("Not enough arguments for " + token.value());
                            args.add(stack.pop());
                        }
                        Collections.reverse(args);
                        stack.push(mFunc.logic().apply(args));
                    }
                }
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: The stack has " +
                    stack.size() + " values remaining. Check for missing operators.");
        }
        return stack.pop();
    }

    private static double factorial(double n) {
        if (n < 0) return Double.NaN;
        double res = 1;
        int val = (int) Math.round(n);
        for (int i = 2; i <= val; i++) res *= i;
        return res;
    }

    // Iterative implementations for summation and product
    public static double summation(String varName, double start, double end, String expression, Map<String, Double> baseVars, StrEq strEqInstance) {
        double total = 0;
        Map<String, Double> context = new HashMap<>(baseVars);

        for (int i = (int) start; i <= (int) end; i++) {
            context.put(varName, (double) i);
            total += strEqInstance.evaluateInternal(expression, context);
        }
        return total;
    }

    public static double product(String varName, double start, double end, String expression, Map<String, Double> baseVars, StrEq strEqInstance) {
        double total = 1;
        Map<String, Double> context = new HashMap<>(baseVars);

        for (int i = (int) start; i <= (int) end; i++) {
            context.put(varName, (double) i);
            total *= strEqInstance.evaluateInternal(expression, context);
        }
        return total;
    }

    /**
     * Implements the Shunting-Yard algorithm to convert Infix to Postfix.
     */
    static List<Tokenizer.Token> toPostfix(List<Tokenizer.Token> tokens) {
        List<Tokenizer.Token> output = new ArrayList<>(tokens.size());
        Deque<Tokenizer.Token> stack = new ArrayDeque<>();

        for (Tokenizer.Token token : tokens) {
            switch (token.type()) {
                case NUMBER, STRING, VARIABLE -> output.add(token);
                case FUNCTION -> stack.push(token);
                case OPERATOR -> {
                    int p1 = PRECEDENCE.get(token.value());
                    while (!stack.isEmpty() && stack.peek().type() == Tokenizer.TokenType.OPERATOR) {
                        int p2 = PRECEDENCE.get(stack.peek().value());

                        if (p2 > p1 || (p2 == p1 && !token.value().equals("^"))) {
                            output.add(stack.pop());
                        } else {
                            break;
                        }
                    }
                    stack.push(token);
                }
                case PARENTHESIS -> {
                    if ("(".equals(token.value())) stack.push(token);
                    else {
                        while (!stack.isEmpty() && !"(".equals(stack.peek().value())) {
                            output.add(stack.pop());
                        }
                        if (!stack.isEmpty()) stack.pop(); // Pop '('

                        // After closing parenthesis, check if it belonged to a function
                        if (!stack.isEmpty() && stack.peek().type() == Tokenizer.TokenType.FUNCTION) {
                            output.add(stack.pop());
                        }
                    }
                }
                case COMMA -> {
                    while (!stack.isEmpty() && !"(".equals(stack.peek().value())) {
                        output.add(stack.pop());
                    }
                }
            }
        }
        while (!stack.isEmpty()) {
            if ("(".equals(stack.peek().value())) throw new IllegalArgumentException("Mismatched parentheses");
            output.add(stack.pop());
        }

        return output;
    }
}