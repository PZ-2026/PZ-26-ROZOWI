package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** DTO wyjściowe z danymi użytkownika zwracanymi przez endpointy administracyjne. */
@Data
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    private UUID apartmentId;

    /**
     * Tworzy odpowiedź z danymi użytkownika.
     *
     * @param id identyfikator użytkownika
     * @param firstName imię
     * @param lastName nazwisko
     * @param email adres e-mail
     * @param phone numer telefonu (może być {@code null})
     * @param role rola w systemie
     * @param active czy konto jest aktywne
     * @param createdAt data utworzenia konta
     * @param apartmentId identyfikator przypisanego lokalu (może być {@code null})
     */
    public UserResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String role,
            boolean active,
            LocalDateTime createdAt,
            UUID apartmentId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.apartmentId = apartmentId;
    }
}
