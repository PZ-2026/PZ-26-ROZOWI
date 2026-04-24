package pl.edu.ur.blokur.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Document;

/** Repozytorium do zarządzania encjami dokumentów. */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Zwraca listę dokumentów powiązanych z mieszkaniem lub właścicielem.
     *
     * @param apartmentId identyfikator mieszkania
     * @param ownerUserId identyfikator właściciela
     * @return lista dokumentów
     */
    @Query(
            "SELECT d FROM Document d WHERE d.apartment.id = :apartmentId OR d.ownerUser.id ="
                    + " :ownerUserId ORDER BY d.createdAt DESC")
    List<Document> findByApartmentIdOrOwnerUserId(
            @Param("apartmentId") UUID apartmentId, @Param("ownerUserId") UUID ownerUserId);

    /**
     * Zwraca wszystkie dokumenty przefiltrowane przez zarządcę z sortowaniem od najnowszego.
     *
     * @param apartmentId opcjonalny identyfikator mieszkania
     * @param type opcjonalny typ dokumentu
     * @param startDate początek zakresu dat
     * @param endDate koniec zakresu dat
     * @return lista dokumentów
     */
    @Query(
            "SELECT d FROM Document d WHERE "
                    + "(:apartmentId IS NULL OR d.apartment.id = :apartmentId) AND "
                    + "(:type IS NULL OR d.type = :type) AND "
                    + "(:startDate IS NULL OR d.createdAt >= :startDate) AND "
                    + "(:endDate IS NULL OR d.createdAt <= :endDate) "
                    + "ORDER BY d.createdAt DESC")
    List<Document> findAllWithFilters(
            @Param("apartmentId") UUID apartmentId,
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}