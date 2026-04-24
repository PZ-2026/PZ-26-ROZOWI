package pl.edu.ur.blokur.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import org.springframework.stereotype.Service;

/**
 * Serwis do generowania i walidacji tokenów JWT. Tokeny są podpisywane algorytmem HMAC-SHA256 i
 * ważne przez 24 godziny.
 */
@Service
public class JwtService {

    private static final String SECRET_KEY = "ToJestBardzoTajnyKluczDoGenerowaniaTokenowJwT!123";
    private static final long EXPIRATION_TIME = 86400000L;
    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generuje token JWT dla podanego użytkownika i roli.
     *
     * @param username nazwa użytkownika
     * @param role rola użytkownika (bez prefiksu ROLE_)
     * @return podpisany token JWT
     */
    public String generateToken(String username, String role) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Wyciąga nazwę użytkownika z tokenu JWT.
     *
     * @param token token JWT
     * @return nazwa użytkownika lub null jeśli token jest nieprawidłowy
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Wyciąga rolę użytkownika z tokenu JWT.
     *
     * @param token token JWT
     * @return rola użytkownika lub null jeśli token jest nieprawidłowy
     */
    public String extractRole(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Generuje kryptograficznie bezpieczny token odświeżania (256 bitów, Base64 URL-safe).
     *
     * @return wartość refresh tokena do zapisania w bazie
     */
    public String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Zwraca datę wygaśnięcia nowego refresh tokena.
     *
     * @return LocalDateTime za {@value REFRESH_TOKEN_EXPIRATION_DAYS} dni
     */
    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS);
    }

    /**
     * Sprawdza czy podany token JWT jest ważny i nieprzeterminowany.
     *
     * @param token token JWT
     * @return true jeśli token jest prawidłowy
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
