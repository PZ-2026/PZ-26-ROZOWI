package pl.edu.ur.blokur.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.edu.ur.blokur.models.ResolutionOption;

/**
 * Repozytorium dostępu do danych dla encji ResolutionOption.
 * Zapewnia standardowe operacje CRUD dla opcji dostępnych w uchwałach.
 */
@Repository
public interface ResolutionOptionRepository extends JpaRepository<ResolutionOption, UUID> {
    // Standardowe metody odziedziczone po JpaRepository
}
