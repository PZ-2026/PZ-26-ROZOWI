package pl.edu.ur.blokur.service;

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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy token resetowania hasła"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token wygasł");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    public void inviteUser(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(72);
        tokenRepository.save(new PasswordResetToken(user, token, expiry));
        sendInvitationEmail(user.getEmail(), user.getFirstName(), token);
    }

    @Async
    protected void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – reset hasła");
        message.setText(
            "Otrzymaliśmy prośbę o reset hasła do Twojego konta w aplikacji Blokur.\n\n" +
            "Kliknij poniższy link, aby ustawić nowe hasło (ważny przez 1 godzinę):\n" +
            resetBaseUrl + "?token=" + token + "\n\n" +
            "Jeśli to nie Ty wysłałeś tę prośbę, zignoruj tę wiadomość."
        );
        mailSender.send(message);
    }

    @Async
    protected void sendInvitationEmail(String email, String firstName, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – witamy w systemie!");
        message.setText(
            "Cześć " + firstName + "!\n\n" +
            "Administrator utworzył dla Ciebie konto w aplikacji Blokur.\n\n" +
            "Kliknij poniższy link, aby ustawić swoje hasło (link ważny przez 72 godziny):\n" +
            resetBaseUrl + "?token=" + token + "\n\n" +
            "Jeśli nie spodziewałeś się tej wiadomości, skontaktuj się z administratorem."
        );
        mailSender.send(message);
    }
}
