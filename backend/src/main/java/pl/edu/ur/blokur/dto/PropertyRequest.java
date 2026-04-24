package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO z danymi nieruchomości przesyłanymi przez klienta (tworzenie i aktualizacja). */
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

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public PropertyRequest() {}

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

    /**
     * Zwraca nazwę nieruchomości.
     *
     * @return nazwa nieruchomości
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę nieruchomości.
     *
     * @param name nazwa nieruchomości
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Zwraca adres nieruchomości.
     *
     * @return adres nieruchomości
     */
    public String getAddress() {
        return address;
    }

    /**
     * Ustawia adres nieruchomości.
     *
     * @param address adres nieruchomości
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Zwraca NIP nieruchomości.
     *
     * @return NIP (10 cyfr)
     */
    public String getNip() {
        return nip;
    }

    /**
     * Ustawia NIP nieruchomości.
     *
     * @param nip NIP (dokładnie 10 cyfr)
     */
    public void setNip(String nip) {
        this.nip = nip;
    }

    /**
     * Zwraca numer telefonu zarządcy.
     *
     * @return numer telefonu lub {@code null}
     */
    public String getManagerPhone() {
        return managerPhone;
    }

    /**
     * Ustawia numer telefonu zarządcy.
     *
     * @param managerPhone numer telefonu
     */
    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    /**
     * Zwraca adres e-mail zarządcy.
     *
     * @return adres e-mail lub {@code null}
     */
    public String getManagerEmail() {
        return managerEmail;
    }

    /**
     * Ustawia adres e-mail zarządcy.
     *
     * @param managerEmail adres e-mail
     */
    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }
}
