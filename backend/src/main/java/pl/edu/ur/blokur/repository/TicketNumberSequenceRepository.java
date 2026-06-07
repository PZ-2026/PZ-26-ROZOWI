package pl.edu.ur.blokur.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.ur.blokur.models.TicketNumberSequence;

/**
 * Repozytorium JPA dla encji {@link TicketNumberSequence}. Udostępnia blokadę pesymistyczną
 * na poziomie wiersza, dzięki której generator numerów zgłoszeń jest bezpieczny w środowiskach
 * wieloinstancyjnych.
 */
public interface TicketNumberSequenceRepository
        extends JpaRepository<TicketNumberSequence, Integer> {

    /**
     * Pobiera sekwencję dla podanego roku z blokadą {@code SELECT FOR UPDATE}. Blokada zapobiega
     * równoczesnej modyfikacji tego samego wiersza przez inne instancje aplikacji.
     *
     * @param year rok kalendarzowy
     * @return opcjonalna sekwencja z założoną blokadą pesymistyczną
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TicketNumberSequence s WHERE s.year = :year")
    Optional<TicketNumberSequence> findByYearForUpdate(@Param("year") int year);
}
