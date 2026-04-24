package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO z danymi budynku zwracanymi przez API po operacjach tworzenia i edycji. */
public class BuildingResponse {

    private UUID id;
    private UUID propertyId;
    private String estateName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    /** Konstruktor bezargumentowy wymagany przez serializację JSON. */
    public BuildingResponse() {}

    /**
     * Zwraca identyfikator budynku.
     *
     * @return UUID budynku
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator budynku.
     *
     * @param id UUID budynku
     */
    public void setId(UUID id) {
        this.id = id;
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
}
