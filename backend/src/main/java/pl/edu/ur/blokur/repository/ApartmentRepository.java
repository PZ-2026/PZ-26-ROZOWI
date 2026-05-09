package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.ur.blokur.models.Apartment;

/** Repozytorium JPA dla encji {@link Apartment}. */
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    /**
     * Pobiera wszystkie lokale należące do wskazanej nieruchomości wraz z klatką i budynkiem
     * (FETCH JOIN zapobiega N+1).
     *
     * @param propertyId identyfikator nieruchomości
     * @return lista lokali w danej nieruchomości
     */
    @Query(
            "SELECT DISTINCT a FROM Apartment a"
                    + " JOIN FETCH a.staircase s"
                    + " JOIN FETCH s.building b"
                    + " WHERE b.property.id = :propertyId")
    List<Apartment> findAllByPropertyId(@Param("propertyId") UUID propertyId);

    /**
     * Pobiera wszystkie lokale wraz z klatką i budynkiem (FETCH JOIN zapobiega N+1). Używane gdy
     * zarządca nie filtruje po nieruchomości.
     *
     * @return lista wszystkich lokali
     */
    @Query(
            "SELECT DISTINCT a FROM Apartment a"
                    + " JOIN FETCH a.staircase s"
                    + " JOIN FETCH s.building b")
    List<Apartment> findAllWithBuilding();
}
