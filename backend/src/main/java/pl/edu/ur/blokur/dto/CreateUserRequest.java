package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** DTO wejściowe z danymi nowego użytkownika tworzonego przez zarządcę. */
public class CreateUserRequest {

    @NotBlank private String firstName;

    @NotBlank private String lastName;

    @NotBlank @Email private String email;

    @NotBlank private String role;

    @NotNull private UUID apartmentId;

    /**
     * Zwraca imię nowego użytkownika.
     *
     * @return imię
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Ustawia imię nowego użytkownika.
     *
     * @param firstName imię
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Zwraca nazwisko nowego użytkownika.
     *
     * @return nazwisko
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Ustawia nazwisko nowego użytkownika.
     *
     * @param lastName nazwisko
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Zwraca adres e-mail nowego użytkownika (używany jako login).
     *
     * @return adres e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Ustawia adres e-mail nowego użytkownika.
     *
     * @param email adres e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Zwraca rolę przypisywaną nowemu użytkownikowi.
     *
     * @return rola (np. MIESZKANIEC, ZARZADCA)
     */
    public String getRole() {
        return role;
    }

    /**
     * Ustawia rolę przypisywaną nowemu użytkownikowi.
     *
     * @param role rola użytkownika
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Zwraca identyfikator lokalu, do którego przypisywany jest nowy użytkownik.
     *
     * @return identyfikator UUID lokalu
     */
    public UUID getApartmentId() {
        return apartmentId;
    }

    /**
     * Ustawia identyfikator lokalu, do którego przypisywany jest nowy użytkownik.
     *
     * @param apartmentId identyfikator UUID lokalu
     */
    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }
}
