package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AcceptInvitationRequestDto
import pl.edu.ur.blokur.dtos.AcceptInvitationResponseDto
import pl.edu.ur.blokur.dtos.AuthResponseDto
import pl.edu.ur.blokur.dtos.ForgotPasswordRequestDto
import pl.edu.ur.blokur.dtos.LoginRequestDto
import pl.edu.ur.blokur.dtos.MessageResponseDto
import pl.edu.ur.blokur.dtos.RefreshTokenRequestDto
import pl.edu.ur.blokur.dtos.ResetPasswordRequestDto
import pl.edu.ur.blokur.dtos.TokenPairResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Retrofit interface dla endpointów autoryzacji. */
interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): Response<TokenPairResponseDto>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<MessageResponseDto>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<MessageResponseDto>

    /**
     * POST /api/auth/accept-invitation — pierwsze logowanie po zaproszeniu.
     *
     * Użytkownik podaje token z maila i ustawia hasło.
     * Wywołanie nie wymaga JWT — używany jest klient "bare" (bez AuthInterceptor).
     * Po sukcesie użytkownik może się zalogować standardowo przez /api/auth/login.
     *
     * HTTP 200 OK — sukces, {"message": "Hasło zostało ustawione..."}
     * HTTP 400 Bad Request — token wygasł lub hasło nie spełnia wymagań
     */
    @POST("api/auth/accept-invitation")
    suspend fun acceptInvitation(
        @Body request: AcceptInvitationRequestDto
    ): Response<AcceptInvitationResponseDto>
}
