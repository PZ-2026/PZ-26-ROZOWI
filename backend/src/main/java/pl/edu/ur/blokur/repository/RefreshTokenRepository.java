package pl.edu.ur.blokur.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.RefreshToken;
import pl.edu.ur.blokur.models.User;

/** Repozytorium refresh tokenów JWT wykorzystywanych do odświeżania sesji użytkownika. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Wyszukuje refresh token po jego wartości.
     *
     * @param token wartość tokenu
     * @return token jeśli istnieje
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Usuwa wszystkie refresh tokeny należące do podanego użytkownika (np. przy wylogowaniu ze
     * wszystkich urządzeń).
     *
     * @param user użytkownik, którego tokeny mają zostać usunięte
     */
    void deleteAllByUser(User user);
}
