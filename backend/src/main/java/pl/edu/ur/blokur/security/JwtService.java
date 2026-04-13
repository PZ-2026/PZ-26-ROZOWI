package pl.edu.ur.blokur.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Serwis do generowania i walidacji tokenów JWT.
 * Tokeny są podpisywane algorytmem HMAC-SHA256 i ważne przez 24 godziny.
 */
@Service
public class JwtService {

    private static final String SECRET_KEY = "ToJestBardzoTajnyKluczDoGenerowaniaTokenowJwT!123";
    private static final long EXPIRATION_TIME = 86400000L;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

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
     * Sprawdza czy podany token JWT jest ważny i nieprzeterminowany.
     *
     * @param token token JWT
     * @return true jeśli token jest prawidłowy
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
