package pl.edu.ur.blokur.controller

import pl.edu.ur.blokur.dto.AuthResponse
import pl.edu.ur.blokur.dto.LoginRequest
import pl.edu.ur.blokur.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )

            val role = authentication.authorities
                .firstOrNull()?.authority
                ?.replace("ROLE_", "") ?: "USER"

            val token = jwtService.generateToken(authentication.name, role)

            ResponseEntity.ok(AuthResponse(token, role))

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}