package pl.edu.ur.blokur.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService {

    private val secretKey = "ToJestBardzoTajnyKluczDoGenerowaniaTokenowJwT!123"
    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
    private val expirationTime = 86400000L 

    fun generateToken(username: String, role: String): String {
        val now = System.currentTimeMillis()

        return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(Date(now))
            .setExpiration(Date(now + expirationTime))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
}