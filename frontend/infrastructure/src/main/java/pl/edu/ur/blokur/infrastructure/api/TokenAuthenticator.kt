package pl.edu.ur.blokur.infrastructure.api

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import pl.edu.ur.blokur.infrastructure.api.dto.RefreshTokenRequest
import javax.inject.Inject
import javax.inject.Named

/**
 * OkHttp [Authenticator] obsługujący automatyczne odświeżanie access tokena po HTTP 401.
 *
 * Mechanizm działania:
 * 1. OkHttp wykrywa odpowiedź 401 i wywołuje `authenticate()`.
 * 2. Authenticator pobiera refresh token z [TokenStorage].
 * 3. Wykonuje żądanie POST /api/auth/refresh przez dedykowany „bare" klient
 *    (bez interceptorów auth, by uniknąć rekurencji).
 * 4. Zapisuje nowe tokeny i ponawia oryginalne żądanie z nowym access tokenem.
 *
 * Zabezpieczenia przed pętlą nieskończoną:
 * - Jeśli żądanie zawiera już nagłówek `X-Token-Refreshed`, `authenticate` zwraca `null`
 *   i żądanie nie jest ponawiane.
 * - Jeśli refresh token nie istnieje lub odświeżanie się nie powiedzie, zwracane jest `null`.
 *
 * @property tokenStorage   lokalny magazyn tokenów JWT.
 * @property authApiService Retrofit service podpięty pod „bare" OkHttpClient (bez auth).
 */
internal class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    @Named("auth") private val authApiService: AuthApiService
) : Authenticator {

    /** Nagłówek-sentinel zapobiegający wielokrotnemu odświeżeniu dla tego samego żądania. */
    private companion object {
        const val HEADER_TOKEN_REFRESHED = "X-Token-Refreshed"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Nie odświeżaj, jeśli to już było ponowione żądanie lub dotyczy endpointów auth
        if (response.request.header(HEADER_TOKEN_REFRESHED) != null) return null
        if (response.request.url.pathSegments.contains("auth")) return null

        val refreshToken = runBlocking { tokenStorage.getRefreshToken() } ?: return null

        return try {
            val refreshResponse = runBlocking {
                authApiService.refresh(RefreshTokenRequest(refreshToken))
            }

            if (!refreshResponse.isSuccessful) return null

            val newTokens = refreshResponse.body() ?: return null

            runBlocking {
                tokenStorage.saveTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken,
                    role = newTokens.role
                )
            }

            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .header(HEADER_TOKEN_REFRESHED, "true")
                .build()
        } catch (e: Exception) {
            null
        }
    }
}
