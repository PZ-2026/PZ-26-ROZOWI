package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.dto.TicketHistoryDto;
import pl.edu.ur.blokur.models.TicketHistory;

/**
 * Repozytorium JPA dla encji {@link TicketHistory}. Zawiera złożone kwerendy łączące tabele
 * ticket_history i users w celu śledzenia audytu zmian statusów zgłoszeń.
 */
@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, UUID> {

    /**
     * Pobiera pełną historię zmian statusów zgłoszenia wraz z danymi użytkownika dokonującego
     * zmiany. Złącza tabele ticket_history i users.
     *
     * @param ticketId identyfikator zgłoszenia
     * @return lista wpisów historii posortowana od najstarszego
     */
    @Query(
            "SELECT new pl.edu.ur.blokur.dto.TicketHistoryDto("
                    + "h.id, h.ticket.id, h.status, "
                    + "CONCAT(u.firstName, ' ', u.lastName), "
                    + "h.comment, h.createdAt) "
                    + "FROM TicketHistory h "
                    + "JOIN h.changedBy u "
                    + "WHERE h.ticket.id = :ticketId "
                    + "ORDER BY h.createdAt ASC")
    List<TicketHistoryDto> findHistoryByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Pobiera ostatni wpis historii dla danego zgłoszenia. Używane do wyświetlania aktualnego
     * statusu z informacją kto go ustawił.
     *
     * @param ticketId identyfikator zgłoszenia
     * @return ostatni wpis historii lub pusty Optional
     */
    @Query(
            "SELECT new pl.edu.ur.blokur.dto.TicketHistoryDto("
                    + "h.id, h.ticket.id, h.status, "
                    + "CONCAT(u.firstName, ' ', u.lastName), "
                    + "h.comment, h.createdAt) "
                    + "FROM TicketHistory h "
                    + "JOIN h.changedBy u "
                    + "WHERE h.ticket.id = :ticketId "
                    + "ORDER BY h.createdAt DESC")
    Optional<TicketHistoryDto> findLatestHistoryByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Pobiera historię zmian statusów dla wielu zgłoszeń jednocześnie. Używane do wsadowego
     * ładowania audytu dla listy zgłoszeń.
     *
     * @param ticketIds lista identyfikatorów zgłoszeń
     * @return lista wpisów historii posortowana od najstarszego
     */
    @Query(
            "SELECT new pl.edu.ur.blokur.dto.TicketHistoryDto("
                    + "h.id, h.ticket.id, h.status, "
                    + "CONCAT(u.firstName, ' ', u.lastName), "
                    + "h.comment, h.createdAt) "
                    + "FROM TicketHistory h "
                    + "JOIN h.changedBy u "
                    + "WHERE h.ticket.id IN :ticketIds "
                    + "ORDER BY h.createdAt ASC")
    List<TicketHistoryDto> findHistoryByTicketIds(@Param("ticketIds") List<UUID> ticketIds);

    /**
     * Pobiera historię zmian statusów wykonanych przez danego użytkownika. Używane w audycie
     * działań konserwatora lub zarządcy.
     *
     * @param userId identyfikator użytkownika
     * @return lista wpisów historii wykonanych przez użytkownika
     */
    @Query(
            "SELECT new pl.edu.ur.blokur.dto.TicketHistoryDto("
                    + "h.id, h.ticket.id, h.status, "
                    + "CONCAT(u.firstName, ' ', u.lastName), "
                    + "h.comment, h.createdAt) "
                    + "FROM TicketHistory h "
                    + "JOIN h.changedBy u "
                    + "WHERE u.id = :userId "
                    + "ORDER BY h.createdAt DESC")
    List<TicketHistoryDto> findHistoryByUserId(@Param("userId") UUID userId);

    /**
     * Pobiera najnowszy wpis historii dla danego zgłoszenia o wskazanym statusie. Używane przez
     * {@code TicketAutoCloseJob} do wyznaczenia momentu wejścia w stan
     * {@code ZAKONCZONE_DO_WERYFIKACJI}.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param status status, dla którego szukamy ostatniego wpisu (np. {@code "ZAKONCZONE_DO_WERYFIKACJI"})
     * @return encja {@link TicketHistory} lub pusty Optional
     */
    @Query(
            "SELECT h FROM TicketHistory h "
                    + "WHERE h.ticket.id = :ticketId AND h.status = :status "
                    + "ORDER BY h.createdAt DESC")
    List<TicketHistory> findByTicketIdAndStatusOrderByCreatedAtDesc(
            @Param("ticketId") UUID ticketId, @Param("status") String status);
}
