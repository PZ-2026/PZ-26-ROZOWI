package pl.edu.ur.blokur.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.MeterReading;

/**
 * Repozytorium JPA dla encji {@link MeterReading}. Zawiera metody do filtrowania odczytów z
 * uwzględnieniem miękkiego usunięcia.
 */
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    /**
     * Pobiera stronicowaną listę nieusuniętych odczytów dla danego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param pageable parametry stronicowania
     * @return strona z odczytami
     */
    Page<MeterReading> findByApartmentIdAndDeletedFalse(UUID apartmentId, Pageable pageable);

    /**
     * Pobiera nieusunięty odczyt o podanym identyfikatorze.
     *
     * @param id identyfikator odczytu
     * @return opcjonalny odczyt
     */
    Optional<MeterReading> findByIdAndDeletedFalse(UUID id);

    /**
     * Zwraca ostatni (najnowszy) nieusunięty odczyt dla wskazanego licznika.
     *
     * @param meterId identyfikator licznika
     * @return ostatni odczyt lub null jeśli brak
     */
    MeterReading findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(UUID meterId);

    /**
     * Sprawdza czy istnieje nieusunięty odczyt dla danego licznika i daty.
     *
     * @param meterId identyfikator licznika
     * @param readingDate data odczytu
     * @return true jeśli duplikat istnieje
     */
    boolean existsByMeterIdAndReadingDateAndDeletedFalse(UUID meterId, LocalDate readingDate);

    /**
     * Sprawdza czy istnieje nieusunięty odczyt dla danego licznika i daty, inny niż wskazany.
     * Używane przy aktualizacji do wykrycia duplikatów.
     *
     * @param meterId identyfikator licznika
     * @param readingDate data odczytu
     * @param id identyfikator wykluczanego odczytu
     * @return true jeśli duplikat istnieje
     */
    boolean existsByMeterIdAndReadingDateAndIdNotAndDeletedFalse(
            UUID meterId, LocalDate readingDate, UUID id);
}
