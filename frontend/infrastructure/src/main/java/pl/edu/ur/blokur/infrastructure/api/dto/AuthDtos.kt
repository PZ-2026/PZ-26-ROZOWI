package pl.edu.ur.blokur.infrastructure.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Ciało żądania POST /api/auth/login.
 *
 * Backend przyjmuje pole `username` jako adres e-mail.
 *
 * @property username adres e-mail użytkownika.
 * @property password hasło użytkownika.
 */
internal data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

/**
 * Odpowiedź na POST /api/auth/login.
 *
 * @property token        krótkotrwały JWT (24 h).
 * @property refreshToken długotrwały token odświeżający (30 dni).
 * @property role         rola użytkownika jako ciąg znaków (np. `"MIESZKANIEC"`).
 */
internal data class AuthApiResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("role") val role: String
)

/**
 * Ciało żądania POST /api/auth/refresh.
 *
 * @property refreshToken bieżący token odświeżający, który ma zostać wymieniony.
 */
internal data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * Odpowiedź na POST /api/auth/refresh (rotacja tokenów).
 *
 * @property accessToken  nowy krótkotrwały JWT.
 * @property refreshToken nowy długotrwały token odświeżający.
 * @property role         rola użytkownika (niezmienna w trakcie sesji).
 */
internal data class TokenPairResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("role") val role: String
)
