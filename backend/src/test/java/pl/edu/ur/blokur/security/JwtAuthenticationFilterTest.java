package pl.edu.ur.blokur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("JwtAuthenticationFilter — walidacja JWT i ustawianie MDC")
class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Nested
    @DisplayName("brak nagłówka Authorization")
    class NoAuthorizationHeader {

        @Test
        @DisplayName("przepuszcza żądanie bez tokenu i nie ustawia MDC")
        void passesRequestAndDoesNotSetMdc() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/categories");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(MDC.get("userId")).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("przepuszcza żądanie z nieprawidłowym prefixem nagłówka")
        void passesRequestWithInvalidAuthPrefix() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(MDC.get("userId")).isNull();
        }
    }

    @Nested
    @DisplayName("prawidłowy token JWT")
    class ValidToken {

        @Test
        @DisplayName("ustawia userId w MDC gdy token jest prawidłowy")
        void setsUserIdInMdcForValidToken() throws Exception {
            String token = "valid.jwt.token";
            when(jwtService.isTokenValid(token)).thenReturn(true);
            when(jwtService.extractUsername(token)).thenReturn("mieszkaniec@blokur.pl");
            when(jwtService.extractRole(token)).thenReturn("MIESZKANIEC");

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Odczytanie MDC musi nastąpić PODCZAS wykonania łańcucha filtrów
            String[] capturedUserId = new String[1];
            FilterChain chain = (req, res) -> capturedUserId[0] = MDC.get("userId");

            filter.doFilterInternal(request, response, chain);

            assertThat(capturedUserId[0]).isEqualTo("mieszkaniec@blokur.pl");
        }

        @Test
        @DisplayName("ustawia uwierzytelnienie w SecurityContext dla prawidłowego tokenu")
        void setsAuthenticationInSecurityContext() throws Exception {
            String token = "valid.jwt.token";
            when(jwtService.isTokenValid(token)).thenReturn(true);
            when(jwtService.extractUsername(token)).thenReturn("admin@blokur.pl");
            when(jwtService.extractRole(token)).thenReturn("ZARZADCA");

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/buildings");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getName()).isEqualTo("admin@blokur.pl");
            assertThat(authentication.getAuthorities())
                    .extracting(Object::toString)
                    .containsExactly("ROLE_ZARZADCA");
        }
    }

    @Nested
    @DisplayName("nieprawidłowy token JWT")
    class InvalidToken {

        @Test
        @DisplayName("nie ustawia MDC gdy token jest nieważny")
        void doesNotSetMdcForInvalidToken() throws Exception {
            String token = "invalid.token";
            when(jwtService.isTokenValid(token)).thenReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(MDC.get("userId")).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("nie ustawia MDC gdy username jest null")
        void doesNotSetMdcWhenUsernameIsNull() throws Exception {
            String token = "token.with.null.username";
            when(jwtService.isTokenValid(token)).thenReturn(true);
            when(jwtService.extractUsername(token)).thenReturn(null);
            when(jwtService.extractRole(token)).thenReturn("MIESZKANIEC");

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(MDC.get("userId")).isNull();
        }
    }
}
