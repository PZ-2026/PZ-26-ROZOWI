package pl.edu.ur.blokur.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Resolution;

/** Repozytorium dostępu do danych dla encji Resolution. */
@Repository
public interface ResolutionRepository extends JpaRepository<Resolution, UUID> {
    // Standardowe metody odziedziczone po JpaRepository
}
