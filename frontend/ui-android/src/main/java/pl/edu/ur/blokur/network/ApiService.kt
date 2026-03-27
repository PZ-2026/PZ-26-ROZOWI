package pl.edu.ur.blokur.network

import pl.edu.ur.blokur.network.dto.LoginRequest
import pl.edu.ur.blokur.network.dto.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
