package pl.edu.ur.blokur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("RateLimitFilter — sliding window 60 req/min per IP")
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Nested
    @DisplayName("ścieżki nieobjęte rate limitingiem")
    class NonRateLimitedPaths {

        @Test
        @DisplayName("przepuszcza /api/auth/refresh bez liczenia")
        void passesRefreshEndpoint() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/refresh");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("przepuszcza /api/tickets bez liczenia")
        void passesTicketsEndpoint() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("rate limiting /api/auth/login")
    class LoginRateLimit {

        @Test
        @DisplayName("60 żądań z tego samego IP przechodzi bez blokady")
        void allowsUpToLimit() throws Exception {
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < RateLimitFilter.MAX_REQUESTS; i++) {
                MockHttpServletRequest request =
                        buildRequest("/api/auth/login", "192.168.1.1");
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilterInternal(request, response, chain);
                assertThat(response.getStatus())
                        .as("żądanie %d powinno przejść", i + 1)
                        .isEqualTo(200);
            }
        }

        @Test
        @DisplayName("61. żądanie z tego samego IP zwraca 429 z nagłówkiem Retry-After")
        void blocksAfterLimit() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            String ip = "10.0.0.5";

            for (int i = 0; i < RateLimitFilter.MAX_REQUESTS; i++) {
                MockHttpServletRequest req = buildRequest("/api/auth/login", ip);
                filter.doFilterInternal(req, new MockHttpServletResponse(), chain);
            }

            MockHttpServletRequest blockedRequest = buildRequest("/api/auth/login", ip);
            MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
            FilterChain blockedChain = mock(FilterChain.class);

            filter.doFilterInternal(blockedRequest, blockedResponse, blockedChain);

            assertThat(blockedResponse.getStatus()).isEqualTo(429);
            assertThat(blockedResponse.getHeader("Retry-After")).isNotNull();
            verify(blockedChain, never()).doFilter(blockedRequest, blockedResponse);
        }

        @Test
        @DisplayName("różne IP nie dzielą limitu")
        void separateLimitsPerIp() throws Exception {
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < RateLimitFilter.MAX_REQUESTS; i++) {
                MockHttpServletRequest req = buildRequest("/api/auth/login", "10.0.0.1");
                filter.doFilterInternal(req, new MockHttpServletResponse(), chain);
            }

            MockHttpServletRequest otherIpRequest = buildRequest("/api/auth/login", "10.0.0.2");
            MockHttpServletResponse otherIpResponse = new MockHttpServletResponse();

            filter.doFilterInternal(otherIpRequest, otherIpResponse, chain);

            assertThat(otherIpResponse.getStatus()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("rate limiting /api/auth/forgot-password")
    class ForgotPasswordRateLimit {

        @Test
        @DisplayName("61. żądanie na forgot-password zwraca 429")
        void blocksAfterLimit() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            String ip = "10.0.0.9";

            for (int i = 0; i < RateLimitFilter.MAX_REQUESTS; i++) {
                MockHttpServletRequest req = buildRequest("/api/auth/forgot-password", ip);
                filter.doFilterInternal(req, new MockHttpServletResponse(), chain);
            }

            MockHttpServletRequest blockedRequest = buildRequest("/api/auth/forgot-password", ip);
            MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

            filter.doFilterInternal(blockedRequest, blockedResponse, mock(FilterChain.class));

            assertThat(blockedResponse.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("resolveClientIp")
    class ResolveClientIp {

        @Test
        @DisplayName("używa X-Forwarded-For gdy dostępny")
        void usesXForwardedFor() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");

            assertThat(filter.resolveClientIp(request)).isEqualTo("1.2.3.4");
        }

        @Test
        @DisplayName("fallback na remoteAddr gdy brak X-Forwarded-For")
        void fallsBackToRemoteAddr() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("9.9.9.9");

            assertThat(filter.resolveClientIp(request)).isEqualTo("9.9.9.9");
        }
    }

    private MockHttpServletRequest buildRequest(String path, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        return request;
    }
}
