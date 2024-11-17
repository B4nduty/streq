package banduty.streq;

import java.util.ArrayList;
import java.util.List;

class Tokenizer {
    enum TokenType {
        NUMBER, OPERATOR, PARENTHESIS, VARIABLE
    }

    record Token(TokenType type, String value) {}

    static List<Token> tokenize(String expression) throws IllegalArgumentException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int length = expression.length();

        boolean expectingNegativeNumber = true;

        for (int i = 0; i < length; i++) {
            char currentChar = expression.charAt(i);

            if (Character.isDigit(currentChar)) {
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
                    while (++i < length && Character.isLetterOrDigit(expression.charAt(i))) {
                        buffer.append(expression.charAt(i));
                    }
                    tokens.add(new Token(TokenType.VARIABLE, buffer.toString()));
                    buffer.setLength(0);
                    i--;
                    expectingNegativeNumber = false;
                } else if (currentChar == '-' && expectingNegativeNumber) {
                    buffer.append(currentChar);
                    expectingNegativeNumber = false;
                } else if (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/') {
                    tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar)));
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

        return tokens;
    }
}