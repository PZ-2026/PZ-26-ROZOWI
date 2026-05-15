package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.NotificationConfigResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.NotificationConfig;
import pl.edu.ur.blokur.repository.NotificationConfigRepository;

/**
 * Serwis zarządzający globalną konfiguracją typów powiadomień PUSH. Umożliwia zarządcy
 * włączanie/wyłączanie poszczególnych typów zdarzeń bez ingerencji w kod.
 */
@Service
public class NotificationConfigService {

    private static final Map<String, String> EVENT_LABELS =
            Map.of(
                    "OGLOSZENIE", "Ogłoszenia",
                    "ZMIANA_STATUSU_ZGLOSZENIA", "Zmiana statusu zgłoszenia",
                    "PRZEGLAD", "Przeglądy techniczne",
                    "NOWY_DOKUMENT", "Nowe dokumenty",
                    "WSTRZYMANIE_ZGLOSZENIA", "Wstrzymanie zgłoszenia");

    private final NotificationConfigRepository notificationConfigRepository;

    /**
     * Tworzy instancję serwisu.
     *
     * @param notificationConfigRepository repozytorium globalnej konfiguracji powiadomień
     */
    public NotificationConfigService(NotificationConfigRepository notificationConfigRepository) {
        this.notificationConfigRepository = notificationConfigRepository;
    }

    /**
     * Zwraca listę wszystkich globalnych konfiguracji typów powiadomień.
     *
     * @return lista odpowiedzi DTO z etykietami i stanem włączenia
     */
    @Transactional(readOnly = true)
    public List<NotificationConfigResponse> getAll() {
        return notificationConfigRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Aktualizuje flagę włączenia dla podanego typu zdarzenia.
     *
     * @param eventType klucz zdarzenia (np. {@code "OGLOSZENIE"})
     * @param enabled nowa wartość flagi
     * @return zaktualizowane DTO konfiguracji
     * @throws NotFoundException gdy podany typ zdarzenia nie istnieje w bazie
     */
    @Transactional
    public NotificationConfigResponse update(String eventType, boolean enabled) {
        NotificationConfig config =
                notificationConfigRepository
                        .findByEventType(eventType)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Nieznany typ zdarzenia: " + eventType));
        config.setEnabled(enabled);
        return toResponse(notificationConfigRepository.save(config));
    }

    private NotificationConfigResponse toResponse(NotificationConfig config) {
        String label = EVENT_LABELS.getOrDefault(config.getEventType(), config.getEventType());
        return new NotificationConfigResponse(config.getEventType(), config.isEnabled(), label);
    }
}
