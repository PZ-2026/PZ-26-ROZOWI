package pl.edu.ur.blokur.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtr HTTP implementujący rate limiting metodą sliding window dla endpointów publicznych /api/auth.
 * Limit: 60 żądań na minutę per IP. Przekroczenie skutkuje odpowiedzią HTTP 429 z nagłówkiem
 * Retry-After informującym o czasie oczekiwania w sekundach.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    static final int MAX_REQUESTS = 60;
    static final long WINDOW_MS = 60_000L;

    private static final Set<String> RATE_LIMITED_PATHS =
            Set.of("/api/auth/login", "/api/auth/forgot-password");

    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!RATE_LIMITED_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(request);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps =
                requestTimestamps.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_REQUESTS) {
                long retryAfterSeconds =
                        (WINDOW_MS - (now - timestamps.peekFirst())) / 1000 + 1;
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter()
                        .write(
                                "{\"error\":\"Too Many Requests\","
                                        + "\"message\":\"Przekroczono limit "
                                        + MAX_REQUESTS
                                        + " \u017c\u0105da\u0144 na minut\u0119. Spr\u00f3buj ponownie za "
                                        + retryAfterSeconds
                                        + " sekund.\"}");
                return;
            }

            timestamps.addLast(now);
        }

        chain.doFilter(request, response);
    }

    String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
