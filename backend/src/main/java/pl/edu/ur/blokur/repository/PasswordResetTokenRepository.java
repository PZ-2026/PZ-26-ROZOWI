package pl.edu.ur.blokur.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.PasswordResetToken;

/** Repozytorium tokenów jednorazowego resetu hasła. */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Wyszukuje token po jego wartości.
     *
     * @param token wartość tokenu
     * @return token jeśli istnieje
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Usuwa token o zadanej wartości.
     *
     * @param token wartość tokenu do usunięcia
     */
    void deleteByToken(String token);
}
