package pl.edu.ur.blokur.dto;

/**
 * DTO zwracane przez endpoint GET /api/admin/notifications/settings. Reprezentuje globalną
 * konfigurację jednego typu powiadomień PUSH.
 *
 * @param eventType wewnętrzny klucz zdarzenia (np. {@code "OGLOSZENIE"})
 * @param enabled {@code true} jeśli powiadomienia tego typu są globalnie włączone
 * @param label czytelna dla użytkownika polska etykieta typu zdarzenia
 */
public record NotificationConfigResponse(String eventType, boolean enabled, String label) {}
