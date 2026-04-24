package pl.edu.ur.blokur.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.ResolutionVote;

/**
 * Repozytorium dostępu do danych dla encji ResolutionVote. Zapewnia standardowe operacje CRUD dla
 * oddanych głosów.
 */
@Repository
public interface ResolutionVoteRepository extends JpaRepository<ResolutionVote, UUID> {

    /**
     * Zlicza liczbę głosów oddanych na daną opcję.
     *
     * @param optionId identyfikator opcji
     * @return liczba głosów
     */
    long countByOptionId(UUID optionId);

    /**
     * Sprawdza, czy użytkownik o podanym ID zagłosował w wybranej uchwale.
     *
     * @param resolutionId identyfikator uchwały
     * @param voterId identyfikator użytkownika
     * @return true jeśli zagłosował, false w przeciwnym wypadku
     */
    boolean existsByResolutionIdAndVoterId(UUID resolutionId, UUID voterId);
}
