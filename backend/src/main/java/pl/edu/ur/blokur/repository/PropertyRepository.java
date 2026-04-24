package pl.edu.ur.blokur.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Property;

/** Repozytorium JPA dla encji {@link Property}. */
@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    /**
     * Sprawdza, czy istnieje nieruchomość o podanym NIP.
     *
     * @param nip NIP do sprawdzenia
     * @return {@code true} jeśli NIP jest już zajęty
     */
    boolean existsByNip(String nip);

    /**
     * Sprawdza, czy istnieje inna nieruchomość o podanym NIP (do walidacji podczas aktualizacji).
     *
     * @param nip NIP do sprawdzenia
     * @param id identyfikator nieruchomości, która powinna być wykluczona z weryfikacji
     * @return {@code true} jeśli NIP jest zajęty przez inną nieruchomość
     */
    boolean existsByNipAndIdNot(String nip, UUID id);
}
