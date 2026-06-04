package pl.edu.ur.blokur.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.ur.blokur.dtos.DeviceRegistrationRequestDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis zarządzający rejestracja tokenu FCM urządzenia w backendzie.
 *
 * Schemat użycia:
 * 1. Po zalogowaniu: `registerDevice(fcmToken)`
 * 2. Po wylogowaniu: `unregisterDevice(fcmToken)`
 *
 * Błędy rejestracji są łagodne (logowane, nie przerywają flow) —
 * push notifications to funkcja dodatkowa, nie krytyczna.
 */
@Singleton
class DeviceService @Inject constructor(
    private val api: DeviceApiService
) {
    companion object {
        private const val TAG = "DeviceService"
    }

    /**
     * Rejestruje token FCM dla zalogowanego użytkownika (POST /api/devices/register).
     * Operacja idempotentna — bezpieczna przy wielokrotnym wywołaniu.
     *
     * @param fcmToken token FCM pobrany z FirebaseMessaging.getInstance().token
     * @return true jeśli rejestracja zakończyła się sukcesem, false w przypadku błędu
     */
    suspend fun registerDevice(fcmToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.registerDevice(DeviceRegistrationRequestDto(fcmToken = fcmToken))
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token zarejestrowany pomyślnie")
                true
            } else {
                Log.w(TAG, "Rejestracja FCM nieudana: HTTP ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd rejestracji FCM token: ${e.message}", e)
            false
        }
    }

    /**
     * Wyrejestrowuje token FCM po wylogowaniu (DELETE /api/devices/{token}).
     * Dzięki temu push notifications przestają docierać na to urządzenie.
     *
     * @param fcmToken token FCM do usunięcia
     * @return true jeśli wyrejestrowanie zakończyło się sukcesem
     */
    suspend fun unregisterDevice(fcmToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.unregisterDevice(fcmToken)
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token wyrejestrowany pomyślnie")
                true
            } else {
                if (response.code() == 404) {
                    Log.w(TAG, "Wyrejestrowanie FCM: token nie znaleziony (404)")
                } else {
                    Log.w(TAG, "Wyrejestrowanie FCM nieudane: HTTP ${response.code()}")
                }
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd wyrejestrowania FCM token: ${e.message}", e)
            false
        }
    }
}
