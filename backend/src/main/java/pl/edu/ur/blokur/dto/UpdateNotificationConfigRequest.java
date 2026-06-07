package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO żądania PATCH /api/admin/notifications/settings/{eventType}. Zawiera nową wartość flagi
 * włączenia powiadomień danego typu.
 */
@Data
public class UpdateNotificationConfigRequest {
    @NotNull private Boolean enabled;
}
