package banduty.streq;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

/**
 * Handles the caching logic for evaluated expressions to improve performance.
 * Uses a Thread-safe LRU (Least Recently Used) cache.
 */
class CacheExpression {
    private static final int MAX_ENTRIES = 1000; // Limits memory usage
    private static final long TTL_MS = 60_000; // 60-second expiration

    private record CacheKey(String expression, Map<String, Double> variables) {}
    private record CacheEntry(double result, long expiry) {}

    /**
     * Synchronized LinkedHashMap acting as an LRU cache.
     */
    private static final Map<CacheKey, CacheEntry> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CacheKey, CacheEntry> eldest) {
                    return size() > MAX_ENTRIES;
                }
            }
    );

    /**
     * Checks the cache for an existing result before computing the expression.
     */
    public static double evaluate(String expression, Map<String, Double> variables,
                                  Map<String, StrEq.MathFunction> functions,
                                  StrEq engine) {
        CacheKey key = new CacheKey(expression, Map.copyOf(variables));
        long now = System.currentTimeMillis();

        CacheEntry entry = CACHE.get(key);
        if (entry != null && now < entry.expiry) {
            return entry.result;
        }

        double result = computeExpression(expression, variables, functions, engine);
        CACHE.put(key, new CacheEntry(result, now + TTL_MS));
        return result;
    }

    /**
     * Internal bridge to the Tokenizer and Evaluator.
     */
    private static double computeExpression(String expression, Map<String, Double> variables,
                                           Map<String, StrEq.MathFunction> functions,
                                           StrEq engine) {
        var tokens = Tokenizer.tokenize(expression, functions.keySet());
        var postfix = ExpressionEvaluator.toPostfix(tokens);
        return ExpressionEvaluator.evaluatePostfix(postfix, functions, engine, variables);
    }
}