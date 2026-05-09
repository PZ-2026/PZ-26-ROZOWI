package pl.edu.ur.blokur.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Encja jednorazowego tokenu zaproszenia wysyłanego nowemu użytkownikowi (TTL 72 h). */
@Entity
@Table(name = "invitation_tokens")
public class InvitationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id")
    private UUID tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /** Konstruktor bezargumentowy wymagany przez JPA. */
    public InvitationToken() {}

    /**
     * Tworzy token zaproszenia powiązany z podanym użytkownikiem.
     *
     * @param user użytkownik, dla którego generowany jest token
     * @param token wygenerowana wartość tokenu
     * @param expiryDate data i czas wygaśnięcia tokenu (72 h od chwili wysyłki)
     */
    public InvitationToken(User user, String token, LocalDateTime expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    /**
     * Zwraca unikalny identyfikator tokenu.
     *
     * @return identyfikator UUID
     */
    public UUID getTokenId() {
        return tokenId;
    }

    /**
     * Zwraca użytkownika, dla którego wystawiono token.
     *
     * @return encja użytkownika
     */
    public User getUser() {
        return user;
    }

    /**
     * Zwraca wartość tokenu (ciąg znaków wysyłany e-mailem).
     *
     * @return wartość tokenu
     */
    public String getToken() {
        return token;
    }

    /**
     * Zwraca datę i czas wygaśnięcia tokenu.
     *
     * @return data i czas wygaśnięcia
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
}
