package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.DeviceRegistrationRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface dla endpointów zarządzania urządzeniami FCM.
 *
 * - POST   /api/devices/register  — rejestracja tokenu FCM po zalogowaniu
 * - DELETE /api/devices/{token}   — wyrejestrowanie tokenu po wylogowaniu
 */
interface DeviceApiService {

    /**
     * POST /api/devices/register — rejestruje token FCM urządzenia dla zalogowanego użytkownika.
     * Należy wywołać natychmiast po pomyślnym zalogowaniu (gdy FCM token jest dostępny).
     * Operacja idempotentna — bezpieczna przy wielokrotnym wywołaniu z tym samym tokenem.
     */
    @POST("/api/devices/register")
    suspend fun registerDevice(
        @Body request: DeviceRegistrationRequestDto
    ): Response<Unit>

    /**
     * DELETE /api/devices/{token} — wyrejestrowuje token FCM po wylogowaniu.
     * Dzięki temu push notifications przestają docierać na to urządzenie.
     */
    @DELETE("/api/devices/{token}")
    suspend fun unregisterDevice(@Path("token") fcmToken: String): Response<Unit>
}
