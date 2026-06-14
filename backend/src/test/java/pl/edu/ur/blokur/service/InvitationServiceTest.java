package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
@DisplayName("InvitationService — kody aktywacyjne nowych użytkowników (72 h)")
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

        testUser = new User();
        testUser.setEmail("jan@blokur.pl");
        testUser.setFirstName("Jan");
        testUser.setPasswordHash("");
    }

    @Nested
    @DisplayName("inviteUser")
    class InviteUser {

        @Test
        @DisplayName("Zapisuje 6-cyfrowy kod z ważnością 72 godzin")
        void shouldSaveSixDigitCodeWithSeventyTwoHourExpiry() {
            invitationService.inviteUser(testUser);

            ArgumentCaptor<InvitationToken> captor = ArgumentCaptor.forClass(InvitationToken.class);
            verify(tokenRepository).save(captor.capture());

            InvitationToken saved = captor.getValue();
            assertThat(saved.getToken()).matches("\\d{6}");
            assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now().plusHours(71));
            assertThat(saved.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(73));
        }

        @Test
        @DisplayName("Czyści wcześniejsze kody zaproszenia tego użytkownika")
        void shouldDeleteExistingCodesForUser() {
            invitationService.inviteUser(testUser);

            verify(tokenRepository).deleteByUser(testUser);
        }

        @Test
        @DisplayName("Zapisuje kod przypisany do właściwego użytkownika")
        void shouldSaveTokenForCorrectUser() {
            invitationService.inviteUser(testUser);

            ArgumentCaptor<InvitationToken> captor = ArgumentCaptor.forClass(InvitationToken.class);
            verify(tokenRepository).save(captor.capture());

            assertThat(captor.getValue().getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Wysyła e-mail powitalny zawierający 6-cyfrowy kod i imię")
        void shouldSendWelcomeEmailWithCode() {
            testUser.setFirstName("Katarzyna");

            invitationService.inviteUser(testUser);

            ArgumentCaptor<SimpleMailMessage> mailCaptor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            ArgumentCaptor<InvitationToken> tokenCaptor =
                    ArgumentCaptor.forClass(InvitationToken.class);
            verify(mailSender).send(mailCaptor.capture());
            verify(tokenRepository).save(tokenCaptor.capture());

            SimpleMailMessage mail = mailCaptor.getValue();
            assertThat(mail.getTo()).contains("jan@blokur.pl");
            assertThat(mail.getText()).contains("Katarzyna");
            assertThat(mail.getText()).contains(tokenCaptor.getValue().getToken());
        }
    }

    @Nested
    @DisplayName("acceptInvitation")
    class AcceptInvitation {

        @Test
        @DisplayName("Nieznany email — rzuca IllegalArgumentException")
        void shouldThrowWhenEmailNotFound() {
            when(userRepository.findByEmail("brak@blokur.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    invitationService.acceptInvitation(
                                            "brak@blokur.pl", "123456", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy kod");
        }

        @Test
        @DisplayName("Nieprawidłowy kod — rzuca IllegalArgumentException")
        void shouldThrowWhenCodeNotFound() {
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(testUser, "000000")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    invitationService.acceptInvitation(
                                            "jan@blokur.pl", "000000", "NoweHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy kod");
        }

        @Test
        @DisplayName("Wygasły kod — rzuca TokenExpiredException i usuwa kod z bazy")
        void shouldThrowAndDeleteExpiredToken() {
            InvitationToken expired =
                    new InvitationToken(testUser, "123456", LocalDateTime.now().minusHours(1));
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(testUser, "123456"))
                    .thenReturn(Optional.of(expired));

            assertThatThrownBy(
                            () ->
                                    invitationService.acceptInvitation(
                                            "jan@blokur.pl", "123456", "NoweHaslo1"))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("wygasł");

            verify(tokenRepository).delete(expired);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Prawidłowy kod — hashuje hasło, zapisuje użytkownika i usuwa kod")
        void shouldHashSaveAndConsumeToken() {
            InvitationToken valid =
                    new InvitationToken(testUser, "654321", LocalDateTime.now().plusHours(72));
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(testUser, "654321"))
                    .thenReturn(Optional.of(valid));
            when(passwordEncoder.encode("NoweHaslo1")).thenReturn("nowyHash");

            invitationService.acceptInvitation("jan@blokur.pl", "654321", "NoweHaslo1");

            assertThat(testUser.getPasswordHash()).isEqualTo("nowyHash");
            verify(userRepository).save(testUser);
            verify(tokenRepository).delete(valid);
        }

        @Test
        @DisplayName("Zużyty kod — nie może być użyty ponownie")
        void shouldNotAllowReuse() {
            InvitationToken valid =
                    new InvitationToken(testUser, "111222", LocalDateTime.now().plusHours(72));
            when(userRepository.findByEmail("jan@blokur.pl")).thenReturn(Optional.of(testUser));
            when(tokenRepository.findByUserAndToken(eq(testUser), eq("111222")))
                    .thenReturn(Optional.of(valid))
                    .thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hash");

            invitationService.acceptInvitation("jan@blokur.pl", "111222", "NoweHaslo1");

            assertThatThrownBy(
                            () ->
                                    invitationService.acceptInvitation(
                                            "jan@blokur.pl", "111222", "InneHaslo1"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
