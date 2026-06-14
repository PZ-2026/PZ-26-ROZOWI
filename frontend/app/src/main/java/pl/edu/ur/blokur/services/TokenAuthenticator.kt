package pl.edu.ur.blokur.services

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import pl.edu.ur.blokur.dtos.RefreshTokenRequestDto
import javax.inject.Inject
import javax.inject.Named

/**
 * OkHttp Authenticator obsługujący automatyczne odświeżanie access tokena po HTTP 401.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
    @Named("auth") private val authApiService: AuthApiService
) : Authenticator {

    private companion object {
        const val TAG = "TokenAuthenticator"
        const val HEADER_TOKEN_REFRESHED = "X-Token-Refreshed"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(HEADER_TOKEN_REFRESHED) != null) return null
        if (response.request.url.pathSegments.contains("auth")) return null

        return tryRefreshAndRetry(response)
    }

    /**
     * Próbuje odświeżyć token i ponowić żądanie.
     * Używany zarówno przez Authenticator (401) jak i ForbiddenRetryInterceptor (403).
     */
    fun tryRefreshAndRetry(response: Response): Request? {
        val refreshToken = runBlocking { tokenStorage.getRefreshToken() }
            ?: return invalidateSessionAndAbort()

        return try {
            val refreshResponse = runBlocking {
                authApiService.refresh(RefreshTokenRequestDto(refreshToken))
            }

            if (!refreshResponse.isSuccessful) {
                Log.w(TAG, "Refresh token nieudany: HTTP ${refreshResponse.code()}")
                return invalidateSessionAndAbort()
            }

            val newTokens = refreshResponse.body() ?: return invalidateSessionAndAbort()

            runBlocking {
                tokenStorage.saveTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken,
                    role = newTokens.role
                )
            }

            Log.d(TAG, "Token odświeżony pomyślnie")

            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .header(HEADER_TOKEN_REFRESHED, "true")
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Błąd odświeżania tokenu: ${e.message}")
            invalidateSessionAndAbort()
        }
    }

    private fun invalidateSessionAndAbort(): Request? {
        runBlocking { sessionManager.invalidateSession() }
        return null
    }
}

/**
 * Interceptor obsługujący HTTP 403 — Spring Security może zwrócić 403 zamiast 401
 * gdy token JWT wygaśnie (brak Authentication w SecurityContext).
 *
 * Interceptor przechwytuje 403, próbuje odświeżyć token przez TokenAuthenticator,
 * i ponawia oryginalne żądanie z nowym tokenem.
 */
class ForbiddenRetryInterceptor @Inject constructor(
    private val tokenAuthenticator: TokenAuthenticator
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code != 403) return response
        // Nie próbuj odświeżać dla endpointów auth
        if (chain.request().url.pathSegments.contains("auth")) return response
        // Już próbowaliśmy odświeżyć
        if (chain.request().header("X-Token-Refreshed") != null) return response

        val retryRequest = tokenAuthenticator.tryRefreshAndRetry(response)
            ?: return response

        response.close()
        return chain.proceed(retryRequest)
    }
}
