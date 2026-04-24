package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/** DTO wejściowe z danymi do aktualizacji istniejącego użytkownika przez zarządcę. */
public class UpdateUserRequest {

    @NotBlank private String firstName;

    @NotBlank private String lastName;

    private String phone;

    @NotBlank private String role;

    private UUID apartmentId;

    /**
     * Zwraca imię użytkownika.
     *
     * @return imię
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Ustawia imię użytkownika.
     *
     * @param firstName imię
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
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
     * Ustawia nazwisko użytkownika.
     *
     * @param lastName nazwisko
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Zwraca numer telefonu użytkownika.
     *
     * @return numer telefonu lub {@code null} jeśli nie podano
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Ustawia numer telefonu użytkownika.
     *
     * @param phone numer telefonu
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Zwraca rolę przypisywaną użytkownikowi.
     *
     * @return rola (np. MIESZKANIEC, ZARZADCA)
     */
    public String getRole() {
        return role;
    }

    /**
     * Ustawia rolę przypisywaną użytkownikowi.
     *
     * @param role rola użytkownika
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Zwraca identyfikator lokalu przypisanego do użytkownika.
     *
     * @return identyfikator UUID lokalu lub {@code null} jeśli lokal nie ma być zmieniany
     */
    public UUID getApartmentId() {
        return apartmentId;
    }

    /**
     * Ustawia identyfikator lokalu przypisanego do użytkownika.
     *
     * @param apartmentId identyfikator UUID lokalu
     */
    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }
}
