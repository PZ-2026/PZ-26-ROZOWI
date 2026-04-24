package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/** DTO z danymi budynku przesyłanymi przez klienta (tworzenie i aktualizacja). */
public class BuildingRequest {

    @NotBlank(message = "Nazwa osiedla nie może być pusta")
    @Size(max = 255, message = "Nazwa osiedla nie może przekraczać 255 znaków")
    private String estateName;

    @NotBlank(message = "Nazwa budynku nie może być pusta")
    @Size(max = 255, message = "Nazwa budynku nie może przekraczać 255 znaków")
    private String name;

    @NotBlank(message = "Adres budynku nie może być pusty")
    @Size(max = 255, message = "Adres nie może przekraczać 255 znaków")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private UUID propertyId;

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public BuildingRequest() {}

    /**
     * Zwraca nazwę osiedla/wspólnoty.
     *
     * @return nazwa osiedla
     */
    public String getEstateName() {
        return estateName;
    }

    /**
     * Ustawia nazwę osiedla/wspólnoty.
     *
     * @param estateName nazwa osiedla
     */
    public void setEstateName(String estateName) {
        this.estateName = estateName;
    }

    /**
     * Zwraca nazwę budynku.
     *
     * @return nazwa budynku
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę budynku.
     *
     * @param name nazwa budynku
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Zwraca adres budynku.
     *
     * @return adres budynku
     */
    public String getAddress() {
        return address;
    }

    /**
     * Ustawia adres budynku.
     *
     * @param address adres budynku
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Zwraca szerokość geograficzną.
     *
     * @return szerokość lub {@code null}
     */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /**
     * Ustawia szerokość geograficzną.
     *
     * @param latitude szerokość geograficzna
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    /**
     * Zwraca długość geograficzną.
     *
     * @return długość lub {@code null}
     */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /**
     * Ustawia długość geograficzną.
     *
     * @param longitude długość geograficzna
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    /**
     * Zwraca identyfikator nieruchomości nadrzędnej.
     *
     * @return UUID nieruchomości lub {@code null}
     */
    public UUID getPropertyId() {
        return propertyId;
    }

    /**
     * Ustawia identyfikator nieruchomości nadrzędnej.
     *
     * @param propertyId UUID nieruchomości
     */
    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }
}
