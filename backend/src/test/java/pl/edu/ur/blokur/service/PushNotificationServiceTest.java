package pl.edu.ur.blokur.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.models.NotificationSetting;
import pl.edu.ur.blokur.repository.NotificationSettingRepository;
import pl.edu.ur.blokur.repository.UserDeviceRepository;

/**
 * Testy jednostkowe dla {@link PushNotificationService}. Weryfikują filtrowanie odbiorców według
 * notification_settings oraz zachowanie przy braku tokenów FCM.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService — serwis powiadomień PUSH")
class PushNotificationServiceTest {

    @Mock private UserDeviceRepository userDeviceRepository;
    @Mock private NotificationSettingRepository notificationSettingRepository;

    @InjectMocks private PushNotificationService pushNotificationService;

    private UUID userId;
    private static final String EVENT = PushNotificationService.EVENT_OGLOSZENIE;
    private static final String TOKEN = "test-fcm-token-abc123";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // =======================================================
    // send — pojedynczy użytkownik
    // =======================================================

    @Nested
    @DisplayName("send — wysyłka do jednego użytkownika")
    class SendSingleUserTests {

        @Test
        @DisplayName("Brak tokenów FCM — nie próbuje wysłać wiadomości")
        void shouldNotSendWhenUserHasNoTokens() {
            when(notificationSettingRepository.findByUserIdAndEventType(userId, EVENT))
                    .thenReturn(Optional.empty());
            when(userDeviceRepository.findFcmTokensByUserId(userId)).thenReturn(List.of());

            pushNotificationService.send(userId, EVENT, "Tytuł", "Treść", Map.of());

            verify(userDeviceRepository).findFcmTokensByUserId(userId);
        }

        @Test
        @DisplayName("Powiadomienia wyłączone w ustawieniach — nie pobiera tokenów")
        void shouldSkipWhenNotificationDisabled() {
            NotificationSetting setting = new NotificationSetting();
            setting.setUserId(userId);
            setting.setEventType(EVENT);
            setting.setEnabled(false);

            when(notificationSettingRepository.findByUserIdAndEventType(userId, EVENT))
                    .thenReturn(Optional.of(setting));

            pushNotificationService.send(userId, EVENT, "Tytuł", "Treść", Map.of());

            verify(userDeviceRepository, never()).findFcmTokensByUserId(any());
        }

        @Test
        @DisplayName("Powiadomienia włączone w ustawieniach — pobiera tokeny użytkownika")
        void shouldFetchTokensWhenNotificationEnabled() {
            NotificationSetting setting = new NotificationSetting();
            setting.setUserId(userId);
            setting.setEventType(EVENT);
            setting.setEnabled(true);

            when(notificationSettingRepository.findByUserIdAndEventType(userId, EVENT))
                    .thenReturn(Optional.of(setting));
            when(userDeviceRepository.findFcmTokensByUserId(userId)).thenReturn(List.of(TOKEN));

            pushNotificationService.send(userId, EVENT, "Tytuł", "Treść", Map.of());

            verify(userDeviceRepository).findFcmTokensByUserId(userId);
        }

        @Test
        @DisplayName("Brak rekordu w notification_settings — domyślnie włączone, pobiera tokeny")
        void shouldFetchTokensWhenNoSettingExists() {
            when(notificationSettingRepository.findByUserIdAndEventType(userId, EVENT))
                    .thenReturn(Optional.empty());
            when(userDeviceRepository.findFcmTokensByUserId(userId)).thenReturn(List.of(TOKEN));

            pushNotificationService.send(userId, EVENT, "Tytuł", "Treść", Map.of());

            verify(userDeviceRepository).findFcmTokensByUserId(userId);
        }

        @Test
        @DisplayName("null userId — nie wykonuje żadnych operacji")
        void shouldDoNothingForNullUserId() {
            pushNotificationService.send(null, EVENT, "Tytuł", "Treść", Map.of());

            verify(notificationSettingRepository, never())
                    .findByUserIdAndEventType(any(), anyString());
            verify(userDeviceRepository, never()).findFcmTokensByUserId(any());
        }
    }

    // =======================================================
    // sendToUsers — wielu użytkowników
    // =======================================================

    @Nested
    @DisplayName("sendToUsers — wysyłka do wielu użytkowników")
    class SendToUsersTests {

        @Test
        @DisplayName("Pusta lista użytkowników — nie wykonuje żadnych operacji")
        void shouldDoNothingForEmptyList() {
            pushNotificationService.sendToUsers(List.of(), EVENT, "Tytuł", "Treść", Map.of());

            verify(notificationSettingRepository, never())
                    .findByUserIdAndEventType(any(), anyString());
        }

        @Test
        @DisplayName("null lista użytkowników — nie wykonuje żadnych operacji")
        void shouldDoNothingForNullList() {
            pushNotificationService.sendToUsers(null, EVENT, "Tytuł", "Treść", Map.of());

            verify(notificationSettingRepository, never())
                    .findByUserIdAndEventType(any(), anyString());
        }

        @Test
        @DisplayName("Dwóch użytkowników: jeden z włączonymi, drugi z wyłączonymi powiadomieniami")
        void shouldOnlySendToUsersWithEnabledNotifications() {
            UUID userEnabled = UUID.randomUUID();
            UUID userDisabled = UUID.randomUUID();

            NotificationSetting disabledSetting = new NotificationSetting();
            disabledSetting.setUserId(userDisabled);
            disabledSetting.setEventType(EVENT);
            disabledSetting.setEnabled(false);

            when(notificationSettingRepository.findByUserIdAndEventType(userEnabled, EVENT))
                    .thenReturn(Optional.empty());
            when(notificationSettingRepository.findByUserIdAndEventType(userDisabled, EVENT))
                    .thenReturn(Optional.of(disabledSetting));
            when(userDeviceRepository.findFcmTokensByUserId(userEnabled)).thenReturn(List.of());

            pushNotificationService.sendToUsers(
                    List.of(userEnabled, userDisabled), EVENT, "Tytuł", "Treść", Map.of());

            verify(userDeviceRepository).findFcmTokensByUserId(userEnabled);
            verify(userDeviceRepository, never()).findFcmTokensByUserId(userDisabled);
        }

        @Test
        @DisplayName(
                "Wszyscy użytkownicy z wyłączonymi powiadomieniami — nie pobiera żadnych tokenów")
        void shouldNotFetchTokensWhenAllDisabled() {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            NotificationSetting off = new NotificationSetting();
            off.setEventType(EVENT);
            off.setEnabled(false);

            when(notificationSettingRepository.findByUserIdAndEventType(eq(user1), eq(EVENT)))
                    .thenReturn(Optional.of(off));
            when(notificationSettingRepository.findByUserIdAndEventType(eq(user2), eq(EVENT)))
                    .thenReturn(Optional.of(off));

            pushNotificationService.sendToUsers(
                    List.of(user1, user2), EVENT, "Tytuł", "Treść", Map.of());

            verify(userDeviceRepository, never()).findFcmTokensByUserId(any());
        }
    }

    // =======================================================
    // Stałe typów zdarzeń
    // =======================================================

    @Nested
    @DisplayName("Stałe typów zdarzeń")
    class EventTypeConstantsTests {

        @Test
        @DisplayName("Stałe EVENT_* mają oczekiwane wartości")
        void shouldHaveCorrectEventTypeValues() {
            org.assertj.core.api.Assertions.assertThat(PushNotificationService.EVENT_OGLOSZENIE)
                    .isEqualTo("OGLOSZENIE");
            org.assertj.core.api.Assertions.assertThat(PushNotificationService.EVENT_ZMIANA_STATUSU)
                    .isEqualTo("ZMIANA_STATUSU_ZGLOSZENIA");
            org.assertj.core.api.Assertions.assertThat(PushNotificationService.EVENT_PRZEGLAD)
                    .isEqualTo("PRZEGLAD");
            org.assertj.core.api.Assertions.assertThat(PushNotificationService.EVENT_NOWY_DOKUMENT)
                    .isEqualTo("NOWY_DOKUMENT");
            org.assertj.core.api.Assertions.assertThat(PushNotificationService.EVENT_WSTRZYMANIE)
                    .isEqualTo("WSTRZYMANIE_ZGLOSZENIA");
        }
    }
}
