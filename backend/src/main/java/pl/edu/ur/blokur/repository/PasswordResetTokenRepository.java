package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByToken(String token);
}
