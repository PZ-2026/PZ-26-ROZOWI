package pl.edu.ur.blokur.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.models.PasswordResetToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.PasswordResetTokenRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis obsługujący reset hasła użytkownika. Generuje 6-cyfrowy kod z TTL 1 h i wysyła wiadomość
 * e-mail zawierającą sam kod.
 */
@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromAddress;

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
     * Rozpoczyna procedurę resetu hasła — wysyła użytkownikowi 6-cyfrowy kod (TTL 1 h). Ze
     * względów bezpieczeństwa metoda nie ujawnia, czy email istnieje w bazie. Wcześniejsze kody
     * resetu dla tego użytkownika są usuwane.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }
        var user = userOpt.get();

        tokenRepository.deleteByUser(user);

        var code = generateCode();
        var expiry = LocalDateTime.now().plusHours(1);

        tokenRepository.save(new PasswordResetToken(user, code, expiry));

        sendResetEmail(email, code);
    }

    /**
     * Ustawia nowe hasło użytkownika na podstawie ważnego kodu resetującego.
     *
     * @throws IllegalArgumentException gdy email lub kod jest nieprawidłowy, lub kod wygasł
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        var user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Nieprawidłowy kod resetowania hasła"));

        var resetToken =
                tokenRepository
                        .findByUserAndToken(user, code)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Nieprawidłowy kod resetowania hasła"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Kod wygasł");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    @Async
    protected void sendResetEmail(String email, String code) {
        var message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – reset hasła");
        message.setText(
                """
                Otrzymaliśmy prośbę o reset hasła do Twojego konta w aplikacji Blokur.

                Twój kod resetujący (ważny przez 1 godzinę):

                    %s

                Wpisz kod w aplikacji, aby ustawić nowe hasło.

                Jeśli to nie Ty wysłałeś tę prośbę, zignoruj tę wiadomość.
                """
                        .formatted(code));
        mailSender.send(message);
    }
}
