package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.NotificationConfigDto
import pl.edu.ur.blokur.dtos.UpdateNotificationConfigRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/** Retrofit interface dla endpointów konfiguracji powiadomień PUSH (dostęp: ZARZADCA). */
interface NotificationApiService {

    @GET("api/admin/notifications/settings")
    suspend fun getSettings(): Response<List<NotificationConfigDto>>

    @PATCH("api/admin/notifications/settings/{eventType}")
    suspend fun updateSetting(
        @Path("eventType") eventType: String,
        @Body request: UpdateNotificationConfigRequest
    ): Response<NotificationConfigDto>
}
