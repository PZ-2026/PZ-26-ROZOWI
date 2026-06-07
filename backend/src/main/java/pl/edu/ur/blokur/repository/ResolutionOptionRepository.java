package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.ResolutionOption;

/**
 * Repozytorium dostępu do danych dla encji ResolutionOption. Zapewnia standardowe operacje CRUD dla
 * opcji dostępnych w uchwałach.
 */
@Repository
public interface ResolutionOptionRepository extends JpaRepository<ResolutionOption, UUID> {

    /**
     * Znajduje wszystkie opcje dla danej uchwały.
     *
     * @param resolutionId identyfikator uchwały
     * @return lista opcji
     */
    List<ResolutionOption> findByResolutionId(UUID resolutionId);
}
