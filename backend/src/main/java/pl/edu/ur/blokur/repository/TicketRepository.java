package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.User;

/**
 * Repozytorium JPA dla encji {@link Ticket}. Zawiera złożone kwerendy do pobierania i filtrowania
 * zgłoszeń.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Pobiera wszystkie zgłoszenia powiązane z danym lokalem mieszkalnym. Uwzględnia zgłoszenia
     * przypisane bezpośrednio do lokalu, do klatki schodowej lokalu lub do budynku lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista zgłoszeń powiązanych z lokalem
     */
    @Query(
            "SELECT t FROM Ticket t "
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
    @Query(
            "SELECT t FROM Ticket t "
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
    @Query(
            "SELECT t FROM Ticket t "
                    + "JOIN t.building b "
                    + "WHERE b.id = :buildingId "
                    + "ORDER BY t.createdAt DESC")
    List<Ticket> findByBuildingId(@Param("buildingId") UUID buildingId);

    /**
     * Pobiera wszystkie zgłoszenia widoczne dla mieszkańca na podstawie jego lokalu. Zwraca
     * zgłoszenia przypisane do jego mieszkania, klatki lub budynku.
     *
     * @param apartmentId identyfikator lokalu mieszkańca
     * @param staircaseId identyfikator klatki schodowej mieszkańca
     * @param buildingId identyfikator budynku mieszkańca
     * @return lista zgłoszeń widocznych dla mieszkańca
     */
    @Query(
            "SELECT t FROM Ticket t "
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
    @Query(
            "SELECT t FROM Ticket t "
                    + "WHERE t.assignedTo.id = :conservatorId "
                    + "ORDER BY t.createdAt DESC")
    List<Ticket> findByAssignedToId(@Param("conservatorId") UUID conservatorId);

    /**
     * Pobiera zgłoszenia nieprzypisane do żadnego konserwatora. Używane przez zarządcę do przeglądu
     * nowych zgłoszeń wymagających delegacji.
     *
     * @return lista nieprzypisanych zgłoszeń
     */
    @Query("SELECT t FROM Ticket t " + "WHERE t.assignedTo IS NULL " + "ORDER BY t.createdAt DESC")
    List<Ticket> findUnassigned();

    /**
     * Pobiera zgłoszenia filtrowane po statusie.
     *
     * @param status status zgłoszenia (np. NOWE, W_REALIZACJI, ZAKONCZONE)
     * @return lista zgłoszeń o danym statusie
     */
    @Query("SELECT t FROM Ticket t " + "WHERE t.status = :status " + "ORDER BY t.createdAt DESC")
    List<Ticket> findByStatus(@Param("status") String status);

    /**
     * Pobiera zgłoszenia przypisane do konserwatora filtrowane po statusie.
     *
     * @param conservatorId identyfikator konserwatora
     * @param status status zgłoszenia
     * @return lista zgłoszeń konserwatora o danym statusie
     */
    @Query(
            "SELECT t FROM Ticket t "
                    + "WHERE t.assignedTo.id = :conservatorId "
                    + "AND t.status = :status "
                    + "ORDER BY t.createdAt DESC")
    List<Ticket> findByAssignedToIdAndStatus(
            @Param("conservatorId") UUID conservatorId, @Param("status") String status);

    /**
     * Pobiera zgłoszenia dla danego budynku filtrowane po statusie. Używane przez zarządcę do
     * monitorowania stanu zgłoszeń w budynku.
     *
     * @param buildingId identyfikator budynku
     * @param status status zgłoszenia
     * @return lista zgłoszeń dla budynku o danym statusie
     */
    @Query(
            "SELECT t FROM Ticket t WHERE (t.building.id = :buildingId OR t.staircase.id IN (SELECT"
                + " s.id FROM Staircase s WHERE s.building.id = :buildingId) OR t.apartment.id IN"
                + " (SELECT a.id FROM Apartment a WHERE a.staircase.id IN (SELECT s.id FROM"
                + " Staircase s WHERE s.building.id = :buildingId))) AND t.status = :status ORDER"
                + " BY t.createdAt DESC")
    List<Ticket> findByBuildingIdAndStatus(
            @Param("buildingId") UUID buildingId, @Param("status") String status);

    /**
     * Przypisuje konserwatora do zgłoszenia.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param conservator encja konserwatora
     */
    @Modifying
    @Query("UPDATE Ticket t SET t.assignedTo = :conservator WHERE t.id = :ticketId")
    void assignConservator(
            @Param("ticketId") UUID ticketId, @Param("conservator") User conservator);

    /**
     * Aktualizuje status zgłoszenia.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param status nowy status
     */
    @Modifying
    @Query("UPDATE Ticket t SET t.status = :status WHERE t.id = :ticketId")
    void updateStatus(@Param("ticketId") UUID ticketId, @Param("status") String status);

    /**
     * Pobiera listę zgłoszeń jako DTO ze złączenia tabel tickets, users (autor), users
     * (konserwator) oraz ticket_categories. Lokalizacja wyznaczana jest na podstawie pierwszej
     * dostępnej wartości: lokal, klatka lub budynek. Kwerenda natywna SQL — wydajny JOIN przez 4
     * tabele bez lazy loading.
     *
     * @return lista zgłoszeń z pełnymi danymi powiązanymi
     */
    @Query(
            value =
                    "SELECT t.id, t.ticket_number, t.title, t.status, tc.name AS category_name,"
                        + " CONCAT(a.first_name, ' ', a.last_name) AS author_name, CASE WHEN ass.id"
                        + " IS NOT NULL THEN CONCAT(ass.first_name, ' ', ass.last_name) ELSE NULL"
                        + " END AS assigned_to_name, COALESCE(ap.number, st.label, b.name) AS"
                        + " location_label, t.created_at, t.closed_at FROM tickets t JOIN"
                        + " ticket_categories tc ON t.category_id = tc.id JOIN users a ON"
                        + " t.author_id = a.id LEFT JOIN users ass ON t.assigned_to_id = ass.id"
                        + " LEFT JOIN apartments ap ON t.apartment_id = ap.id LEFT JOIN staircases"
                        + " st ON t.staircase_id = st.id LEFT JOIN buildings b ON t.building_id ="
                        + " b.id WHERE t.is_deleted = false ORDER BY t.created_at DESC",
            nativeQuery = true)
    List<Object[]> findAllSummariesRaw();

    /**
     * Pobiera szczegóły pojedynczego zgłoszenia jako DTO ze złączenia tabel tickets, users (autor),
     * users (konserwator), ticket_categories oraz apartments/staircases/buildings (lokalizacja).
     * Kwerenda natywna SQL — wydajny JOIN przez wiele tabel.
     *
     * @param ticketId identyfikator zgłoszenia
     * @return dane zgłoszenia lub pusty Optional
     */
    @Query(
            value =
                    "SELECT t.id, t.ticket_number, t.title, t.status, tc.name AS category_name,"
                        + " CONCAT(a.first_name, ' ', a.last_name) AS author_name, CASE WHEN ass.id"
                        + " IS NOT NULL THEN CONCAT(ass.first_name, ' ', ass.last_name) ELSE NULL"
                        + " END AS assigned_to_name, COALESCE(ap.number, st.label, b.name) AS"
                        + " location_label, t.created_at, t.closed_at FROM tickets t JOIN"
                        + " ticket_categories tc ON t.category_id = tc.id JOIN users a ON"
                        + " t.author_id = a.id LEFT JOIN users ass ON t.assigned_to_id = ass.id"
                        + " LEFT JOIN apartments ap ON t.apartment_id = ap.id LEFT JOIN staircases"
                        + " st ON t.staircase_id = st.id LEFT JOIN buildings b ON t.building_id ="
                        + " b.id WHERE t.id = :ticketId AND t.is_deleted = false",
            nativeQuery = true)
    Optional<Object[]> findSummaryByIdRaw(@Param("ticketId") UUID ticketId);

    /**
     * Pobiera zgłoszenia przypisane do konserwatora jako DTO ze złączenia tabel tickets, users i
     * ticket_categories. Kwerenda natywna SQL filtrująca po assigned_to_id.
     *
     * @param conservatorId identyfikator konserwatora
     * @return lista zgłoszeń przypisanych do konserwatora z pełnymi danymi
     */
    @Query(
            value =
                    "SELECT t.id, t.ticket_number, t.title, t.status, "
                            + "tc.name AS category_name, "
                            + "CONCAT(a.first_name, ' ', a.last_name) AS author_name, "
                            + "CONCAT(ass.first_name, ' ', ass.last_name) AS assigned_to_name, "
                            + "COALESCE(ap.number, st.label, b.name) AS location_label, "
                            + "t.created_at, t.closed_at "
                            + "FROM tickets t "
                            + "JOIN ticket_categories tc ON t.category_id = tc.id "
                            + "JOIN users a ON t.author_id = a.id "
                            + "JOIN users ass ON t.assigned_to_id = ass.id "
                            + "LEFT JOIN apartments ap ON t.apartment_id = ap.id "
                            + "LEFT JOIN staircases st ON t.staircase_id = st.id "
                            + "LEFT JOIN buildings b ON t.building_id = b.id "
                            + "WHERE ass.id = :conservatorId AND t.is_deleted = false "
                            + "ORDER BY t.created_at DESC",
            nativeQuery = true)
    List<Object[]> findByConservatorRaw(@Param("conservatorId") UUID conservatorId);

    /**
     * Pobiera zgłoszenia widoczne dla mieszkańca (po lokalu, klatce lub budynku) ze złączenia tabel
     * tickets, users i ticket_categories. Kwerenda natywna SQL z wielokrotnym LEFT JOIN.
     *
     * @param apartmentId identyfikator lokalu mieszkańca
     * @param staircaseId identyfikator klatki mieszkańca
     * @param buildingId identyfikator budynku mieszkańca
     * @return lista zgłoszeń widocznych dla mieszkańca z pełnymi danymi
     */
    @Query(
            value =
                    "SELECT t.id, t.ticket_number, t.title, t.status, tc.name AS category_name,"
                        + " CONCAT(a.first_name, ' ', a.last_name) AS author_name, CASE WHEN ass.id"
                        + " IS NOT NULL THEN CONCAT(ass.first_name, ' ', ass.last_name) ELSE NULL"
                        + " END AS assigned_to_name, COALESCE(ap.number, st.label, b.name) AS"
                        + " location_label, t.created_at, t.closed_at FROM tickets t JOIN"
                        + " ticket_categories tc ON t.category_id = tc.id JOIN users a ON"
                        + " t.author_id = a.id LEFT JOIN users ass ON t.assigned_to_id = ass.id"
                        + " LEFT JOIN apartments ap ON t.apartment_id = ap.id LEFT JOIN staircases"
                        + " st ON t.staircase_id = st.id LEFT JOIN buildings b ON t.building_id ="
                        + " b.id WHERE t.is_deleted = false AND (t.apartment_id = :apartmentId OR"
                        + " t.staircase_id = :staircaseId OR t.building_id = :buildingId) ORDER BY"
                        + " t.created_at DESC",
            nativeQuery = true)
    List<Object[]> findForResidentRaw(
            @Param("apartmentId") UUID apartmentId,
            @Param("staircaseId") UUID staircaseId,
            @Param("buildingId") UUID buildingId);
}
