package pl.edu.ur.blokur.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.models.RefreshToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.RefreshTokenRepository;
import pl.edu.ur.blokur.security.JwtService;

/**
 * Serwis obsługujący cykl życia refresh tokenów JWT. Pozwala wymieniać ważny refresh token na nową
 * parę (access + refresh), stosując rotację tokenów.
 */
@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param refreshTokenRepository repozytorium refresh tokenów
     * @param jwtService serwis generujący i walidujący tokeny JWT
     */
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    /**
     * Tworzy i zapisuje nowy refresh token dla podanego użytkownika.
     *
     * @param user użytkownik, dla którego generowany jest token
     * @return zapisany refresh token
     */
    public RefreshToken createRefreshToken(User user) {
        var token =
                new RefreshToken(
                        user,
                        jwtService.generateRefreshTokenValue(),
                        jwtService.getRefreshTokenExpiry());
        return refreshTokenRepository.save(token);
    }

    /**
     * Wymienia ważny refresh token na nową parę (access + refresh). Stary refresh token zostaje
     * unieważniony (rotacja tokenów).
     *
     * @param tokenValue wartość refresh tokenu przesłana przez klienta
     * @return nowa para tokenów wraz z rolą użytkownika
     * @throws IllegalArgumentException gdy token jest nieprawidłowy, unieważniony lub wygasły
     */
    public TokenPair exchange(String tokenValue) {
        var stored =
                refreshTokenRepository
                        .findByToken(tokenValue)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Nieprawidłowy refresh token"));

        if (stored.isRevoked()) {
            throw new IllegalArgumentException("Token został unieważniony");
        }

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("Token wygasł");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        var user = stored.getUser();
        var newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        var newRefreshToken = createRefreshToken(user);

        return new TokenPair(newAccessToken, newRefreshToken.getToken(), user.getRole());
    }

    /**
     * Para tokenów zwracana po odświeżeniu sesji.
     *
     * @param accessToken krótkożyjący token dostępu (JWT)
     * @param refreshToken nowy, długożyjący refresh token
     * @param role rola użytkownika
     */
    public record TokenPair(String accessToken, String refreshToken, String role) {}
}
