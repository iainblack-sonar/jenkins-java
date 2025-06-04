package demo.security.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RateLimitFilter {
    private RateLimitFilter() { /* Utility class */ }

    private static final int MAX_REQUESTS = 10;
    private static final int TIME_WINDOW_SECONDS = 60;
    // Note: Guava's MapMaker.expireAfterWrite is deprecated, but used here for lack of a better alternative in Java 17 + Guava for expiring maps.
    @SuppressWarnings("deprecation")
    private static final java.util.concurrent.ConcurrentMap<String, SimpleCounter> requestCounts =
        new com.google.common.collect.MapMaker().expireAfterWrite(TIME_WINDOW_SECONDS, java.util.concurrent.TimeUnit.SECONDS).makeMap();

    private static class SimpleCounter {
        int count = 0;
    }

    private static void handleRateLimitExceeded(HttpServletResponse response) {
        if (response != null) {
            try {
                response.setStatus(429);
                response.getWriter().write("Rate limit exceeded. Try again later.");
                response.getWriter().close();
            } catch (Exception e) {
                // Intentionally ignored
            }
        }
    }

    public static boolean allowRequest(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();
        try {
            SimpleCounter counter = requestCounts.get(clientIp);
            if (counter == null) {
                counter = new SimpleCounter();
                requestCounts.put(clientIp, counter);
            }
            synchronized (counter) {
                counter.count++;
                if (counter.count > MAX_REQUESTS) {
                    handleRateLimitExceeded(response);
                    return false;
                }
            }
        } catch (Exception e) {
            // Should not happen
            return true;
        }
        return true;
    }
}
