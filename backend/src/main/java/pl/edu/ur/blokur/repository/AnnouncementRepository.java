package pl.edu.ur.blokur.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    /**
     * Zwraca listę ogłoszeń dopasowanych do użytkownika, tzn.: - Ogłoszenia globalne (gdzie
     * targetBuilding, targetStaircase i targetApartment są równoważnie null). - Ogłoszenia
     * kierowane do bloku (targetBuilding.id = buildingId). - Ogłoszenia kierowane do klatki
     * (targetStaircase.id = staircaseId). - Ogłoszenia dla mieszkania (targetApartment.id =
     * apartmentId).
     *
     * <p>Ze względu na CHECK w bazie, tylko jedno z target id ma wartość.
     */
    @Query(
            "SELECT a FROM Announcement a "
                    + "WHERE (a.targetBuilding IS NULL AND a.targetStaircase IS NULL "
                    + "AND a.targetApartment IS NULL) "
                    + "OR (a.targetBuilding.id = :buildingId) "
                    + "OR (a.targetStaircase.id = :staircaseId) "
                    + "OR (a.targetApartment.id = :apartmentId)")
    List<Announcement> findForUser(
            @Param("buildingId") UUID buildingId,
            @Param("staircaseId") UUID staircaseId,
            @Param("apartmentId") UUID apartmentId);

    /**
     * Zwraca listę ogłoszeń dopasowanych do użytkownika, utworzonych po podanej dacie. Ogłoszenia
     * starsze niż podana data graniczna nie są zwracane.
     *
     * @param buildingId identyfikator budynku
     * @param staircaseId identyfikator klatki
     * @param apartmentId identyfikator lokalu
     * @param cutoffDate data graniczna (ogłoszenia starsze nie będą zwracane)
     * @return lista ogłoszeń posortowana od najnowszego
     */
    @Query(
            "SELECT a FROM Announcement a "
                    + "WHERE a.createdAt >= :cutoffDate "
                    + "AND ((a.targetBuilding IS NULL AND a.targetStaircase IS NULL "
                    + "AND a.targetApartment IS NULL) "
                    + "OR (a.targetBuilding.id = :buildingId) "
                    + "OR (a.targetStaircase.id = :staircaseId) "
                    + "OR (a.targetApartment.id = :apartmentId)) "
                    + "ORDER BY a.createdAt DESC")
    List<Announcement> findForUserAfterDate(
            @Param("buildingId") UUID buildingId,
            @Param("staircaseId") UUID staircaseId,
            @Param("apartmentId") UUID apartmentId,
            @Param("cutoffDate") LocalDateTime cutoffDate);
}
