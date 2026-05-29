package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

/** Role użytkownika w systemie Blokur. */
enum class UserRole {
    MIESZKANIEC,
    KONSERWATOR,
    ZARZADCA
}

/** Hierarchia wyjątków związanych z autoryzacją. */
sealed class AuthException(message: String) : Exception(message) {
    data object InvalidCredentials : AuthException("Nieprawidłowy e-mail lub hasło")
    data object AccountLocked : AuthException("Konto zostało zablokowane. Spróbuj ponownie za 15 minut")
    data class ApiError(val code: Int) : AuthException("Błąd serwera: $code")
    data object EmptyResponse : AuthException("Brak danych w odpowiedzi serwera")
    data class UnknownRole(val role: String) : AuthException("Nieznana rola użytkownika: $role")
}

/** POST /api/auth/login — ciało żądania. */
data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

/** POST /api/auth/login — odpowiedź. */
data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("role") val role: String
)

/** POST /api/auth/refresh — ciało żądania. */
data class RefreshTokenRequestDto(
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * POST /api/auth/refresh — odpowiedź.
 *
 * Backend zwraca JSON: { "token": "...", "refreshToken": "...", "role": "..." }
 * (ta sama struktura co AuthResponseDto — używa AuthResponse.java po stronie serwera).
 */
data class TokenPairResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("role") val role: String
) {
    /** Alias dla kompatybilności z TokenAuthenticator i innymi konsumentami. */
    val accessToken: String get() = token
}

/** POST /api/auth/forgot-password — ciało żądania. */
data class ForgotPasswordRequestDto(
    @SerializedName("email") val email: String
)

/** POST /api/auth/reset-password — ciało żądania. */
data class ResetPasswordRequestDto(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)

/** Generyczna odpowiedź z komunikatem (forgot-password, reset-password). */
data class MessageResponseDto(
    @SerializedName("message") val message: String
)

/**
 * POST /api/auth/accept-invitation — ciło żądania przyjecia zaproszenia.
 *
 * Endpoint służy do pierwszego logowania użytkownika po zaproszeniu przez zarządcę.
 * Token zaproszenia jest przekazywany w linku mailowym (query param ?token=...).
 *
 * Wymagania hasła (walidacja backendowa):
 * - minimum 8 znaków
 * - co najmniej jedna wielka litera
 * - co najmniej jedna cyfra
 */
data class AcceptInvitationRequestDto(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)

/** POST /api/auth/accept-invitation — odpowiedź (mapa {"message": "..."} z backendu). */
data class AcceptInvitationResponseDto(
    @SerializedName("message") val message: String
)
