package pl.edu.ur.blokur.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @org.springframework.data.jpa.repository.Query(
            "SELECT new pl.edu.ur.blokur.dto.UserWithTicketsDto(u.id, u.firstName, u.lastName,"
                + " u.email, u.phone, COUNT(t)) FROM User u LEFT JOIN Ticket t ON t.assignedTo = u"
                + " AND t.status NOT IN (pl.edu.ur.blokur.models.TicketStatus.ZAMKNIETE,"
                + " pl.edu.ur.blokur.models.TicketStatus.ODRZUCONE) WHERE u.role = :role GROUP BY"
                + " u.id")
    java.util.List<pl.edu.ur.blokur.dto.UserWithTicketsDto> findUsersWithActiveTicketsByRole(
            @org.springframework.data.repository.query.Param("role") String role);

    /**
     * Zlicza aktywnych użytkowników z rolą {@code MIESZKANIEC} przypisanych do lokali w danym
     * budynku. Używane do obliczenia liczby odbiorców ogłoszenia skierowanego do budynku (WF-05).
     *
     * @param buildingId identyfikator budynku
     * @return liczba mieszkańców w budynku
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(DISTINCT u) FROM User u "
                    + "JOIN u.userApartments ua "
                    + "JOIN ua.apartment ap "
                    + "JOIN ap.staircase sc "
                    + "JOIN sc.building b "
                    + "WHERE b.id = :buildingId AND u.role = 'MIESZKANIEC' AND u.isActive = true")
    long countResidentsByBuildingId(
            @org.springframework.data.repository.query.Param("buildingId") java.util.UUID buildingId);

    /**
     * Zlicza aktywnych użytkowników z rolą {@code MIESZKANIEC} przypisanych do lokali w danej
     * klatce schodowej. Używane do obliczenia liczby odbiorców ogłoszenia (WF-05).
     *
     * @param staircaseId identyfikator klatki schodowej
     * @return liczba mieszkańców w klatce
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(DISTINCT u) FROM User u "
                    + "JOIN u.userApartments ua "
                    + "JOIN ua.apartment ap "
                    + "JOIN ap.staircase sc "
                    + "WHERE sc.id = :staircaseId AND u.role = 'MIESZKANIEC' AND u.isActive = true")
    long countResidentsByStaircaseId(
            @org.springframework.data.repository.query.Param("staircaseId")
                    java.util.UUID staircaseId);

    /**
     * Zlicza wszystkich aktywnych użytkowników z rolą {@code MIESZKANIEC}. Używane do obliczenia
     * liczby odbiorców ogłoszenia skierowanego do wszystkich (WF-05).
     *
     * @return łączna liczba aktywnych mieszkańców w systemie
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(u) FROM User u "
                    + "WHERE u.role = 'MIESZKANIEC' AND u.isActive = true")
    long countAllResidents();

    /**
     * Zlicza aktywnych użytkowników z rolą {@code MIESZKANIEC} przypisanych do danego lokalu.
     * Używane do obliczenia liczby odbiorców ogłoszenia skierowanego do konkretnego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @return liczba mieszkańców lokalu
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(DISTINCT u) FROM User u "
                    + "JOIN u.userApartments ua "
                    + "JOIN ua.apartment ap "
                    + "WHERE ap.id = :apartmentId AND u.role = 'MIESZKANIEC' AND u.isActive = true")
    long countResidentsByApartmentId(
            @org.springframework.data.repository.query.Param("apartmentId")
                    java.util.UUID apartmentId);
}
