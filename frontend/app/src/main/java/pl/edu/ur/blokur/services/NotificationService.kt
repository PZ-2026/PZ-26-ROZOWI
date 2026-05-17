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
            if (!resp.isSuccessful) throw Exception("Błąd pobierania ustawień (${resp.code()})")
            resp.body() ?: emptyList()
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Włącza lub wyłącza globalnie wybrany typ powiadomień. */
    suspend fun updateSetting(eventType: String, enabled: Boolean): NotificationConfigDto {
        return runCatching {
            val resp = api.updateSetting(eventType, UpdateNotificationConfigRequest(enabled))
            if (!resp.isSuccessful) throw Exception(
                when (resp.code()) {
                    404 -> "Nieznany typ powiadomienia."
                    else -> "Błąd aktualizacji (${resp.code()})"
                }
            )
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
