package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Building;

import java.util.UUID;

/**
 * Repozytorium udostępniające operacje bazodanowe dla encji Building.
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {
}
