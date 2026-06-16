package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.ur.blokur.models.PasswordResetToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.PasswordResetTokenRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService — reset hasła 6-cyfrowym kodem (TTL 1 h)")
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordResetTokenRepository tokenRepository;

    @Mock private JavaMailSender mailSender;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private PasswordResetService passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "fromAddress", "noreply@blokur.pl");

        testUser = new User();
        testUser.setEmail("jan@blokur.pl");
        testUser.setPasswordHash("staryHash");
    }

    @Nested
    @DisplayName("requestPasswordReset")
    class RequestPasswordReset {

        @Test
        @DisplayName("Gdy e-mail nie istnieje — nie zapisuje kodu ani nie wysyła maila")
        void shouldDoNothingWhenEmailNotFound() {
            when(userRepository.findByEmail("brak@blokur.pl")).thenReturn(Optional.empty());

            passwordResetService.requestPasswordReset("brak@blokur.pl");

            verify(tokenRepository, never()).save(any());
            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Gdy e-mail istnieje — usuwa stare kody i zapisuje nowy 6-cyfrowy")
        void shouldReplaceExistingCodes() {
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));

            passwordResetService.requestPasswordReset("jan@blokur.pl");

            verify(tokenRepository).deleteByUser(testUser);

            ArgumentCaptor<PasswordResetToken> captor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());

            PasswordResetToken saved = captor.getValue();
            assertThat(saved.getToken()).matches("\\d{6}");
            assertThat(saved.getUser()).isEqualTo(testUser);
            assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now().plusMinutes(59));
            assertThat(saved.getExpiryDate()).isBefore(LocalDateTime.now().plusMinutes(61));
        }

        @Test
        @DisplayName("Wysyła e-mail zawierający 6-cyfrowy kod")
        void shouldSendEmailWithCode() {
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));

            passwordResetService.requestPasswordReset("jan@blokur.pl");

            ArgumentCaptor<SimpleMailMessage> mailCaptor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            ArgumentCaptor<PasswordResetToken> tokenCaptor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(mailSender).send(mailCaptor.capture());
            verify(tokenRepository).save(tokenCaptor.capture());

            SimpleMailMessage mail = mailCaptor.getValue();
            assertThat(mail.getTo()).contains("jan@blokur.pl");
            assertThat(mail.getText()).contains(tokenCaptor.getValue().getToken());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("Nieznany email — rzuca IllegalArgumentException")
        void shouldThrowWhenEmailNotFound() {
            when(userRepository.findByEmail("brak@blokur.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    passwordResetService.resetPassword(
                                            "brak@blokur.pl", "123456", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy kod");
        }

        @Test
        @DisplayName("Nieprawidłowy kod — rzuca IllegalArgumentException")
        void shouldThrowWhenCodeNotFound() {
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(testUser, "000000"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    passwordResetService.resetPassword(
                                            "jan@blokur.pl", "000000", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy kod");
        }

        @Test
        @DisplayName("Wygasły kod — rzuca IllegalArgumentException i usuwa kod z bazy")
        void shouldThrowAndDeleteExpiredToken() {
            PasswordResetToken expired =
                    new PasswordResetToken(testUser, "123456", LocalDateTime.now().minusHours(1));
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(testUser, "123456"))
                    .thenReturn(Optional.of(expired));

            assertThatThrownBy(
                            () ->
                                    passwordResetService.resetPassword(
                                            "jan@blokur.pl", "123456", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("wygasł");

            verify(tokenRepository).delete(expired);
        }

        @Test
        @DisplayName("Prawidłowy kod — hashuje hasło, zapisuje użytkownika i usuwa kod")
        void shouldHashSaveAndConsumeCode() {
            PasswordResetToken valid =
                    new PasswordResetToken(testUser, "654321", LocalDateTime.now().plusHours(1));
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(testUser, "654321"))
                    .thenReturn(Optional.of(valid));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            passwordResetService.resetPassword("jan@blokur.pl", "654321", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isEqualTo("nowyHash");
            verify(userRepository).save(testUser);
            verify(tokenRepository).delete(valid);
        }
    }
}
