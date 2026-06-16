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
import pl.edu.ur.blokur.exception.TokenExpiredException;
import pl.edu.ur.blokur.models.InvitationToken;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.InvitationTokenRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis obsługujący zaproszenia nowych użytkowników do systemu. Generuje 6-cyfrowy kod
 * aktywacyjny z ważnością 72 h i wysyła wiadomość powitalną zawierającą sam kod.
 */
@Service
public class InvitationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvitationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public InvitationService(
            InvitationTokenRepository tokenRepository,
            UserRepository userRepository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generuje 6-cyfrowy kod aktywacyjny (TTL 72 h) dla nowo utworzonego konta i wysyła e-mail z
     * kodem. Wcześniejsze kody zaproszenia dla tego użytkownika są usuwane.
     */
    @Transactional
    public void inviteUser(User user) {
        tokenRepository.deleteByUser(user);
        var code = generateCode();
        var expiry = LocalDateTime.now().plusHours(72);
        tokenRepository.save(new InvitationToken(user, code, expiry));
        sendInvitationEmail(user.getEmail(), user.getFirstName(), code);
    }

    /**
     * Przyjmuje zaproszenie — ustawia hasło użytkownika i unieważnia kod.
     *
     * @throws IllegalArgumentException gdy email lub kod jest nieprawidłowy
     * @throws TokenExpiredException gdy kod wygasł (HTTP 410)
     */
    @Transactional
    public void acceptInvitation(String email, String code, String newPassword) {
        var user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Nieprawidłowy kod zaproszenia"));

        var invitationToken =
                tokenRepository
                        .findByUserAndToken(user, code)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Nieprawidłowy kod zaproszenia"));

        if (invitationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(invitationToken);
            throw new TokenExpiredException("Kod zaproszenia wygasł");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(invitationToken);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    @Async
    protected void sendInvitationEmail(String email, String firstName, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – witamy w systemie!");
        message.setText(
                "Cześć "
                        + firstName
                        + "!\n\n"
                        + "Administrator utworzył dla Ciebie konto w aplikacji Blokur.\n\n"
                        + "Twój kod aktywacyjny (ważny przez 72 godziny):\n\n"
                        + "    "
                        + code
                        + "\n\n"
                        + "Wpisz kod w aplikacji, aby ustawić hasło i aktywować konto.\n\n"
                        + "Jeśli nie spodziewałeś się tej wiadomości, skontaktuj się z"
                        + " administratorem.");
        mailSender.send(message);
    }
}
