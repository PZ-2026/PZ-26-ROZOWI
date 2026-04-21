package pl.edu.ur.blokur.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.edu.ur.blokur.models.ResolutionVote;

/**
 * Repozytorium dostępu do danych dla encji ResolutionVote.
 * Zapewnia standardowe operacje CRUD dla oddanych głosów.
 */
@Repository
public interface ResolutionVoteRepository extends JpaRepository<ResolutionVote, UUID> {
    // Standardowe metody odziedziczone po JpaRepository
}
