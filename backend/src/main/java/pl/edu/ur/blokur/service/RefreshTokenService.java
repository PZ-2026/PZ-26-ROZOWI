package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.models.RefreshToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.RefreshTokenRepository;
import pl.edu.ur.blokur.security.JwtService;

import java.time.LocalDateTime;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken(
            user,
            jwtService.generateRefreshTokenValue(),
            jwtService.getRefreshTokenExpiry()
        );
        return refreshTokenRepository.save(token);
    }

    public TokenPair exchange(String tokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy refresh token"));

        if (stored.isRevoked()) {
            throw new IllegalArgumentException("Token został unieważniony");
        }

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("Token wygasł");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        RefreshToken newRefreshToken = createRefreshToken(user);

        return new TokenPair(newAccessToken, newRefreshToken.getToken(), user.getRole());
    }

    public record TokenPair(String accessToken, String refreshToken, String role) {}
}
