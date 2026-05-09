package pl.edu.ur.blokur.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.models.PasswordResetToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.PasswordResetTokenRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis obsługujący reset hasła użytkownika. Generuje jednorazowy token z TTL 1 h i wysyła
 * wiadomość e-mail z linkiem do ustawienia nowego hasła.
 */
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.reset-password.base-url:https://blokur.pl/reset}")
    private String resetBaseUrl;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param userRepository repozytorium użytkowników
     * @param tokenRepository repozytorium tokenów resetu hasła
     * @param mailSender klient SMTP do wysyłki e-maili
     * @param passwordEncoder enkoder haseł (BCrypt)
     */
    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Rozpoczyna procedurę resetu hasła dla użytkownika o podanym adresie e-mail. Ze względów
     * bezpieczeństwa metoda nie ujawnia, czy email istnieje w bazie.
     *
     * @param email adres e-mail, na który ma zostać wysłany link resetujący
     */
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        // Zawsze zwracamy sukces — nie ujawniamy czy email istnieje w bazie
        if (userOpt.isEmpty()) {
            return;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        tokenRepository.save(new PasswordResetToken(userOpt.get(), token, expiry));

        sendResetEmail(email, token);
    }

    /**
     * Ustawia nowe hasło użytkownika na podstawie ważnego tokenu resetującego.
     *
     * @param token wartość tokenu resetującego
     * @param newPassword nowe hasło (zostanie zahashowane)
     * @throws IllegalArgumentException gdy token nie istnieje lub wygasł
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken =
                tokenRepository
                        .findByToken(token)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Nieprawidłowy token resetowania hasła"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token wygasł");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    @Async
    protected void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – reset hasła");
        message.setText(
                "Otrzymaliśmy prośbę o reset hasła do Twojego konta w aplikacji Blokur.\n\n"
                        + "Kliknij poniższy link, aby ustawić nowe hasło (ważny przez 1 godzinę):\n"
                        + resetBaseUrl
                        + "?token="
                        + token
                        + "\n\n"
                        + "Jeśli to nie Ty wysłałeś tę prośbę, zignoruj tę wiadomość.");
        mailSender.send(message);
    }
}
