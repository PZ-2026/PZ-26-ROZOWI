package pl.edu.ur.blokur.infrastructure.api

import pl.edu.ur.blokur.domain.AuthException
import pl.edu.ur.blokur.domain.model.UserRole
import pl.edu.ur.blokur.domain.repository.AuthRepository
import pl.edu.ur.blokur.infrastructure.api.dto.LoginRequest
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementacja [AuthRepository] komunikująca się z backendem przez Retrofit.
 *
 * Używa dedykowanego „bare" klienta (bez interceptorów auth), dzięki czemu
 * logowanie i wylogowanie nie są narażone na pętlę odświeżania tokenów.
 *
 * Tokeny są zapisywane w [TokenStorage] (DataStore) po każdym udanym logowaniu.
 *
 * @property authApiService Retrofit service podpięty pod „bare" OkHttpClient.
 * @property tokenStorage   lokalny magazyn tokenów i roli użytkownika.
 */
internal class RetrofitAuthRepository @Inject constructor(
    @Named("auth") private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    /**
     * Loguje użytkownika przez POST /api/auth/login, zapisuje tokeny i zwraca rolę.
     *
     * @throws AuthException.InvalidCredentials gdy serwer zwróci HTTP 401.
     * @throws AuthException.AccountLocked      gdy serwer zwróci HTTP 423.
     * @throws AuthException.ApiError           przy innym kodzie błędu HTTP.
     * @throws AuthException.EmptyResponse      gdy odpowiedź 2xx nie zawiera ciała.
     * @throws AuthException.UnknownRole        gdy rola nie jest znana aplikacji.
     */
    override suspend fun login(email: String, password: String): UserRole {
        val response = authApiService.login(LoginRequest(username = email, password = password))

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

    /**
     * Usuwa tokeny z DataStore – użytkownik zostaje wylogowany lokalnie.
     *
     * Nie wykonuje żadnego żądania do backendu (backend nie posiada endpointu logout).
     */
    override suspend fun logout() {
        tokenStorage.clearTokens()
    }

    /**
     * Odczytuje rolę z DataStore bez kontaktowania się z serwerem.
     *
     * @return [UserRole] zalogowanego użytkownika lub `null`, gdy sesja nie istnieje.
     */
    override suspend fun getCurrentUserRole(): UserRole? {
        val roleString = tokenStorage.getUserRole() ?: return null
        return UserRole.entries.firstOrNull { it.name == roleString }
    }
}
