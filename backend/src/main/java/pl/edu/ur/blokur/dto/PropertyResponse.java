package pl.edu.ur.blokur.dto;

import java.util.UUID;

/** DTO z danymi nieruchomości zwracanymi przez API. */
public class PropertyResponse {

    private UUID id;
    private String name;
    private String address;
    private String nip;
    private String managerPhone;
    private String managerEmail;
    private String logoPath;

    /**
     * Konstruktor wszystkich pól.
     *
     * @param id identyfikator UUID nieruchomości
     * @param name nazwa nieruchomości
     * @param address adres nieruchomości
     * @param nip NIP nieruchomości
     * @param managerPhone numer telefonu zarządcy (może być {@code null})
     * @param managerEmail adres e-mail zarządcy (może być {@code null})
     * @param logoPath ścieżka pliku logo (może być {@code null})
     */
    public PropertyResponse(
            UUID id,
            String name,
            String address,
            String nip,
            String managerPhone,
            String managerEmail,
            String logoPath) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.nip = nip;
        this.managerPhone = managerPhone;
        this.managerEmail = managerEmail;
        this.logoPath = logoPath;
    }

    /**
     * Zwraca identyfikator nieruchomości.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator nieruchomości.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
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
     * @param nip NIP
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

    /**
     * Zwraca ścieżkę pliku logo na dysku serwera.
     *
     * @return ścieżka logo lub {@code null} jeśli logo nie zostało przesłane
     */
    public String getLogoPath() {
        return logoPath;
    }

    /**
     * Ustawia ścieżkę pliku logo.
     *
     * @param logoPath ścieżka do pliku logo
     */
    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
}
