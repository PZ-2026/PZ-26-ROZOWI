package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO z danymi lokalu zwracanymi przez API po operacjach tworzenia i edycji. */
public class ApartmentResponse {

    private UUID id;
    private UUID staircaseId;
    private String number;
    private Integer floor;
    private BigDecimal areaM2;
    private String ownershipType;
    private BigDecimal currentBalance;

    /** Konstruktor bezargumentowy wymagany przez serializację JSON. */
    public ApartmentResponse() {}

    /**
     * Zwraca identyfikator lokalu.
     *
     * @return UUID lokalu
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator lokalu.
     *
     * @param id UUID lokalu
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator klatki schodowej, w której znajduje się lokal.
     *
     * @return UUID klatki
     */
    public UUID getStaircaseId() {
        return staircaseId;
    }

    /**
     * Ustawia identyfikator klatki schodowej.
     *
     * @param staircaseId UUID klatki
     */
    public void setStaircaseId(UUID staircaseId) {
        this.staircaseId = staircaseId;
    }

    /**
     * Zwraca numer lokalu.
     *
     * @return numer lokalu
     */
    public String getNumber() {
        return number;
    }

    /**
     * Ustawia numer lokalu.
     *
     * @param number numer lokalu
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Zwraca piętro lokalu.
     *
     * @return numer piętra lub {@code null}
     */
    public Integer getFloor() {
        return floor;
    }

    /**
     * Ustawia piętro lokalu.
     *
     * @param floor numer piętra
     */
    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    /**
     * Zwraca powierzchnię lokalu w m².
     *
     * @return powierzchnia lub {@code null}
     */
    public BigDecimal getAreaM2() {
        return areaM2;
    }

    /**
     * Ustawia powierzchnię lokalu w m².
     *
     * @param areaM2 powierzchnia w m²
     */
    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    /**
     * Zwraca typ własności lokalu.
     *
     * @return WLASNOSCIOWY, NAJEM lub {@code null}
     */
    public String getOwnershipType() {
        return ownershipType;
    }

    /**
     * Ustawia typ własności lokalu.
     *
     * @param ownershipType WLASNOSCIOWY lub NAJEM
     */
    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    /**
     * Zwraca aktualne saldo rozliczeniowe lokalu.
     *
     * @return saldo w PLN
     */
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    /**
     * Ustawia aktualne saldo rozliczeniowe lokalu.
     *
     * @param currentBalance saldo w PLN
     */
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
}
