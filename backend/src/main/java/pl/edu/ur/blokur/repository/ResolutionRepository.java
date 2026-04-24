package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Resolution;

/** Repozytorium dostępu do danych dla encji Resolution. */
@Repository
public interface ResolutionRepository extends JpaRepository<Resolution, UUID> {

    /**
     * Znajduje uchwały powiązane z podanym identyfikatorem budynku.
     *
     * @param buildingId identyfikator budynku
     * @return lista uchwał
     */
    List<Resolution> findByBuildingId(UUID buildingId);
}
