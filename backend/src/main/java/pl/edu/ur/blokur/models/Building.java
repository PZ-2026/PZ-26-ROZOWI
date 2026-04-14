package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * Encja reprezentująca budynek przypisany do wspólnoty/osiedla.
 */
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEstateName() {
        return estateName;
    }

    public void setEstateName(String estateName) {
        this.estateName = estateName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public List<Staircase> getStaircases() {
        return staircases;
    }

    public void setStaircases(List<Staircase> staircases) {
        this.staircases = staircases;
    }
}
