package pl.edu.ur.blokur.dto;

import lombok.Data;

/**
 * DTO zwracane przez endpoint GET /api/admin/notifications/settings. Reprezentuje globalną
 * konfigurację jednego typu powiadomień PUSH.
 */
// public record NotificationConfigResponse(String eventType, boolean enabled, String label) {}
@Data
public class NotificationConfigResponse {
    private String eventType;
    private boolean enabled;
    private String label;

    /**
     * Tworzy globalną konfiguracje powiadomień PUSH
     *
     * @param eventType wewnętrzny klucz zdarzenia (np. {@code "OGLOSZENIE"})
     * @param enabled {@code true} jeśli powiadomienia tego typu są globalnie włączone
     * @param label czytelna dla użytkownika polska etykieta typu zdarzenia
     */
    public NotificationConfigResponse(String eventType, boolean enabled, String label) {
        this.eventType = eventType;
        this.enabled = enabled;
        this.label = label;
    }
}
