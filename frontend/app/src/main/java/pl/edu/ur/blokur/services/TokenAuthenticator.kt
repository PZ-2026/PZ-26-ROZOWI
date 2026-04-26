package pl.edu.ur.blokur.services

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
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
    @Named("auth") private val authApiService: AuthApiService
) : Authenticator {

    private companion object {
        const val HEADER_TOKEN_REFRESHED = "X-Token-Refreshed"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(HEADER_TOKEN_REFRESHED) != null) return null
        if (response.request.url.pathSegments.contains("auth")) return null

        val refreshToken = runBlocking { tokenStorage.getRefreshToken() } ?: return null

        return try {
            val refreshResponse = runBlocking {
                authApiService.refresh(RefreshTokenRequestDto(refreshToken))
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
