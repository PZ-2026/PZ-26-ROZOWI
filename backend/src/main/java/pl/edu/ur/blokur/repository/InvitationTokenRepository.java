package pl.edu.ur.blokur.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.InvitationToken;

/** Repozytorium jednorazowych tokenów zaproszenia nowych użytkowników. */
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, UUID> {

    /**
     * Wyszukuje token zaproszenia po jego wartości.
     *
     * @param token wartość tokenu
     * @return token jeśli istnieje
     */
    Optional<InvitationToken> findByToken(String token);

    /**
     * Usuwa token zaproszenia o zadanej wartości.
     *
     * @param token wartość tokenu do usunięcia
     */
    void deleteByToken(String token);
}
