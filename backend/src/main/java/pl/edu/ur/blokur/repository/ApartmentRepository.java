package pl.edu.ur.blokur.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.Apartment;

/** Repozytorium JPA dla encji {@link Apartment}. */
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {}
