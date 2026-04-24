package pl.edu.ur.blokur.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Staircase;

/** Repozytorium JPA dla encji {@link Staircase}. */
@Repository
public interface StaircaseRepository extends JpaRepository<Staircase, UUID> {}
