package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AuthException
import pl.edu.ur.blokur.dtos.LoginRequestDto
import pl.edu.ur.blokur.dtos.UserRole
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Serwis autoryzacji — obsługuje logowanie, wylogowanie i odczyt roli.
 *
 * Bezpośrednia implementacja (bez interfejsu) korzystająca z Retrofit i TokenStorage.
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
}
