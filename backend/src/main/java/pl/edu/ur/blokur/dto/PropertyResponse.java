package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO z danymi nieruchomości zwracanymi przez API. */
@Data
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
}
