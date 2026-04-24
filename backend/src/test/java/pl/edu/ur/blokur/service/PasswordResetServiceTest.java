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
@DisplayName("PasswordResetService — reset hasła przez e-mail")
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

<<<<<<< HEAD
            assertThatThrownBy(() -> passwordResetService.resetPassword("nieistniejacy", "NoweHaslo1"))
=======
            assertThatThrownBy(
                            () -> passwordResetService.resetPassword("nieistniejacy", "NoweHaslo1"))
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy token");
        }

        @Test
        @DisplayName("Wygasły token — rzuca IllegalArgumentException i usuwa token z bazy")
        void shouldThrowAndDeleteExpiredToken() {
<<<<<<< HEAD
            PasswordResetToken expiredToken = new PasswordResetToken(
                    testUser, "wygasly", LocalDateTime.now().minusHours(1));
=======
            PasswordResetToken expiredToken =
                    new PasswordResetToken(testUser, "wygasly", LocalDateTime.now().minusHours(1));
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(tokenRepository.findByToken("wygasly")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> passwordResetService.resetPassword("wygasly", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("wygasł");

            verify(tokenRepository).delete(expiredToken);
        }

        @Test
        @DisplayName("Prawidłowy token — hashuje nowe hasło i zapisuje użytkownika")
        void shouldHashAndSaveNewPassword() {
<<<<<<< HEAD
            PasswordResetToken validToken = new PasswordResetToken(
                    testUser, "dobryToken", LocalDateTime.now().plusHours(1));
=======
            PasswordResetToken validToken =
                    new PasswordResetToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(1));
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            passwordResetService.resetPassword("dobryToken", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isEqualTo("nowyHash");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Prawidłowy token — usuwa token po zmianie hasła")
        void shouldDeleteTokenAfterSuccessfulReset() {
<<<<<<< HEAD
            PasswordResetToken validToken = new PasswordResetToken(
                    testUser, "dobryToken", LocalDateTime.now().plusHours(1));
=======
            PasswordResetToken validToken =
                    new PasswordResetToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(1));
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode(any())).thenReturn("hash");

            passwordResetService.resetPassword("dobryToken", "NoweHaslo1");

            verify(tokenRepository).delete(validToken);
        }

        @Test
        @DisplayName("Prawidłowy token — stare hasło nie pozostaje w bazie")
        void shouldNotKeepOldPassword() {
<<<<<<< HEAD
            PasswordResetToken validToken = new PasswordResetToken(
                    testUser, "dobryToken", LocalDateTime.now().plusHours(1));
=======
            PasswordResetToken validToken =
                    new PasswordResetToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(1));
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            passwordResetService.resetPassword("dobryToken", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isNotEqualTo("staryHash");
        }
    }

    // -------------------------------------------------------
    // inviteUser
    // -------------------------------------------------------

    @Nested
    @DisplayName("inviteUser — zaproszenie nowego użytkownika")
    class InviteUser {

        @Test
        @DisplayName("Zapisuje token zaproszenia z ważnością 72 godzin")
        void shouldSaveTokenWithSeventyTwoHourExpiry() {
            testUser.setFirstName("Jan");

            passwordResetService.inviteUser(testUser);

            ArgumentCaptor<PasswordResetToken> captor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());

            PasswordResetToken saved = captor.getValue();
            assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now().plusHours(71));
            assertThat(saved.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(73));
        }

        @Test
        @DisplayName("Zapisuje token przypisany do właściwego użytkownika")
        void shouldSaveTokenForCorrectUser() {
            testUser.setFirstName("Jan");

            passwordResetService.inviteUser(testUser);

            ArgumentCaptor<PasswordResetToken> captor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());

            assertThat(captor.getValue().getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Wysyła e-mail powitalny na adres użytkownika")
        void shouldSendWelcomeEmailToUser() {
            testUser.setFirstName("Jan");

            passwordResetService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> captor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getTo()).contains("jan@blokur.pl");
        }

        @Test
        @DisplayName("E-mail powitalny zawiera link z tokenem")
        void shouldIncludeTokenLinkInEmail() {
            testUser.setFirstName("Jan");

            passwordResetService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> mailCaptor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            ArgumentCaptor<PasswordResetToken> tokenCaptor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(mailSender).send(mailCaptor.capture());
            verify(tokenRepository).save(tokenCaptor.capture());

            String token = tokenCaptor.getValue().getToken();
            assertThat(mailCaptor.getValue().getText())
                    .contains("https://blokur.pl/reset?token=" + token);
        }

        @Test
        @DisplayName("E-mail powitalny zawiera imię użytkownika")
        void shouldIncludeFirstNameInEmail() {
            testUser.setFirstName("Katarzyna");

            passwordResetService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> captor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getText()).contains("Katarzyna");
        }

        @Test
        @DisplayName("Każde zaproszenie generuje unikalny token")
        void shouldGenerateUniqueTokenEachTime() {
            testUser.setFirstName("Jan");
            User secondUser = new User();
            secondUser.setEmail("anna@blokur.pl");
            secondUser.setFirstName("Anna");

            passwordResetService.inviteUser(testUser);
            passwordResetService.inviteUser(secondUser);

            ArgumentCaptor<PasswordResetToken> captor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());

            String token1 = captor.getAllValues().get(0).getToken();
            String token2 = captor.getAllValues().get(1).getToken();
            assertThat(token1).isNotEqualTo(token2);
        }
    }
}
