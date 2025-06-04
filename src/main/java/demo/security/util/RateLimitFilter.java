package demo.security.util;

import com.google.common.collect.MapMaker;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentMap;

public class RateLimitFilter {
    private static final int MAX_REQUESTS = 10;
    private static final int TIME_WINDOW_SECONDS = 60;
    @SuppressWarnings("deprecation")
    private static final ConcurrentMap<String, SimpleCounter> requestCounts =
        new MapMaker().expireAfterWrite(TIME_WINDOW_SECONDS, java.util.concurrent.TimeUnit.SECONDS).makeMap();

    private static class SimpleCounter {
        int count = 0;
    }

    private RateLimitFilter() { /* Utility class */ }

    public static boolean allowRequest(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();
        SimpleCounter counter = requestCounts.get(clientIp);
        if (counter == null) {
            counter = new SimpleCounter();
            requestCounts.put(clientIp, counter);
        }
        synchronized (counter) {
            counter.count++;
            if (counter.count > MAX_REQUESTS) {
                if (response != null) {
                    try {
                        response.setStatus(429);
                        response.getWriter().write("Rate limit exceeded. Try again later.");
                        response.getWriter().close();
                    } catch (Exception e) {
                        // Intentionally ignored
                    }
                }
                return false;
            }
        }
        return true;
    }
}
