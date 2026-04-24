package pl.edu.ur.blokur.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Encja długożyjącego refresh tokenu JWT, używanego do pobierania nowej pary tokenów bez ponownego
 * logowania. Każde użycie tokenu powoduje jego unieważnienie i wygenerowanie nowego (rotacja).
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

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

    @Column(nullable = false)
    private boolean revoked = false;

    /** Konstruktor bezargumentowy wymagany przez JPA. */
    public RefreshToken() {}

    /**
     * Tworzy aktywny refresh token powiązany z podanym użytkownikiem.
     *
     * @param user użytkownik, dla którego generowany jest token
     * @param token wygenerowana wartość tokenu
     * @param expiryDate data i czas wygaśnięcia tokenu
     */
    public RefreshToken(User user, String token, LocalDateTime expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
        this.revoked = false;
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
     * Zwraca wartość tokenu (ciąg znaków przesyłany klientowi).
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

    /**
     * Informuje, czy token został unieważniony (np. po użyciu lub wylogowaniu).
     *
     * @return {@code true} jeśli token jest unieważniony
     */
    public boolean isRevoked() {
        return revoked;
    }

    /**
     * Ustawia flagę unieważnienia tokenu.
     *
     * @param revoked {@code true} aby unieważnić token
     */
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
