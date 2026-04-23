package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.Meter;

import java.util.List;
import java.util.UUID;

/**
 * Repozytorium JPA dla encji {@link Meter}.
 */
public interface MeterRepository extends JpaRepository<Meter, UUID> {

    /**
     * Zwraca wszystkie liczniki przypisane do wskazanego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista liczników (aktywnych i nieaktywnych)
     */
    List<Meter> findByApartmentId(UUID apartmentId);

    /**
     * Sprawdza czy istnieje licznik o danym numerze seryjnym.
     *
     * @param serialNumber numer seryjny
     * @return true jeśli licznik istnieje
     */
    boolean existsBySerialNumber(String serialNumber);
}
