package pl.edu.ur.blokur.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Serwis odpowiedzialny za asynchroniczne wysyłanie wiadomości e-mail. Metody oznaczone
 * {@link Async} są wykonywane w osobnym wątku zarządzanym przez Spring, dzięki czemu nie blokują
 * bieżącej transakcji bazodanowej.
 */
@Service
public class AsyncEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.invitation.base-url:https://blokur.pl/invite}")
    private String inviteBaseUrl;

    @Value("${app.reset-password.base-url:https://blokur.pl/reset}")
    private String resetBaseUrl;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param mailSender klient SMTP do wysyłki e-maili
     */
    public AsyncEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Wysyła e-mail z linkiem aktywacyjnym dla nowego użytkownika.
     *
     * @param email adres e-mail odbiorcy
     * @param firstName imię użytkownika (personalizacja treści)
     * @param token token zaproszenia zawarty w linku aktywacyjnym
     */
    @Async
    public void sendInvitationEmail(String email, String firstName, String token) {
        var message = new SimpleMailMessage();
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

    /**
     * Wysyła e-mail z linkiem do resetu hasła.
     *
     * @param email adres e-mail odbiorcy
     * @param token token resetu hasła zawarty w linku
     */
    @Async
    public void sendResetEmail(String email, String token) {
        var message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Blokur – reset hasła");
        message.setText(
                """
                Otrzymaliśmy prośbę o reset hasła do Twojego konta w aplikacji Blokur.

                Kliknij poniższy link, aby ustawić nowe hasło (ważny przez 1 godzinę):
                %s?token=%s

                Jeśli to nie Ty wysłałeś tę prośbę, zignoruj tę wiadomość.
                """
                        .formatted(resetBaseUrl, token));
        mailSender.send(message);
    }
}
