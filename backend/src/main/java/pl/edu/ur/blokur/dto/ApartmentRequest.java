package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** DTO z danymi lokalu przesyłanymi przez klienta (tworzenie i aktualizacja). */
public class ApartmentRequest {

    @NotBlank(message = "Numer lokalu nie może być pusty")
    @Size(max = 50, message = "Numer lokalu nie może przekraczać 50 znaków")
    private String number;

    private Integer floor;

    private BigDecimal areaM2;

    @Pattern(
            regexp = "WLASNOSCIOWY|NAJEM",
            message = "Typ własności musi być WLASNOSCIOWY lub NAJEM")
    private String ownershipType;

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public ApartmentRequest() {}

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
}
