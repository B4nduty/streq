package banduty.streq;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CacheExpression {
    private static final int CACHE_MAX_SIZE = 1000;
    private static final long CACHE_TTL_MS = 60000;

    private static final Map<String, CacheEntry> cache = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > CACHE_MAX_SIZE;
        }
    });

    private static class CacheEntry {
        double result;
        long timestamp;

        CacheEntry(double result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
    }

    public static double evaluate(String expression, Map<String, Double> variables) {
        String cacheKey = expression + serializeVariables(variables);

        synchronized (cache) {
            CacheEntry entry = cache.get(cacheKey);
            if (entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_TTL_MS) {
                return entry.result;
            }
        }

        double result = computeExpression(expression, variables);

        synchronized (cache) {
            cache.put(cacheKey, new CacheEntry(result, System.currentTimeMillis()));
        }

        return result;
    }

    private static String serializeVariables(Map<String, Double> variables) {
        return variables.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    private static double computeExpression(String expression, Map<String, Double> variables) {
        List<Tokenizer.Token> tokens = Tokenizer.tokenize(expression);
        List<Tokenizer.Token> postfix = ExpressionEvaluator.toPostfix(tokens, variables);
        return ExpressionEvaluator.evaluatePostfix(postfix);
    }
}
