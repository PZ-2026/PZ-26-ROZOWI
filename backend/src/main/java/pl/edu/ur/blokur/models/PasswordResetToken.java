package pl.edu.ur.blokur.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Encja jednorazowego tokenu służącego do resetowania hasła lub zapraszania nowego użytkownika. */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
public class PasswordResetToken {

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

    /**
     * Tworzy token resetowania hasła powiązany z podanym użytkownikiem.
     *
     * @param user użytkownik, dla którego generowany jest token
     * @param token wygenerowana wartość tokenu
     * @param expiryDate data i czas wygaśnięcia tokenu
     */
    public PasswordResetToken(User user, String token, LocalDateTime expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
