package pl.edu.ur.blokur.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.repository.NotificationConfigRepository;
import pl.edu.ur.blokur.repository.NotificationSettingRepository;
import pl.edu.ur.blokur.repository.UserDeviceRepository;

/**
 * Serwis odpowiedzialny za wysyłanie powiadomień PUSH przez Firebase Cloud Messaging. Respektuje
 * ustawienia notification_settings użytkownika i pomija tokeny nieaktywne.
 */
@Service
public class PushNotificationService {

    public static final String EVENT_OGLOSZENIE = "OGLOSZENIE";
    public static final String EVENT_ZMIANA_STATUSU = "ZMIANA_STATUSU_ZGLOSZENIA";
    public static final String EVENT_PRZEGLAD = "PRZEGLAD";
    public static final String EVENT_NOWY_DOKUMENT = "NOWY_DOKUMENT";
    public static final String EVENT_WSTRZYMANIE = "WSTRZYMANIE_ZGLOSZENIA";
    public static final String EVENT_ZMIANA_ROLI = "ZMIANA_ROLI";

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationConfigRepository notificationConfigRepository;

    /**
     * Tworzy instancję serwisu.
     *
     * @param userDeviceRepository repozytorium urządzeń użytkowników
     * @param notificationSettingRepository repozytorium per-użytkownikowych ustawień powiadomień
     * @param notificationConfigRepository repozytorium globalnej konfiguracji typów powiadomień
     */
    public PushNotificationService(
            UserDeviceRepository userDeviceRepository,
            NotificationSettingRepository notificationSettingRepository,
            NotificationConfigRepository notificationConfigRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.notificationConfigRepository = notificationConfigRepository;
    }

    /**
     * Wysyła powiadomienie ciche (Data-Only PUSH) do użytkownika, omijając globalne filtry
     * powiadomień. Tego typu powiadomienie zawsze jest odbierane w tle na Androidzie.
     *
     * @param userId identyfikator użytkownika
     * @param data dane do wysłania w PUSH
     */
    @Async
    @Transactional(readOnly = true)
    public void sendSystemDataOnly(UUID userId, Map<String, String> data) {
        log.info("Zlecono wysyłkę PUSH (SystemDataOnly) do userId: {}", userId);
        if (userId == null) {
            return;
        }

        var tokens = userDeviceRepository.findFcmTokensByUserId(userId);
        if (tokens.isEmpty()) {
            log.info("Brak aktywnych tokenów FCM dla userId: {}. Zignorowano.", userId);
            return;
        }

        for (String token : tokens) {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info(
                        "Firebase nie jest zainicjalizowany — pomijam wysyłkę PUSH (token: {})", token);
                continue;
            }

            try {
                AndroidConfig androidConfig = AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build();

                Message.Builder builder = Message.builder()
                        .setToken(token)
                        .setAndroidConfig(androidConfig);
                
                if (data != null) {
                    builder.putAllData(data);
                }
                var messageId = FirebaseMessaging.getInstance().send(builder.build());
                log.info("Cichy FCM wysłany do {}: {}", userId, messageId);
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    log.info("Token FCM nieaktywny, usuwam: {}", token);
                    userDeviceRepository.deleteByFcmToken(token);
                } else {
                    log.error("Błąd wysyłki FCM (token: {}): {}", token, e.getMessage());
                }
            }
        }
    }

    /**
     * Wysyła powiadomienie PUSH asynchronicznie do wielu użytkowników. Najpierw sprawdza globalną
     * konfigurację (notification_config), a następnie per-użytkownikowe ustawienia
     * (notification_settings). Użytkownicy z wyłączonym typem zdarzenia nie otrzymują
     * powiadomienia.
     *
     * @param userIds lista identyfikatorów użytkowników
     * @param eventType typ zdarzenia (stała EVENT_*)
     * @param title tytuł powiadomienia
     * @param body treść powiadomienia
     * @param data dodatkowe dane do przesłania
     */
    @Async
    @Transactional(readOnly = true)
    public void sendToUsers(
            List<UUID> userIds,
            String eventType,
            String title,
            String body,
            Map<String, String> data) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        if (!isGloballyEnabled(eventType)) {
            log.debug("Typ powiadomienia '{}' globalnie wyłączony — pomijam wysyłkę.", eventType);
            return;
        }
        for (UUID userId : userIds) {
            if (isEnabled(userId, eventType)) {
                sendToUser(userId, title, body, data);
            }
        }
    }

    /**
     * Wysyła powiadomienie PUSH asynchronicznie do pojedynczego użytkownika. Najpierw sprawdza
     * globalną konfigurację (notification_config), a następnie per-użytkownikowe ustawienia
     * (notification_settings).
     *
     * @param userId identyfikator użytkownika
     * @param eventType typ zdarzenia
     * @param title tytuł powiadomienia
     * @param body treść powiadomienia
     * @param data dodatkowe dane
     */
    @Async
    @Transactional(readOnly = true)
    public void send(
            UUID userId, String eventType, String title, String body, Map<String, String> data) {
        if (userId == null) {
            return;
        }
        if (!isGloballyEnabled(eventType)) {
            log.debug("Typ powiadomienia '{}' globalnie wyłączony — pomijam wysyłkę.", eventType);
            return;
        }
        if (isEnabled(userId, eventType)) {
            sendToUser(userId, title, body, data);
        }
    }

    private void sendToUser(UUID userId, String title, String body, Map<String, String> data) {
        var tokens = userDeviceRepository.findFcmTokensByUserId(userId);
        for (String token : tokens) {
            sendMessage(token, title, body, data);
        }
    }

    private void sendMessage(String token, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug(
                    "Firebase nie jest zainicjalizowany — pomijam wysyłkę PUSH (token: {})", token);
            return;
        }

        try {
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build();

            Notification notification =
                    Notification.builder().setTitle(title).setBody(body).build();
            Message.Builder builder =
                    Message.builder()
                            .setToken(token)
                            .setAndroidConfig(androidConfig)
                            .setNotification(notification);
            if (data != null) {
                builder.putAllData(data);
            }
            var messageId = FirebaseMessaging.getInstance().send(builder.build());
            log.debug("FCM wysłany: {}", messageId);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.info("Token FCM nieaktywny, usuwam: {}", token);
                userDeviceRepository.deleteByFcmToken(token);
            } else {
                log.error("Błąd wysyłki FCM (token: {}): {}", token, e.getMessage());
            }
        }
    }

    private boolean isGloballyEnabled(String eventType) {
        return notificationConfigRepository
                .findByEventType(eventType)
                .map(c -> c.isEnabled())
                .orElse(true);
    }

    private boolean isEnabled(UUID userId, String eventType) {
        return notificationSettingRepository
                .findByUserIdAndEventType(userId, eventType)
                .map(s -> s.isEnabled())
                .orElse(true);
    }
}
