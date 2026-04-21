package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.User;

import java.util.List;
import java.util.UUID;

/**
 * Repozytorium JPA dla encji {@link Ticket}.
 * Zawiera złożone kwerendy do pobierania i filtrowania zgłoszeń.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Pobiera wszystkie zgłoszenia powiązane z danym lokalem mieszkalnym.
     * Uwzględnia zgłoszenia przypisane bezpośrednio do lokalu,
     * do klatki schodowej lokalu lub do budynku lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista zgłoszeń powiązanych z lokalem
     */
    @Query("SELECT t FROM Ticket t "
        + "JOIN t.apartment a "
        + "WHERE a.id = :apartmentId "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByApartmentId(@Param("apartmentId") UUID apartmentId);

    /**
     * Pobiera wszystkie zgłoszenia powiązane z daną klatką schodową.
     *
     * @param staircaseId identyfikator klatki schodowej
     * @return lista zgłoszeń dla klatki
     */
    @Query("SELECT t FROM Ticket t "
        + "JOIN t.staircase s "
        + "WHERE s.id = :staircaseId "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByStaircaseId(@Param("staircaseId") UUID staircaseId);

    /**
     * Pobiera wszystkie zgłoszenia powiązane z danym budynkiem.
     *
     * @param buildingId identyfikator budynku
     * @return lista zgłoszeń dla budynku
     */
    @Query("SELECT t FROM Ticket t "
        + "JOIN t.building b "
        + "WHERE b.id = :buildingId "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByBuildingId(@Param("buildingId") UUID buildingId);

    /**
     * Pobiera wszystkie zgłoszenia widoczne dla mieszkańca na podstawie jego lokalu.
     * Zwraca zgłoszenia przypisane do jego mieszkania, klatki lub budynku.
     *
     * @param apartmentId identyfikator lokalu mieszkańca
     * @param staircaseId identyfikator klatki schodowej mieszkańca
     * @param buildingId  identyfikator budynku mieszkańca
     * @return lista zgłoszeń widocznych dla mieszkańca
     */
    @Query("SELECT t FROM Ticket t "
        + "WHERE (t.apartment.id = :apartmentId) "
        + "OR (t.staircase.id = :staircaseId) "
        + "OR (t.building.id = :buildingId) "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findForResident(
            @Param("apartmentId") UUID apartmentId,
            @Param("staircaseId") UUID staircaseId,
            @Param("buildingId") UUID buildingId);

    /**
     * Pobiera wszystkie zgłoszenia przypisane do danego konserwatora.
     *
     * @param conservatorId identyfikator konserwatora
     * @return lista zgłoszeń przypisanych do konserwatora
     */
    @Query("SELECT t FROM Ticket t "
        + "WHERE t.assignedTo.id = :conservatorId "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByAssignedToId(@Param("conservatorId") UUID conservatorId);

    /**
     * Pobiera zgłoszenia nieprzypisane do żadnego konserwatora.
     * Używane przez zarządcę do przeglądu nowych zgłoszeń wymagających delegacji.
     *
     * @return lista nieprzypisanych zgłoszeń
     */
    @Query("SELECT t FROM Ticket t "
        + "WHERE t.assignedTo IS NULL "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findUnassigned();

    /**
     * Pobiera zgłoszenia filtrowane po statusie.
     *
     * @param status status zgłoszenia (np. NOWE, W_REALIZACJI, ZAKONCZONE)
     * @return lista zgłoszeń o danym statusie
     */
    @Query("SELECT t FROM Ticket t "
        + "WHERE t.status = :status "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByStatus(@Param("status") String status);

    /**
     * Pobiera zgłoszenia przypisane do konserwatora filtrowane po statusie.
     *
     * @param conservatorId identyfikator konserwatora
     * @param status        status zgłoszenia
     * @return lista zgłoszeń konserwatora o danym statusie
     */
    @Query("SELECT t FROM Ticket t "
        + "WHERE t.assignedTo.id = :conservatorId "
        + "AND t.status = :status "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByAssignedToIdAndStatus(
            @Param("conservatorId") UUID conservatorId,
            @Param("status") String status);

    /**
     * Pobiera zgłoszenia dla danego budynku filtrowane po statusie.
     * Używane przez zarządcę do monitorowania stanu zgłoszeń w budynku.
     *
     * @param buildingId identyfikator budynku
     * @param status     status zgłoszenia
     * @return lista zgłoszeń dla budynku o danym statusie
     */
    @Query("SELECT t FROM Ticket t "
        + "WHERE (t.building.id = :buildingId "
        + "OR t.staircase.id IN (SELECT s.id FROM Staircase s WHERE s.building.id = :buildingId) "
        + "OR t.apartment.id IN (SELECT a.id FROM Apartment a WHERE a.staircase.id IN "
        + "(SELECT s.id FROM Staircase s WHERE s.building.id = :buildingId))) "
        + "AND t.status = :status "
        + "ORDER BY t.createdAt DESC")
    List<Ticket> findByBuildingIdAndStatus(
            @Param("buildingId") UUID buildingId,
            @Param("status") String status);

    /**
     * Przypisuje konserwatora do zgłoszenia.
     *
     * @param ticketId      identyfikator zgłoszenia
     * @param conservator   encja konserwatora
     */
    @Modifying
    @Query("UPDATE Ticket t SET t.assignedTo = :conservator WHERE t.id = :ticketId")
    void assignConservator(
            @Param("ticketId") UUID ticketId,
            @Param("conservator") User conservator);

    /**
     * Aktualizuje status zgłoszenia.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param status   nowy status
     */
    @Modifying
    @Query("UPDATE Ticket t SET t.status = :status WHERE t.id = :ticketId")
    void updateStatus(
            @Param("ticketId") UUID ticketId,
            @Param("status") String status);
}
