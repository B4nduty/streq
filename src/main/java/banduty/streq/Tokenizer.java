package banduty.streq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The Tokenizer breaks a raw String expression into a list of meaningful components (Tokens).
 * It identifies numbers, variables, functions, and handles special syntax like implicit multiplication.
 */
final class Tokenizer {
    enum TokenType {
        NUMBER, OPERATOR, PARENTHESIS, VARIABLE, FUNCTION, COMMA, STRING
    }

    /**
     * Represents a single unit of the expression.
     * @param argCount Used primarily for functions to know how many values to pop from the stack.
     */
    record Token(TokenType type, String value, int argCount) {
        Token(TokenType type, String value) {
            this(type, value, 0);
        }
    }

    private Tokenizer() {}

    /**
     * Scans the input string and converts it into a List of Tokens.
     * * @param expression The raw math string (e.g., "2x + sin(pi)")
     * @param registeredFunctions Set of known function names to distinguish them from variables.
     */
    static List<Token> tokenize(String expression, Set<String> registeredFunctions) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int length = expression.length();

        for (int i = 0; i < length; i++) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) continue;

            // String Literal Handling
            // Used for sub-expressions in sum() and prod() or variable names.
            if (c == '\'' || c == '"') {
                StringBuilder sb = new StringBuilder();
                i++;
                while (i < length && expression.charAt(i) != c) {
                    char current = expression.charAt(i);
                    if (current == '\\' && i + 1 < length) { // Escape character support
                        sb.append(expression.charAt(i + 1));
                        i += 2;
                    } else {
                        sb.append(current);
                        i++;
                    }
                }
                tokens.add(new Token(TokenType.STRING, sb.toString()));
                continue;
            }

            // Number Handling
            if (Character.isDigit(c) || c == '.') {
                buffer.append(c);
            } 
            
            // Word Handling (Variables or Functions)
            else if (Character.isLetter(c)) {
                if (!buffer.isEmpty()) {
                    tokens.add(new Token(TokenType.NUMBER, buffer.toString()));
                    buffer.setLength(0);
                    tokens.add(new Token(TokenType.OPERATOR, "*"));
                }

                // Logic for Implicit Multiplication: "2x" becomes "2 * x"
                if (!tokens.isEmpty()) {
                    Token last = tokens.get(tokens.size() - 1);
                    if (last.type() == TokenType.PARENTHESIS && last.value().equals(")")) {
                        tokens.add(new Token(TokenType.OPERATOR, "*"));
                    }
                }

                StringBuilder nameBuf = new StringBuilder();
                nameBuf.append(c);
                while (i + 1 < length && (Character.isLetter(expression.charAt(i + 1)) || Character.isDigit(expression.charAt(i + 1)))) {
                    i++;
                    nameBuf.append(expression.charAt(i));
                }

                String name = nameBuf.toString();
                // Check if the word is a known function or a user-defined variable
                tokens.add(registeredFunctions.contains(name) ?
                        new Token(TokenType.FUNCTION, name) : new Token(TokenType.VARIABLE, name));
            } else {
                // Flush the number buffer if we hit an operator or parenthesis
                if (!buffer.isEmpty()) {
                    tokens.add(new Token(TokenType.NUMBER, buffer.toString()));
                    buffer.setLength(0);
                }

                // Structural Symbol Handling
                if (c == '(') {
                    if (!buffer.isEmpty()) {
                        tokens.add(new Token(TokenType.NUMBER, buffer.toString()));
                        buffer.setLength(0);
                        tokens.add(new Token(TokenType.OPERATOR, "*"));
                    }

                    // Logic for Implicit Multiplication: "5(x)" becomes "5 * (x)"
                    if (!tokens.isEmpty()) {
                        Token last = tokens.get(tokens.size() - 1);
                        if (last.type() == TokenType.VARIABLE || (last.type() == TokenType.PARENTHESIS && last.value().equals(")"))) {
                            tokens.add(new Token(TokenType.OPERATOR, "*"));
                        }
                    }
                    tokens.add(new Token(TokenType.PARENTHESIS, "("));
                } else if (c == ')') {
                    tokens.add(new Token(TokenType.PARENTHESIS, ")"));
                } else if (c == ',') {
                    tokens.add(new Token(TokenType.COMMA, ","));
                } else if (c == '=' && i + 1 < length && expression.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.OPERATOR, "=="));
                    i++;
                } else if ("+-*/^!%".indexOf(c) != -1) {
                    tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                }
            }
        }

        if (!buffer.isEmpty()) {
            tokens.add(new Token(TokenType.NUMBER, buffer.toString()));
        }

        return Collections.unmodifiableList(tokens);
    }
}