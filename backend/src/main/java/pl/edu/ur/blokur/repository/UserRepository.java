package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.ur.blokur.models.User;

/**
 * Repozytorium JPA dla encji {@link User}. Udostępnia wyszukiwanie użytkownika po adresie email
 * (login).
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Wyszukuje użytkownika po adresie email.
     *
     * @param email adres email (login) użytkownika
     * @return opcjonalny użytkownik
     */
    Optional<User> findByEmail(String email);

    @Query(
            "SELECT u FROM User u WHERE "
                    + "(:search IS NULL OR :search = '' OR "
                    + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
                    + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
                    + "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query(
            "SELECT new pl.edu.ur.blokur.dto.UserWithTicketsDto(u.id, u.firstName, u.lastName,"
                + " u.email, u.phone, COUNT(t)) FROM User u LEFT JOIN Ticket t ON t.assignedTo = u"
                + " AND t.status NOT IN (pl.edu.ur.blokur.models.TicketStatus.ZAMKNIETE,"
                + " pl.edu.ur.blokur.models.TicketStatus.ODRZUCONE) WHERE u.role = :role GROUP BY"
                + " u.id")
    List<pl.edu.ur.blokur.dto.UserWithTicketsDto> findUsersWithActiveTicketsByRole(
            @Param("role") String role);

    @Query("SELECT DISTINCT ua.user.id FROM UserApartment ua")
    List<UUID> findAllResidentIds();

    @Query(
            "SELECT DISTINCT ua.user.id FROM UserApartment ua"
                    + " WHERE ua.apartment.staircase.building.id = :buildingId")
    List<UUID> findUserIdsByBuildingId(@Param("buildingId") UUID buildingId);

    @Query(
            "SELECT DISTINCT ua.user.id FROM UserApartment ua"
                    + " WHERE ua.apartment.staircase.id = :staircaseId")
    List<UUID> findUserIdsByStaircaseId(@Param("staircaseId") UUID staircaseId);

    @Query(
            "SELECT DISTINCT ua.user.id FROM UserApartment ua"
                    + " WHERE ua.apartment.id = :apartmentId")
    List<UUID> findUserIdsByApartmentId(@Param("apartmentId") UUID apartmentId);

    @Query(
            "SELECT DISTINCT ua.user.id FROM UserApartment ua"
                    + " WHERE ua.apartment.staircase.building.property.id = :propertyId")
    List<UUID> findUserIdsByPropertyId(@Param("propertyId") UUID propertyId);

    @Query("SELECT u.id FROM User u WHERE u.role = 'ZARZADCA'")
    List<UUID> findManagerIds();
}
