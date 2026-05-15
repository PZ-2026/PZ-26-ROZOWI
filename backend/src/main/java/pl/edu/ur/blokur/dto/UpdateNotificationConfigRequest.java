package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO żądania PATCH /api/admin/notifications/settings/{eventType}. Zawiera nową wartość flagi
 * włączenia powiadomień danego typu.
 *
 * @param enabled {@code true} aby włączyć, {@code false} aby wyłączyć powiadomienia
 */
public record UpdateNotificationConfigRequest(@NotNull Boolean enabled) {}
