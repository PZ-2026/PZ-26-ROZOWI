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
import pl.edu.ur.blokur.exception.TokenExpiredException;
import pl.edu.ur.blokur.models.InvitationToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.InvitationTokenRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationService — zaproszenia nowych użytkowników (72 h)")
class InvitationServiceTest {

    @Mock private InvitationTokenRepository tokenRepository;

    @Mock private UserRepository userRepository;

    @Mock private JavaMailSender mailSender;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private InvitationService invitationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "fromAddress", "noreply@blokur.pl");
        ReflectionTestUtils.setField(
                invitationService, "inviteBaseUrl", "https://blokur.pl/invite");

        testUser = new User();
        testUser.setEmail("jan@blokur.pl");
        testUser.setFirstName("Jan");
        testUser.setPasswordHash("");
    }

    // -------------------------------------------------------
    // inviteUser
    // -------------------------------------------------------

    @Nested
    @DisplayName("inviteUser")
    class InviteUser {

        @Test
        @DisplayName("Zapisuje token zaproszenia z ważnością 72 godzin")
        void shouldSaveTokenWithSeventyTwoHourExpiry() {
            invitationService.inviteUser(testUser);

            ArgumentCaptor<InvitationToken> captor =
                    ArgumentCaptor.forClass(InvitationToken.class);
            verify(tokenRepository).save(captor.capture());

            InvitationToken saved = captor.getValue();
            assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now().plusHours(71));
            assertThat(saved.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(73));
        }

        @Test
        @DisplayName("Zapisuje token przypisany do właściwego użytkownika")
        void shouldSaveTokenForCorrectUser() {
            invitationService.inviteUser(testUser);

            ArgumentCaptor<InvitationToken> captor =
                    ArgumentCaptor.forClass(InvitationToken.class);
            verify(tokenRepository).save(captor.capture());

            assertThat(captor.getValue().getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Wysyła e-mail powitalny na adres użytkownika")
        void shouldSendWelcomeEmailToUser() {
            invitationService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> captor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getTo()).contains("jan@blokur.pl");
        }

        @Test
        @DisplayName("E-mail powitalny zawiera link /invite/{token}")
        void shouldIncludeInviteLinkInEmail() {
            invitationService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> mailCaptor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            ArgumentCaptor<InvitationToken> tokenCaptor =
                    ArgumentCaptor.forClass(InvitationToken.class);
            verify(mailSender).send(mailCaptor.capture());
            verify(tokenRepository).save(tokenCaptor.capture());

            String token = tokenCaptor.getValue().getToken();
            assertThat(mailCaptor.getValue().getText())
                    .contains("https://blokur.pl/invite/" + token);
        }

        @Test
        @DisplayName("E-mail powitalny zawiera imię użytkownika")
        void shouldIncludeFirstNameInEmail() {
            testUser.setFirstName("Katarzyna");

            invitationService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> captor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            assertThat(captor.getValue().getText()).contains("Katarzyna");
        }

        @Test
        @DisplayName("Każde zaproszenie generuje unikalny token")
        void shouldGenerateUniqueTokenEachTime() {
            User secondUser = new User();
            secondUser.setEmail("anna@blokur.pl");
            secondUser.setFirstName("Anna");

            invitationService.inviteUser(testUser);
            invitationService.inviteUser(secondUser);

            ArgumentCaptor<InvitationToken> captor =
                    ArgumentCaptor.forClass(InvitationToken.class);
            verify(tokenRepository, times(2)).save(captor.capture());

            String token1 = captor.getAllValues().get(0).getToken();
            String token2 = captor.getAllValues().get(1).getToken();
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // -------------------------------------------------------
    // acceptInvitation
    // -------------------------------------------------------

    @Nested
    @DisplayName("acceptInvitation")
    class AcceptInvitation {

        @Test
        @DisplayName("Nieprawidłowy token — rzuca IllegalArgumentException")
        void shouldThrowWhenTokenNotFound() {
            when(tokenRepository.findByToken("nieistniejacy")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    invitationService.acceptInvitation(
                                            "nieistniejacy", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy token");
        }

        @Test
        @DisplayName("Wygasły token — rzuca TokenExpiredException (410) i usuwa token z bazy")
        void shouldThrowAndDeleteExpiredToken() {
            InvitationToken expiredToken =
                    new InvitationToken(
                            testUser, "wygasly", LocalDateTime.now().minusHours(1));
            when(tokenRepository.findByToken("wygasly")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(
                            () -> invitationService.acceptInvitation("wygasly", "NoweHaslo1"))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("wygasł");

            verify(tokenRepository).delete(expiredToken);
        }

        @Test
        @DisplayName("Wygasły token — nie ustawia hasła użytkownika")
        void shouldNotSetPasswordForExpiredToken() {
            InvitationToken expiredToken =
                    new InvitationToken(
                            testUser, "wygasly", LocalDateTime.now().minusHours(1));
            when(tokenRepository.findByToken("wygasly")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(
                    () -> invitationService.acceptInvitation("wygasly", "NoweHaslo1"));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Prawidłowy token — hashuje nowe hasło i zapisuje użytkownika")
        void shouldHashAndSaveNewPassword() {
            InvitationToken validToken =
                    new InvitationToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(72));
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            invitationService.acceptInvitation("dobryToken", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isEqualTo("nowyHash");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Prawidłowy token — usuwa token po ustawieniu hasła (brak możliwości ponownego użycia)")
        void shouldDeleteTokenAfterSuccessfulAccept() {
            InvitationToken validToken =
                    new InvitationToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(72));
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode(any())).thenReturn("hash");

            invitationService.acceptInvitation("dobryToken", "NoweHaslo1");

            verify(tokenRepository).delete(validToken);
        }

        @Test
        @DisplayName("Zużyty token — nie może być użyty ponownie (token usuniętym z bazy)")
        void shouldNotAllowReuse() {
            InvitationToken validToken =
                    new InvitationToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(72));
            when(tokenRepository.findByToken("dobryToken"))
                    .thenReturn(Optional.of(validToken))
                    .thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hash");

            invitationService.acceptInvitation("dobryToken", "NoweHaslo1");

            assertThatThrownBy(
                            () -> invitationService.acceptInvitation("dobryToken", "InneHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Prawidłowy token — stare (puste) hasło nie pozostaje w bazie")
        void shouldReplaceEmptyPassword() {
            InvitationToken validToken =
                    new InvitationToken(
                            testUser, "dobryToken", LocalDateTime.now().plusHours(72));
            when(tokenRepository.findByToken("dobryToken")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            invitationService.acceptInvitation("dobryToken", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isNotEqualTo("");
        }
    }
}
