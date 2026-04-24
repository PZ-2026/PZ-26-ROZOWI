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
import pl.edu.ur.blokur.models.RefreshToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.RefreshTokenRepository;
import pl.edu.ur.blokur.security.JwtService;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @Mock private JwtService jwtService;

    @InjectMocks private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("jan@blokur.pl");
        testUser.setRole("MIESZKANIEC");
    }

    @Nested
    @DisplayName("createRefreshToken")
    class CreateRefreshToken {

        @Test
        @DisplayName("Zapisuje token powiązany z użytkownikiem")
        void shouldSaveTokenForUser() {
            when(jwtService.generateRefreshTokenValue()).thenReturn("token-value");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            refreshTokenService.createRefreshToken(testUser);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Nowy token ma revoked=false")
        void shouldCreateTokenWithRevokedFalse() {
            when(jwtService.generateRefreshTokenValue()).thenReturn("token-value");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            refreshTokenService.createRefreshToken(testUser);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().isRevoked()).isFalse();
        }

        @Test
        @DisplayName("Używa wartości tokena z JwtService")
        void shouldUseTokenValueFromJwtService() {
            when(jwtService.generateRefreshTokenValue()).thenReturn("wygenerowany-token");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken result = refreshTokenService.createRefreshToken(testUser);

            assertThat(result.getToken()).isEqualTo("wygenerowany-token");
        }
    }

    @Nested
    @DisplayName("exchange — wymiana tokena")
    class Exchange {

        @Test
        @DisplayName("Nieistniejący token — rzuca IllegalArgumentException")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("nieznany")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.exchange("nieznany"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nieprawidłowy refresh token");
        }

        @Test
        @DisplayName("Token unieważniony — rzuca IllegalArgumentException")
        void shouldThrowWhenTokenRevoked() {
            RefreshToken revoked =
                    new RefreshToken(testUser, "token", LocalDateTime.now().plusDays(30));
            revoked.setRevoked(true);
            when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(revoked));

            assertThatThrownBy(() -> refreshTokenService.exchange("token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unieważniony");
        }

        @Test
        @DisplayName("Token wygasły — rzuca IllegalArgumentException i usuwa token z bazy")
        void shouldThrowAndDeleteExpiredToken() {
            RefreshToken expired =
                    new RefreshToken(testUser, "token", LocalDateTime.now().minusSeconds(1));
            when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> refreshTokenService.exchange("token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("wygasł");

            verify(refreshTokenRepository).delete(expired);
        }

        @Test
        @DisplayName("Prawidłowy token — unieważnia stary token")
        void shouldRevokeOldToken() {
            RefreshToken valid =
                    new RefreshToken(testUser, "stary-token", LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.findByToken("stary-token")).thenReturn(Optional.of(valid));
            when(jwtService.generateToken(any(), any())).thenReturn("nowy-access");
            when(jwtService.generateRefreshTokenValue()).thenReturn("nowy-refresh");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            refreshTokenService.exchange("stary-token");

            assertThat(valid.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("Prawidłowy token — zwraca nowy access token wygenerowany dla użytkownika")
        void shouldReturnNewAccessToken() {
            RefreshToken valid =
                    new RefreshToken(testUser, "stary-token", LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.findByToken("stary-token")).thenReturn(Optional.of(valid));
            when(jwtService.generateToken("jan@blokur.pl", "MIESZKANIEC"))
                    .thenReturn("nowy-access");
            when(jwtService.generateRefreshTokenValue()).thenReturn("nowy-refresh");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshTokenService.TokenPair pair = refreshTokenService.exchange("stary-token");

            assertThat(pair.accessToken()).isEqualTo("nowy-access");
        }

        @Test
        @DisplayName("Prawidłowy token — zwraca nowy refresh token różny od starego")
        void shouldReturnNewRefreshToken() {
            RefreshToken valid =
                    new RefreshToken(testUser, "stary-token", LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.findByToken("stary-token")).thenReturn(Optional.of(valid));
            when(jwtService.generateToken(any(), any())).thenReturn("nowy-access");
            when(jwtService.generateRefreshTokenValue()).thenReturn("nowy-refresh");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshTokenService.TokenPair pair = refreshTokenService.exchange("stary-token");

            assertThat(pair.refreshToken()).isEqualTo("nowy-refresh");
            assertThat(pair.refreshToken()).isNotEqualTo("stary-token");
        }

        @Test
        @DisplayName("Prawidłowy token — zwraca rolę użytkownika")
        void shouldReturnUserRole() {
            RefreshToken valid =
                    new RefreshToken(testUser, "token", LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(valid));
            when(jwtService.generateToken(any(), any())).thenReturn("access");
            when(jwtService.generateRefreshTokenValue()).thenReturn("refresh");
            when(jwtService.getRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(30));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshTokenService.TokenPair pair = refreshTokenService.exchange("token");

            assertThat(pair.role()).isEqualTo("MIESZKANIEC");
        }
    }
}
