package pl.edu.ur.blokur.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.MeterReading;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repozytorium JPA dla encji {@link MeterReading}.
 * Zawiera metody do filtrowania odczytów z uwzględnieniem miękkiego usunięcia.
 */
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    /**
     * Pobiera stronicowaną listę nieusunietych odczytów dla danego lokalu.
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
     * Zwraca ostatni (najnowszy) nieusunięty odczyt danego typu licznika dla lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param meterType typ licznika
     * @return ostatni odczyt lub null jeśli brak
     */
    MeterReading findTopByApartmentIdAndMeterTypeAndDeletedFalseOrderByReadingDateDesc(
        UUID apartmentId,
        String meterType
    );

    /**
     * Sprawdza czy istnieje nieusunięty odczyt dla danego lokalu, typu licznika i daty.
     *
     * @param apartmentId identyfikator lokalu
     * @param meterType typ licznika
     * @param readingDate data odczytu
     * @return true jeśli duplikat istnieje
     */
    boolean existsByApartmentIdAndMeterTypeAndReadingDateAndDeletedFalse(
        UUID apartmentId,
        String meterType,
        LocalDate readingDate
    );

    /**
     * Sprawdza czy istnieje nieusunięty odczyt dla danego lokalu, typu i daty, inny niż wskazany.
     * Używane przy aktualizacji do wykrycia duplikatów.
     *
     * @param apartmentId identyfikator lokalu
     * @param meterType typ licznika
     * @param readingDate data odczytu
     * @param id identyfikator wykluczanego odczytu
     * @return true jeśli duplikat istnieje
     */
    boolean existsByApartmentIdAndMeterTypeAndReadingDateAndIdNotAndDeletedFalse(
        UUID apartmentId,
        String meterType,
        LocalDate readingDate,
        UUID id
    );
}
