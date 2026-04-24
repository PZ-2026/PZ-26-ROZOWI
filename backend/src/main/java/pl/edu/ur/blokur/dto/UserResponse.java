package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO wyjściowe z danymi użytkownika zwracanymi przez endpointy administracyjne. */
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

    /**
     * Zwraca identyfikator użytkownika.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Zwraca imię użytkownika.
     *
     * @return imię
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Zwraca nazwisko użytkownika.
     *
     * @return nazwisko
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Zwraca adres e-mail użytkownika.
     *
     * @return adres e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Zwraca numer telefonu użytkownika.
     *
     * @return numer telefonu lub {@code null}
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Zwraca rolę użytkownika w systemie.
     *
     * @return rola (np. MIESZKANIEC, ZARZADCA)
     */
    public String getRole() {
        return role;
    }

    /**
     * Informuje, czy konto użytkownika jest aktywne.
     *
     * @return {@code true} jeśli konto jest aktywne
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Zwraca datę i czas utworzenia konta.
     *
     * @return data i czas rejestracji
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Zwraca identyfikator lokalu przypisanego do użytkownika.
     *
     * @return identyfikator UUID lokalu lub {@code null} jeśli brak przypisania
     */
    public UUID getApartmentId() {
        return apartmentId;
    }
}
