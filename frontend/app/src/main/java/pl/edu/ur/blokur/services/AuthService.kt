package pl.edu.ur.blokur.services

import com.google.gson.Gson
import pl.edu.ur.blokur.dtos.AuthException
import pl.edu.ur.blokur.dtos.ForgotPasswordRequestDto
import pl.edu.ur.blokur.dtos.LoginRequestDto
import pl.edu.ur.blokur.dtos.MessageResponseDto
import pl.edu.ur.blokur.dtos.ResetPasswordRequestDto
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.dtos.isExpiredTokenMessage
import retrofit2.Response
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

    private val gson = Gson()

    /**
     * Loguje użytkownika przez POST /api/auth/login, zapisuje tokeny i zwraca rolę.
     */
    suspend fun login(email: String, password: String): UserRole {
        val response = authApiService.login(LoginRequestDto(username = email, password = password))

        if (!response.isSuccessful) {
            val serverMessage = parseMessage(response)
            throw when (response.code()) {
                401 -> AuthException.InvalidCredentials
                423 -> AuthException.AccountLocked
                429 -> AuthException.RateLimited(parseRetryAfter(response))
                else -> {
                    if (!serverMessage.isNullOrBlank()) {
                        Exception(serverMessage)
                    } else {
                        AuthException.ApiError(response.code())
                    }
                }
            }
        }

        val body = response.body() ?: throw AuthException.EmptyResponse

        val role = UserRole.entries.firstOrNull { it.name == body.role }
            ?: throw AuthException.UnknownRole(body.role)

        tokenStorage.saveTokens(
            accessToken = body.token,
            refreshToken = body.refreshToken,
            role = body.role,
            email = email
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
            throw mapAuthFailure(response, "Nie udało się wysłać linku resetującego")
        }
        return response.body()?.message
            ?: "Jeśli podany adres e-mail istnieje w systemie, wysłaliśmy link do resetowania hasła."
    }

    /**
     * Resetuje hasło — POST /api/auth/reset-password.
     *
     * @param email adres e-mail, na który wysłany został kod.
     * @param code  6-cyfrowy kod z wiadomości e-mail.
     * @param newPassword nowe hasło (min. 8 znaków).
     */
    suspend fun resetPassword(email: String, code: String, newPassword: String): String {
        val response = authApiService.resetPassword(
            ResetPasswordRequestDto(email = email, code = code, newPassword = newPassword)
        )
        if (!response.isSuccessful) {
            throw mapAuthFailure(response, "Nie udało się zmienić hasła", checkExpiredToken = true)
        }
        return response.body()?.message
            ?: "Hasło zostało zmienione. Możesz się teraz zalogować."
    }

    /**
     * Ustawia hasło i akceptuje zaproszenie do systemu.
     *
     * @param email adres e-mail nowego konta.
     * @param code  6-cyfrowy kod aktywacyjny z wiadomości e-mail.
     * @param newPassword nowe hasło użytkownika.
     */
    suspend fun acceptInvitation(email: String, code: String, newPassword: String): String {
        val response = authApiService.acceptInvitation(
            pl.edu.ur.blokur.dtos.AcceptInvitationRequestDto(
                email = email,
                code = code,
                newPassword = newPassword
            )
        )
        if (!response.isSuccessful) {
            throw mapAuthFailure(response, "Nie udało się aktywować konta", checkExpiredToken = true)
        }
        return response.body()?.message
            ?: "Konto aktywowane. Możesz się teraz zalogować."
    }

    private fun parseRetryAfter(response: Response<*>): Int? =
        response.headers()["Retry-After"]?.toIntOrNull()

    private fun parseMessage(response: Response<*>): String? {
        val errorBody = response.errorBody()?.string() ?: return null
        return try {
            gson.fromJson(errorBody, MessageResponseDto::class.java).message
        } catch (_: Exception) {
            null
        }
    }

    private fun mapAuthFailure(
        response: Response<*>,
        defaultMessage: String,
        checkExpiredToken: Boolean = false
    ): Exception {
        when (response.code()) {
            429 -> return AuthException.RateLimited(parseRetryAfter(response))
        }
        val message = parseMessage(response)
            ?: "Błąd serwera: ${response.code()}"
        if (checkExpiredToken && isExpiredTokenMessage(message)) {
            return AuthException.TokenExpired(message)
        }
        return Exception(message)
    }
}
