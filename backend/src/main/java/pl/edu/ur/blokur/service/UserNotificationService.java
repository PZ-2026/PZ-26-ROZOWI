package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.NotificationConfigResponse;
import pl.edu.ur.blokur.models.NotificationSetting;
import pl.edu.ur.blokur.repository.NotificationSettingRepository;

/**
 * Serwis zarządzający osobistymi ustawieniami powiadomień użytkownika.
 */
@Service
public class UserNotificationService {

    private static final Map<String, String> EVENT_LABELS =
            Map.of(
                    PushNotificationService.EVENT_OGLOSZENIE, "Ogłoszenia",
                    PushNotificationService.EVENT_ZMIANA_STATUSU, "Zmiana statusu zgłoszenia",
                    PushNotificationService.EVENT_PRZEGLAD, "Przeglądy techniczne",
                    PushNotificationService.EVENT_NOWY_DOKUMENT, "Nowe dokumenty",
                    PushNotificationService.EVENT_WSTRZYMANIE, "Wstrzymanie zgłoszenia");

    private final NotificationSettingRepository notificationSettingRepository;

    public UserNotificationService(NotificationSettingRepository notificationSettingRepository) {
        this.notificationSettingRepository = notificationSettingRepository;
    }

    /**
     * Zwraca listę wszystkich typów powiadomień z flagą czy są włączone dla danego użytkownika.
     */
    @Transactional(readOnly = true)
    public List<NotificationConfigResponse> getSettingsForUser(UUID userId) {
        return EVENT_LABELS.keySet().stream()
                .map(eventType -> {
                    boolean enabled = notificationSettingRepository
                            .findByUserIdAndEventType(userId, eventType)
                            .map(NotificationSetting::isEnabled)
                            .orElse(true); // default true
                    return new NotificationConfigResponse(eventType, enabled, EVENT_LABELS.get(eventType));
                })
                .toList();
    }

    /**
     * Aktualizuje ustawienie powiadomienia dla danego użytkownika.
     */
    @Transactional
    public NotificationConfigResponse updateSettingForUser(UUID userId, String eventType, boolean enabled) {
        if (!EVENT_LABELS.containsKey(eventType)) {
            throw new IllegalArgumentException("Nieznany typ zdarzenia: " + eventType);
        }

        NotificationSetting setting = notificationSettingRepository
                .findByUserIdAndEventType(userId, eventType)
                .orElseGet(() -> {
                    NotificationSetting newSetting = new NotificationSetting();
                    newSetting.setUserId(userId);
                    newSetting.setEventType(eventType);
                    return newSetting;
                });

        setting.setEnabled(enabled);
        notificationSettingRepository.save(setting);

        return new NotificationConfigResponse(eventType, enabled, EVENT_LABELS.get(eventType));
    }
}
