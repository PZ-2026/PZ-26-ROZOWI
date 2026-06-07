package pl.edu.ur.blokur.service;

import java.time.LocalDateTime;
import java.util.UUID;
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
 * Serwis obsługujący zaproszenia nowych użytkowników do systemu. Generuje jednorazowy token z
 * ważnością 72 h i wysyła wiadomość powitalną z linkiem aktywacyjnym.
 */
@Service
public class InvitationService {

    private final InvitationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.invitation.base-url:https://blokur.pl/invite}")
    private String inviteBaseUrl;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param tokenRepository repozytorium tokenów zaproszenia
     * @param userRepository repozytorium użytkowników
     * @param mailSender klient SMTP do wysyłki e-maili
     * @param passwordEncoder enkoder haseł (BCrypt)
     */
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
     * Generuje token zaproszenia (TTL 72 h) dla nowo utworzonego konta i wysyła e-mail z linkiem
     * aktywacyjnym.
     *
     * @param user nowo utworzony użytkownik
     */
    public void inviteUser(User user) {
        var token = UUID.randomUUID().toString();
        var expiry = LocalDateTime.now().plusHours(72);
        tokenRepository.save(new InvitationToken(user, token, expiry));
        sendInvitationEmail(user.getEmail(), user.getFirstName(), token);
    }

    /**
     * Przyjmuje zaproszenie — ustawia hasło użytkownika i unieważnia token.
     *
     * <p>Zwraca 410 gdy token wygasł, 400 gdy token nie istnieje.
     *
     * @param token wartość tokenu z linku aktywacyjnego
     * @param newPassword hasło wybrane przez użytkownika (zostanie zahashowane)
     * @throws IllegalArgumentException gdy token nie istnieje w bazie
     * @throws TokenExpiredException gdy token wygasł (HTTP 410)
     */
    @Transactional
    public void acceptInvitation(String token, String newPassword) {
        var invitationToken =
                tokenRepository
                        .findByToken(token)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Nieprawidłowy token zaproszenia"));

        if (invitationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(invitationToken);
            throw new TokenExpiredException("Token zaproszenia wygasł");
        }

        var user = invitationToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(invitationToken);
    }

    @Async
    protected void sendInvitationEmail(String email, String firstName, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – witamy w systemie!");
        message.setText(
                "Cześć "
                        + firstName
                        + "!\n\n"
                        + "Administrator utworzył dla Ciebie konto w aplikacji Blokur.\n\n"
                        + "Kliknij poniższy link, aby ustawić swoje hasło (link ważny przez 72"
                        + " godziny):\n"
                        + inviteBaseUrl
                        + "/"
                        + token
                        + "\n\n"
                        + "Jeśli nie spodziewałeś się tej wiadomości, skontaktuj się z"
                        + " administratorem.");
        mailSender.send(message);
    }
}
