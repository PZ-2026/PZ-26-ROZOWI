package pl.edu.ur.blokur.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.ur.blokur.dtos.UserProfileDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis do zarządzania własnym profilem użytkownika.
 */
@Singleton
class UserService @Inject constructor(
    private val api: UserApiService
) {
    companion object {
        private const val TAG = "UserService"
    }

    /**
     * Pobiera dane aktualnie zalogowanego użytkownika.
     */
    suspend fun getMe(): UserProfileDto = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Pusta odpowiedź z serwera")
            } else {
                Log.w(TAG, "Błąd pobierania profilu: HTTP ${response.code()}")
                throw Exception("Błąd pobierania danych z serwera: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd sieciowy podczas pobierania profilu: ${e.message}", e)
            throw e
        }
    }
}
