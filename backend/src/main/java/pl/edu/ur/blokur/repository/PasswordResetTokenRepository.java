package pl.edu.ur.blokur.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.PasswordResetToken;
import pl.edu.ur.blokur.models.User;

/** Repozytorium 6-cyfrowych kodów jednorazowego resetu hasła. */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /** Wyszukuje aktywny kod resetu hasła danego użytkownika. */
    Optional<PasswordResetToken> findByUserAndToken(User user, String token);

    /** Usuwa wszystkie istniejące kody resetu danego użytkownika. */
    void deleteByUser(User user);
}
