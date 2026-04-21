package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.RefreshToken;
import pl.edu.ur.blokur.models.User;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUser(User user);
}
