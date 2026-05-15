package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.NotificationConfigResponse;
import pl.edu.ur.blokur.dto.UpdateNotificationConfigRequest;
import pl.edu.ur.blokur.service.NotificationConfigService;

/**
 * Kontroler administracyjny do zarządzania globalną konfiguracją typów powiadomień PUSH. Dostępny
 * wyłącznie dla zarządcy (rola ZARZADCA).
 */
@RestController
@RequestMapping("/api/admin/notifications/settings")
@PreAuthorize("hasRole('ZARZADCA')")
public class AdminNotificationController {

    private final NotificationConfigService notificationConfigService;

    /**
     * Tworzy instancję kontrolera.
     *
     * @param notificationConfigService serwis konfiguracji powiadomień
     */
    public AdminNotificationController(NotificationConfigService notificationConfigService) {
        this.notificationConfigService = notificationConfigService;
    }

    /**
     * Zwraca listę wszystkich globalnych konfiguracji typów powiadomień.
     *
     * @return 200 z listą konfiguracji
     */
    @GetMapping
    public ResponseEntity<List<NotificationConfigResponse>> getAll() {
        return ResponseEntity.ok(notificationConfigService.getAll());
    }

    /**
     * Aktualizuje flagę włączenia dla podanego typu zdarzenia.
     *
     * @param eventType klucz zdarzenia (np. {@code OGLOSZENIE})
     * @param request żądanie zawierające nową wartość flagi
     * @return 200 z zaktualizowaną konfiguracją
     */
    @PatchMapping("/{eventType}")
    public ResponseEntity<NotificationConfigResponse> update(
            @PathVariable String eventType,
            @Valid @RequestBody UpdateNotificationConfigRequest request) {
        return ResponseEntity.ok(notificationConfigService.update(eventType, request.enabled()));
    }
}
