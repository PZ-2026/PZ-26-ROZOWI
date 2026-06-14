package pl.edu.ur.blokur.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.InvitationToken;
import pl.edu.ur.blokur.models.User;

/** Repozytorium jednorazowych kodów zaproszenia nowych użytkowników. */
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, UUID> {

    /** Wyszukuje aktywny kod zaproszenia danego użytkownika. */
    Optional<InvitationToken> findByUserAndToken(User user, String token);

    /** Usuwa wszystkie istniejące kody zaproszenia danego użytkownika. */
    void deleteByUser(User user);
}
