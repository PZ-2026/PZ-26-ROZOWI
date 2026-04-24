package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca klatkę schodową w budynku. */
@Entity
@Table(name = "staircases")
public class Staircase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "label", nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @OneToMany(mappedBy = "staircase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Apartment> apartments = new ArrayList<>();

    /**
     * Zwraca unikalny identyfikator klatki schodowej.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator klatki schodowej.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca etykietę klatki schodowej (np. "A", "B", "1").
     *
     * @return etykieta klatki
     */
    public String getLabel() {
        return label;
    }

    /**
     * Ustawia etykietę klatki schodowej.
     *
     * @param label etykieta klatki
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Zwraca budynek, do którego należy klatka schodowa.
     *
     * @return encja budynku
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * Ustawia budynek, do którego należy klatka schodowa.
     *
     * @param building encja budynku
     */
    public void setBuilding(Building building) {
        this.building = building;
    }

    /**
     * Zwraca listę lokali przypisanych do tej klatki schodowej.
     *
     * @return lista lokali
     */
    public List<Apartment> getApartments() {
        return apartments;
    }

    /**
     * Ustawia listę lokali przypisanych do tej klatki schodowej.
     *
     * @param apartments lista lokali
     */
    public void setApartments(List<Apartment> apartments) {
        this.apartments = apartments;
    }
}
