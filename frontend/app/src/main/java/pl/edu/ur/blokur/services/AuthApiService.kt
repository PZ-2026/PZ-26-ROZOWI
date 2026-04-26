package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AuthResponseDto
import pl.edu.ur.blokur.dtos.LoginRequestDto
import pl.edu.ur.blokur.dtos.RefreshTokenRequestDto
import pl.edu.ur.blokur.dtos.TokenPairResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Retrofit interface dla endpointów autoryzacji. */
interface AuthApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): Response<TokenPairResponseDto>
}
