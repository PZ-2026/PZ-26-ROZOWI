package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserDevice;
import pl.edu.ur.blokur.repository.UserDeviceRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Testy jednostkowe dla {@link UserDeviceService}. Weryfikują rejestrację i wyrejestrowanie tokenów
 * FCM oraz obsługę przypadków brzegowych (idempotentność, podmiana tokenu).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDeviceService — zarządzanie urządzeniami FCM")
class UserDeviceServiceTest {

    @Mock private UserDeviceRepository userDeviceRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private UserDeviceService userDeviceService;

    private UUID userId;
    private User user;
    private static final String TOKEN = "fcm-token-xyz";
    private static final String PLATFORM = "ANDROID";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@blokur.pl");
    }

    // =======================================================
    // registerDevice
    // =======================================================

    @Nested
    @DisplayName("Rejestracja urządzenia")
    class RegisterDeviceTests {

        @Test
        @DisplayName("Nowe urządzenie — zapisuje rekord z tokenem i platformą")
        void shouldSaveNewDevice() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userDeviceRepository.existsByFcmTokenAndUserId(TOKEN, userId)).thenReturn(false);
            when(userDeviceRepository.findByFcmToken(TOKEN)).thenReturn(Optional.empty());

            userDeviceService.registerDevice(userId, TOKEN, PLATFORM);

            ArgumentCaptor<UserDevice> captor = ArgumentCaptor.forClass(UserDevice.class);
            verify(userDeviceRepository).save(captor.capture());
            UserDevice saved = captor.getValue();
            org.assertj.core.api.Assertions.assertThat(saved.getUserId()).isEqualTo(userId);
            org.assertj.core.api.Assertions.assertThat(saved.getFcmToken()).isEqualTo(TOKEN);
            org.assertj.core.api.Assertions.assertThat(saved.getPlatform()).isEqualTo(PLATFORM);
        }

        @Test
        @DisplayName("Token już istnieje dla tego użytkownika — operacja idempotentna, brak zapisu")
        void shouldNotSaveWhenTokenAlreadyExistsForUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userDeviceRepository.existsByFcmTokenAndUserId(TOKEN, userId)).thenReturn(true);

            userDeviceService.registerDevice(userId, TOKEN, PLATFORM);

            verify(userDeviceRepository, never()).save(any());
        }

        @Test
        @DisplayName(
                "Token istnieje u innego użytkownika — usuwa stary rekord i zapisuje nowy dla"
                        + " bieżącego")
        void shouldReassignTokenFromOtherUser() {
            UUID otherUserId = UUID.randomUUID();
            UserDevice existingDevice = new UserDevice();
            existingDevice.setUserId(otherUserId);
            existingDevice.setFcmToken(TOKEN);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userDeviceRepository.existsByFcmTokenAndUserId(TOKEN, userId)).thenReturn(false);
            when(userDeviceRepository.findByFcmToken(TOKEN))
                    .thenReturn(Optional.of(existingDevice));

            userDeviceService.registerDevice(userId, TOKEN, PLATFORM);

            verify(userDeviceRepository).delete(existingDevice);
            verify(userDeviceRepository).save(any(UserDevice.class));
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDeviceService.registerDevice(userId, TOKEN, PLATFORM))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Użytkownik nie istnieje");

            verify(userDeviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Platforma null — zapisuje urządzenie bez platformy")
        void shouldSaveDeviceWithNullPlatform() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userDeviceRepository.existsByFcmTokenAndUserId(TOKEN, userId)).thenReturn(false);
            when(userDeviceRepository.findByFcmToken(TOKEN)).thenReturn(Optional.empty());

            userDeviceService.registerDevice(userId, TOKEN, null);

            ArgumentCaptor<UserDevice> captor = ArgumentCaptor.forClass(UserDevice.class);
            verify(userDeviceRepository).save(captor.capture());
            org.assertj.core.api.Assertions.assertThat(captor.getValue().getPlatform()).isNull();
        }
    }

    // =======================================================
    // unregisterDevice
    // =======================================================

    @Nested
    @DisplayName("Wyrejestrowanie urządzenia")
    class UnregisterDeviceTests {

        @Test
        @DisplayName("Wywołuje deleteByFcmTokenAndUserId z poprawnymi argumentami")
        void shouldCallDeleteWithCorrectArguments() {
            userDeviceService.unregisterDevice(userId, TOKEN);

            verify(userDeviceRepository).deleteByFcmTokenAndUserId(TOKEN, userId);
        }

        @Test
        @DisplayName("Nieistniejący token — nie rzuca wyjątku (operacja idempotentna)")
        void shouldNotThrowForNonExistentToken() {
            org.assertj.core.api.Assertions.assertThatCode(
                            () -> userDeviceService.unregisterDevice(userId, "brak-tokenu"))
                    .doesNotThrowAnyException();
        }
    }
}
