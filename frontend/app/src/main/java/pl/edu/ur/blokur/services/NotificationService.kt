package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.NotificationConfigDto
import pl.edu.ur.blokur.dtos.UpdateNotificationConfigRequest
import javax.inject.Inject
import javax.inject.Singleton

/** Serwis zarządzający globalną konfiguracją typów powiadomień PUSH. */
@Singleton
class NotificationService @Inject constructor(
    private val api: NotificationApiService
) {

    /** Pobiera listę wszystkich globalnych konfiguracji typów powiadomień. */
    suspend fun getSettings(): List<NotificationConfigDto> {
        return runCatching {
            val resp = api.getSettings()
            ApiResponseHandler.requireSuccess(resp, "Błąd pobierania ustawień powiadomień")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd pobierania ustawień powiadomień") }
    }

    /** Włącza lub wyłącza globalnie wybrany typ powiadomień. */
    suspend fun updateSetting(eventType: String, enabled: Boolean): NotificationConfigDto {
        return runCatching {
            val resp = api.updateSetting(eventType, UpdateNotificationConfigRequest(enabled))
            ApiResponseHandler.requireSuccess(resp, "Błąd aktualizacji ustawień powiadomień")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd aktualizacji ustawień powiadomień") }
    }
    /** Pobiera listę wszystkich osobistych konfiguracji typów powiadomień użytkownika. */
    suspend fun getMySettings(): List<NotificationConfigDto> {
        return runCatching {
            val resp = api.getMySettings()
            ApiResponseHandler.requireSuccess(resp, "Błąd pobierania osobistych ustawień powiadomień")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd pobierania osobistych ustawień powiadomień") }
    }

    /** Włącza lub wyłącza personalnie wybrany typ powiadomień dla zalogowanego użytkownika. */
    suspend fun updateMySetting(eventType: String, enabled: Boolean): NotificationConfigDto {
        return runCatching {
            val resp = api.updateMySetting(eventType, UpdateNotificationConfigRequest(enabled))
            ApiResponseHandler.requireSuccess(resp, "Błąd aktualizacji osobistego ustawienia powiadomień")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd aktualizacji osobistego ustawienia powiadomień") }
    }
}
