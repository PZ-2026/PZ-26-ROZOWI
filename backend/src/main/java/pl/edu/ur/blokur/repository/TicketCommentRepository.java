package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.dto.TicketCommentDto;
import pl.edu.ur.blokur.models.TicketComment;

/**
 * Repozytorium JPA dla encji {@link TicketComment}. Zawiera złożone kwerendy łączące tabele
 * ticket_comments i users.
 */
@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {

    /**
     * Pobiera komentarze do zgłoszenia wraz z danymi autora. Złącza tabele ticket_comments i users
     * w celu pobrania imienia i nazwiska autora bez dodatkowych zapytań.
     *
     * @param ticketId identyfikator zgłoszenia
     * @return lista komentarzy z danymi autora posortowana od najstarszego
     */
    @Query(
            "SELECT new pl.edu.ur.blokur.dto.TicketCommentDto("
                    + "c.id, c.ticket.id, "
                    + "CONCAT(u.firstName, ' ', u.lastName), "
                    + "c.content, c.createdAt) "
                    + "FROM TicketComment c "
                    + "JOIN c.author u "
                    + "WHERE c.ticket.id = :ticketId "
                    + "ORDER BY c.createdAt ASC")
    List<TicketCommentDto> findCommentsByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Zlicza komentarze dla danego zgłoszenia. Używane przy pobieraniu listy zgłoszeń bez ładowania
     * pełnych komentarzy.
     *
     * @param ticketId identyfikator zgłoszenia
     * @return liczba komentarzy
     */
    @Query("SELECT COUNT(c) FROM TicketComment c WHERE c.ticket.id = :ticketId")
    long countByTicketId(@Param("ticketId") UUID ticketId);

    /**
     * Pobiera komentarze do wielu zgłoszeń wraz z danymi autora. Używane do wsadowego ładowania
     * komentarzy dla listy zgłoszeń.
     *
     * @param ticketIds lista identyfikatorów zgłoszeń
     * @return lista komentarzy z danymi autora
     */
    @Query(
            "SELECT new pl.edu.ur.blokur.dto.TicketCommentDto("
                    + "c.id, c.ticket.id, "
                    + "CONCAT(u.firstName, ' ', u.lastName), "
                    + "c.content, c.createdAt) "
                    + "FROM TicketComment c "
                    + "JOIN c.author u "
                    + "WHERE c.ticket.id IN :ticketIds "
                    + "ORDER BY c.createdAt ASC")
    List<TicketCommentDto> findCommentsByTicketIds(@Param("ticketIds") List<UUID> ticketIds);
}
