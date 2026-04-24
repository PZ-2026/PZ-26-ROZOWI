package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca budynek przypisany do wspólnoty/osiedla. */
@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "estate_name", nullable = false)
    private String estateName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Staircase> staircases = new ArrayList<>();

    /**
     * Zwraca unikalny identyfikator budynku.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator budynku.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca nazwę osiedla/wspólnoty, do którego należy budynek.
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
     * Zwraca szerokość geograficzną lokalizacji budynku.
     *
     * @return szerokość geograficzna
     */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /**
     * Ustawia szerokość geograficzną lokalizacji budynku.
     *
     * @param latitude szerokość geograficzna
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    /**
     * Zwraca długość geograficzną lokalizacji budynku.
     *
     * @return długość geograficzna
     */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /**
     * Ustawia długość geograficzną lokalizacji budynku.
     *
     * @param longitude długość geograficzna
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    /**
     * Zwraca listę klatek schodowych należących do budynku.
     *
     * @return lista klatek schodowych
     */
    public List<Staircase> getStaircases() {
        return staircases;
    }

    /**
     * Ustawia listę klatek schodowych należących do budynku.
     *
     * @param staircases lista klatek schodowych
     */
    public void setStaircases(List<Staircase> staircases) {
        this.staircases = staircases;
    }
}
