package pl.edu.ur.blokur.security;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtr HTTP logujący każde żądanie w formacie strukturyzowanym. Rejestruje metodę HTTP, ścieżkę,
 * kod statusu, czas obsługi oraz identyfikator zalogowanego użytkownika odczytany z MDC. MDC jest
 * czyszczone po zalogowaniu, zapewniając izolację między żądaniami.
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * Mierzy czas obsługi żądania i loguje jego szczegóły po zakończeniu łańcucha filtrów.
     * Czyszczenie MDC odbywa się w bloku {@code finally}, gwarantując brak wycieków między wątkami
     * w puli połączeń.
     *
     * @param request żądanie HTTP
     * @param response odpowiedź HTTP
     * @param filterChain łańcuch filtrów
     * @throws ServletException w przypadku błędu przetwarzania filtra
     * @throws IOException w przypadku błędu wejścia/wyjścia
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startMs = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startMs;
            String userId = MDC.get("userId");
            log.info(
                    "request",
                    keyValue("method", request.getMethod()),
                    keyValue("path", request.getRequestURI()),
                    keyValue("status", response.getStatus()),
                    keyValue("durationMs", durationMs),
                    keyValue("userId", userId != null ? userId : "anonymous"));
            MDC.remove("userId");
        }
    }
}
