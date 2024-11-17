package banduty.streq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class Tokenizer {
    enum TokenType {
        NUMBER, OPERATOR, PARENTHESIS, VARIABLE, FUNCTION
    }

    record Token(TokenType type, String value) {}

    private Tokenizer() {}

    private static final Set<String> FUNCTIONS = Set.of("sin", "cos", "tan", "arcsin", "arccos", "arctan", "sqrt");

    static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int length = expression.length();
        boolean expectingNegativeNumber = true;

        for (int i = 0; i < length; i++) {
            char currentChar = expression.charAt(i);

            if (Character.isDigit(currentChar) || currentChar == '.') {
                buffer.append(currentChar);
                expectingNegativeNumber = false;
            } else {
                if (!buffer.isEmpty()) {
                    tokens.add(new Token(TokenType.NUMBER, buffer.toString()));
                    buffer.setLength(0);
                }

                if (Character.isLetter(currentChar)) {
                    if (!tokens.isEmpty() && tokens.getLast().type == TokenType.NUMBER) {
                        tokens.add(new Token(TokenType.OPERATOR, "*"));
                    }
                    buffer.append(currentChar);
                    while (++i < length && Character.isLetter(expression.charAt(i))) {
                        buffer.append(expression.charAt(i));
                    }
                    i--;

                    String func = buffer.toString();
                    if (FUNCTIONS.contains(func)) {
                        tokens.add(new Token(TokenType.FUNCTION, func));
                    } else {
                        tokens.add(new Token(TokenType.VARIABLE, func));
                    }
                    buffer.setLength(0);
                    expectingNegativeNumber = false;
                } else if (currentChar == '-' && expectingNegativeNumber) {
                    buffer.append(currentChar);
                    expectingNegativeNumber = false;
                } else if ("+-*/".indexOf(currentChar) != -1) {
                    tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar)));
                    expectingNegativeNumber = true;
                } else if (currentChar == '^') {
                    tokens.add(new Token(TokenType.OPERATOR, "^"));
                    expectingNegativeNumber = true;
                } else if (currentChar == '(') {
                    tokens.add(new Token(TokenType.PARENTHESIS, String.valueOf(currentChar)));
                    expectingNegativeNumber = true;
                } else if (currentChar == ')') {
                    tokens.add(new Token(TokenType.PARENTHESIS, String.valueOf(currentChar)));
                    expectingNegativeNumber = false;
                } else if (!Character.isWhitespace(currentChar)) {
                    throw new IllegalArgumentException("Unrecognized character in expression: " + currentChar);
                }
            }
        }

        if (!buffer.isEmpty()) {
            tokens.add(new Token(TokenType.NUMBER, buffer.toString()));
        }

        return Collections.unmodifiableList(tokens);
    }
}