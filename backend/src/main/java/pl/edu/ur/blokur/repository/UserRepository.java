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
}
