package pl.edu.ur.blokur.repository

import pl.edu.ur.blokur.data.UserPreferences
import pl.edu.ur.blokur.network.ApiService
import pl.edu.ur.blokur.network.dto.LoginRequest

class AuthRepository(
    private val api: ApiService,
    private val userPrefs: UserPreferences,
) {
    suspend fun login(loginRequest: LoginRequest): Result<Unit> {
        return try {
            val response = api.login(loginRequest)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    userPrefs.saveAuthData(body.token, body.role)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Brak danych uwierzytelniających w odpowiedzi"))
                }
            } else {
                Result.failure(Exception("Błędny email lub hasło"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPrefs.clearAuthData()
    }
}
