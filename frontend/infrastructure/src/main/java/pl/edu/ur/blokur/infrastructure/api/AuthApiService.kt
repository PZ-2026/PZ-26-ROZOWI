package pl.edu.ur.blokur.infrastructure.api

import pl.edu.ur.blokur.infrastructure.api.dto.AuthApiResponse
import pl.edu.ur.blokur.infrastructure.api.dto.LoginRequest
import pl.edu.ur.blokur.infrastructure.api.dto.RefreshTokenRequest
import pl.edu.ur.blokur.infrastructure.api.dto.TokenPairResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface dla endpointów autoryzacji.
 *
 * Instancje tworzone są przez Hilt (patrz [NetworkModule]) – jedna bez interceptorów
 * (do logowania i odświeżania tokenów) i jedna z pełnym stosem auth (do pozostałych żądań).
 */
internal interface AuthApiService {

    /**
     * Loguje użytkownika i zwraca parę tokenów JWT + rolę.
     *
     * POST /api/auth/login
     *
     * @param request dane logowania (e-mail + hasło).
     * @return opakowana odpowiedź HTTP z [AuthApiResponse] lub kodem błędu.
     */
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthApiResponse>

    /**
     * Wymienia wygasły access token na nową parę tokenów (rotacja tokenów).
     *
     * POST /api/auth/refresh
     *
     * @param request bieżący refresh token.
     * @return opakowana odpowiedź HTTP z [TokenPairResponse] lub kodem błędu.
     */
    @POST("/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<TokenPairResponse>
}
