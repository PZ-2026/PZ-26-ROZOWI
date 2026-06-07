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
@DisplayName("PasswordResetService — reset hasła przez e-mail (TTL 1 h)")
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
        ReflectionTestUtils.setField(
                passwordResetService, "resetBaseUrl", "https://blokur.pl/reset");

        testUser = new User();
        testUser.setEmail("jan@blokur.pl");
        testUser.setPasswordHash("staryHash");
    }

    // -------------------------------------------------------
    // requestPasswordReset
    // -------------------------------------------------------

    @Nested
    @DisplayName("requestPasswordReset")
    class RequestPasswordReset {

        @Test
        @DisplayName("Gdy e-mail nie istnieje w bazie — nie zapisuje tokena ani nie wysyła maila")
        void shouldDoNothingWhenEmailNotFound() {
            when(userRepository.findByEmail("brak@blokur.pl")).thenReturn(Optional.empty());

            passwordResetService.requestPasswordReset("brak@blokur.pl");

            verify(tokenRepository, never()).save(any());
            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Gdy e-mail istnieje — zapisuje token w bazie")
        void shouldSaveTokenWhenEmailExists() {
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));

            passwordResetService.requestPasswordReset("jan@blokur.pl");

            ArgumentCaptor<PasswordResetToken> captor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());

            PasswordResetToken saved = captor.getValue();
            assertThat(saved.getToken()).isNotBlank();
            assertThat(saved.getUser()).isEqualTo(testUser);
            assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now().plusMinutes(59));
            assertThat(saved.getExpiryDate()).isBefore(LocalDateTime.now().plusMinutes(61));
        }

        @Test
        @DisplayName("Gdy e-mail istnieje — wysyła e-mail z linkiem zawierającym token")
        void shouldSendEmailWithResetLink() {
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));

            passwordResetService.requestPasswordReset("jan@blokur.pl");

            ArgumentCaptor<SimpleMailMessage> mailCaptor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(mailCaptor.capture());

            SimpleMailMessage mail = mailCaptor.getValue();
            assertThat(mail.getTo()).contains("jan@blokur.pl");
            assertThat(mail.getText()).contains("https://blokur.pl/reset?token=");
        }
    }

    // -------------------------------------------------------
    // resetPassword
    // -------------------------------------------------------

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("Nieprawidłowy token — rzuca IllegalArgumentException")
        void shouldThrowWhenTokenNotFound() {
            when(tokenRepository.findByToken("nieistniejacy")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () -> passwordResetService.resetPassword("nieistniejacy", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy token");
        }

        @Test
        @DisplayName("Wygasły token — rzuca IllegalArgumentException i usuwa token z bazy")
        void shouldThrowAndDeleteExpiredToken() {
            PasswordResetToken expiredToken =
                    new PasswordResetToken(testUser, "wygasly", LocalDateTime.now().minusHours(1));
            when(tokenRepository.findByToken("wygasly")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> passwordResetService.resetPassword("wygasly", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("wygasł");

            verify(tokenRepository).delete(expiredToken);
        }

        @Test
        @DisplayName("Prawidłowy token — hashuje nowe hasło i zapisuje użytkownika")
        void shouldHashAndSaveNewPassword() {
            PasswordResetToken validToken =
                    new PasswordResetToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(1));
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            passwordResetService.resetPassword("dobryToken", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isEqualTo("nowyHash");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Prawidłowy token — usuwa token po zmianie hasła")
        void shouldDeleteTokenAfterSuccessfulReset() {
            PasswordResetToken validToken =
                    new PasswordResetToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(1));
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode(any())).thenReturn("hash");

            passwordResetService.resetPassword("dobryToken", "NoweHaslo1");

            verify(tokenRepository).delete(validToken);
        }

        @Test
        @DisplayName("Prawidłowy token — stare hasło nie pozostaje w bazie")
        void shouldNotKeepOldPassword() {
            PasswordResetToken validToken =
                    new PasswordResetToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(1));
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            passwordResetService.resetPassword("dobryToken", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isNotEqualTo("staryHash");
        }
    }
}
