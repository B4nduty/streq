package banduty.streq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the caching logic for evaluated expressions to improve performance.
 * Uses a Thread-safe LRU (Least Recently Used) cache.
 */
class CacheExpression {
    // Pre-tokenize and cache the structure separately from the result
    private static final Map<String, List<Tokenizer.Token>> STRUCTURE_CACHE = new ConcurrentHashMap<>();

    /**
     * Checks the cache for an existing result before computing the expression.
     */
    public static double evaluate(String expression, Map<String, Double> variables,
                                  Map<String, StrEq.MathFunction> functions,
                                  StrEq engine) {
        List<Tokenizer.Token> postfix = STRUCTURE_CACHE.computeIfAbsent(expression, expr -> {
            var tokens = Tokenizer.tokenize(expr, functions.keySet());
            return ExpressionEvaluator.toPostfix(tokens);
        });

        return ExpressionEvaluator.evaluatePostfix(postfix, functions, engine, variables);
    }
}