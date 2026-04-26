package pl.edu.ur.blokur.services

import com.google.gson.Gson
import pl.edu.ur.blokur.dtos.AuthException
import pl.edu.ur.blokur.dtos.ForgotPasswordRequestDto
import pl.edu.ur.blokur.dtos.LoginRequestDto
import pl.edu.ur.blokur.dtos.MessageResponseDto
import pl.edu.ur.blokur.dtos.ResetPasswordRequestDto
import pl.edu.ur.blokur.dtos.UserRole
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Serwis autoryzacji — obsługuje logowanie, wylogowanie, odczyt roli,
 * oraz przepływ odzyskiwania hasła.
 */
@Singleton
class AuthService @Inject constructor(
    @Named("auth") private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) {

    /**
     * Loguje użytkownika przez POST /api/auth/login, zapisuje tokeny i zwraca rolę.
     */
    suspend fun login(email: String, password: String): UserRole {
        val response = authApiService.login(LoginRequestDto(username = email, password = password))

        if (!response.isSuccessful) {
            throw when (response.code()) {
                401 -> AuthException.InvalidCredentials
                423 -> AuthException.AccountLocked
                else -> AuthException.ApiError(response.code())
            }
        }

        val body = response.body() ?: throw AuthException.EmptyResponse

        val role = UserRole.entries.firstOrNull { it.name == body.role }
            ?: throw AuthException.UnknownRole(body.role)

        tokenStorage.saveTokens(
            accessToken = body.token,
            refreshToken = body.refreshToken,
            role = body.role
        )

        return role
    }

    /** Wylogowanie — usuwa tokeny z DataStore. */
    suspend fun logout() {
        tokenStorage.clearTokens()
    }

    /** Odczytuje rolę z DataStore bez kontaktowania się z serwerem. */
    suspend fun getCurrentUserRole(): UserRole? {
        val roleString = tokenStorage.getUserRole() ?: return null
        return UserRole.entries.firstOrNull { it.name == roleString }
    }

    /**
     * Wysyła żądanie resetowania hasła — POST /api/auth/forgot-password.
     *
     * Backend zawsze zwraca 200 OK (nie ujawnia czy e-mail istnieje).
     * @return komunikat z serwera.
     */
    suspend fun forgotPassword(email: String): String {
        val response = authApiService.forgotPassword(ForgotPasswordRequestDto(email))

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val message = try {
                Gson().fromJson(errorBody, MessageResponseDto::class.java).message
            } catch (_: Exception) {
                "Błąd serwera: ${response.code()}"
            }
            throw Exception(message)
        }

        return response.body()?.message
            ?: "Jeśli podany adres e-mail istnieje w systemie, wysłaliśmy link do resetowania hasła."
    }

    /**
     * Resetuje hasło — POST /api/auth/reset-password.
     *
     * @param token   token z linku mailowego.
     * @param newPassword nowe hasło (min. 8 znaków).
     * @return komunikat z serwera.
     */
    suspend fun resetPassword(token: String, newPassword: String): String {
        val response = authApiService.resetPassword(
            ResetPasswordRequestDto(token = token, newPassword = newPassword)
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val message = try {
                Gson().fromJson(errorBody, MessageResponseDto::class.java).message
            } catch (_: Exception) {
                "Błąd serwera: ${response.code()}"
            }
            throw Exception(message)
        }

        return response.body()?.message
            ?: "Hasło zostało zmienione. Możesz się teraz zalogować."
    }
}
