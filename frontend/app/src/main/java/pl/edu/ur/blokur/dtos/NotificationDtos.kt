package pl.edu.ur.blokur.dtos

/** DTO reprezentujące globalną konfigurację jednego typu powiadomień PUSH. */
data class NotificationConfigDto(
    val eventType: String,
    val enabled: Boolean,
    val label: String
)

/** Żądanie zmiany flagi włączenia dla wybranego typu powiadomień. */
data class UpdateNotificationConfigRequest(
    val enabled: Boolean
)
