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
}
