package pl.edu.ur.blokur.dto

data class LoginRequest(
    val username: String = "",
    val password: String = ""
)

data class AuthResponse(
    val token: String,
    val role: String
)