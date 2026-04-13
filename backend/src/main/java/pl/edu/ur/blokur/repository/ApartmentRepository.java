package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.Apartment;

import java.util.UUID;

/**
 * Repozytorium JPA dla encji {@link Apartment}.
 */
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
}
