package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO z danymi nieruchomości przesyłanymi przez klienta (tworzenie i aktualizacja). */
@Data
public class PropertyRequest {

    @NotBlank(message = "Nazwa nieruchomości nie może być pusta")
    @Size(max = 255, message = "Nazwa nie może przekraczać 255 znaków")
    private String name;

    @NotBlank(message = "Adres nieruchomości nie może być pusty")
    @Size(max = 255, message = "Adres nie może przekraczać 255 znaków")
    private String address;

    @NotBlank(message = "NIP nieruchomości nie może być pusty")
    @Pattern(regexp = "\\d{10}", message = "NIP musi składać się z dokładnie 10 cyfr")
    private String nip;

    @Size(max = 20, message = "Numer telefonu nie może przekraczać 20 znaków")
    private String managerPhone;

    @Email(message = "Adres e-mail zarządcy ma niepoprawny format")
    @Size(max = 255, message = "Adres e-mail nie może przekraczać 255 znaków")
    private String managerEmail;

    /**
     * Konstruktor wszystkich pól — używany w testach jednostkowych.
     *
     * @param name nazwa nieruchomości
     * @param address adres nieruchomości
     * @param nip NIP (10 cyfr)
     * @param managerPhone numer telefonu zarządcy (opcjonalny)
     * @param managerEmail adres e-mail zarządcy (opcjonalny)
     */
    public PropertyRequest(
            String name, String address, String nip, String managerPhone, String managerEmail) {
        this.name = name;
        this.address = address;
        this.nip = nip;
        this.managerPhone = managerPhone;
        this.managerEmail = managerEmail;
    }
}
