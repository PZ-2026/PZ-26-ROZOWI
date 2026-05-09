package pl.edu.ur.blokur.security;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.ur.blokur.controller.AuthController;
import pl.edu.ur.blokur.service.InvitationService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.PasswordResetService;
import pl.edu.ur.blokur.service.RefreshTokenService;

/**
 * Test integracyjny weryfikujący działanie RateLimitFilter w pełnym łańcuchu Spring MVC. Po
 * wysłaniu 60 żądań do /api/auth/login 61. żądanie powinno zostać zablokowane (HTTP 429) z
 * nagłówkiem Retry-After.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({RateLimitFilter.class, JwtAuthenticationFilter.class})
@DisplayName("RateLimitFilter — test integracyjny blokady po przekroczeniu limitu")
class RateLimitIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private pl.edu.ur.blokur.repository.UserRepository userRepository;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private RefreshTokenService refreshTokenService;
    @MockitoBean private PasswordResetService passwordResetService;
    @MockitoBean private InvitationService invitationService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private static final String LOGIN_BODY =
            "{\"email\":\"test@blokur.pl\",\"password\":\"wrong\"}";

    @Test
    @DisplayName("61. żądanie do /api/auth/login zwraca 429 z nagłówkiem Retry-After")
    void blocksAfterSixtyRequestsToLogin() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_REQUESTS; i++) {
            mockMvc.perform(
                            post("/api/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(LOGIN_BODY)
                                    .with(
                                            req -> {
                                                req.setRemoteAddr("192.0.2.99");
                                                return req;
                                            }))
                    .andExpect(status().is(not(429)));
        }

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(LOGIN_BODY)
                                .with(
                                        req -> {
                                            req.setRemoteAddr("192.0.2.99");
                                            return req;
                                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("61. żądanie do /api/auth/forgot-password zwraca 429")
    void blocksAfterSixtyRequestsToForgotPassword() throws Exception {
        String forgotBody = "{\"email\":\"test@blokur.pl\"}";

        for (int i = 0; i < RateLimitFilter.MAX_REQUESTS; i++) {
            mockMvc.perform(
                            post("/api/auth/forgot-password")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(forgotBody)
                                    .with(
                                            req -> {
                                                req.setRemoteAddr("192.0.2.88");
                                                return req;
                                            }))
                    .andExpect(status().is(not(429)));
        }

        mockMvc.perform(
                        post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(forgotBody)
                                .with(
                                        req -> {
                                            req.setRemoteAddr("192.0.2.88");
                                            return req;
                                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
