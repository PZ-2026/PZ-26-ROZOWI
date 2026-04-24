package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/**
 * Encja reprezentująca nieruchomość (wspólnotę mieszkaniową) — najwyższy poziom hierarchii danych.
 * Nieruchomość grupuje budynki i przechowuje dane identyfikacyjne (NIP) oraz kontaktowe zarządcy.
 * Logo nieruchomości jest używane w nagłówkach generowanych dokumentów PDF.
 */
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    /** NIP nieruchomości — dokładnie 10 cyfr bez separatorów. */
    @Column(name = "nip", nullable = false, length = 10)
    private String nip;

    @Column(name = "manager_phone", length = 20)
    private String managerPhone;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    /** Ścieżka pliku logo na dysku serwera; nullable — logo jest opcjonalne. */
    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @OneToMany(
            mappedBy = "property",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Building> buildings = new ArrayList<>();

    /**
     * Zwraca unikalny identyfikator nieruchomości.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator nieruchomości.
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
     * Zwraca NIP nieruchomości (10 cyfr).
     *
     * @return NIP
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
     * Zwraca numer telefonu zarządcy nieruchomości.
     *
     * @return numer telefonu lub {@code null} jeśli nie podano
     */
    public String getManagerPhone() {
        return managerPhone;
    }

    /**
     * Ustawia numer telefonu zarządcy nieruchomości.
     *
     * @param managerPhone numer telefonu
     */
    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    /**
     * Zwraca adres e-mail zarządcy nieruchomości.
     *
     * @return adres e-mail lub {@code null} jeśli nie podano
     */
    public String getManagerEmail() {
        return managerEmail;
    }

    /**
     * Ustawia adres e-mail zarządcy nieruchomości.
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
     * Ustawia ścieżkę pliku logo na dysku serwera.
     *
     * @param logoPath ścieżka do pliku logo
     */
    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    /**
     * Zwraca listę budynków przypisanych do tej nieruchomości.
     *
     * @return lista budynków
     */
    public List<Building> getBuildings() {
        return buildings;
    }

    /**
     * Ustawia listę budynków przypisanych do tej nieruchomości.
     *
     * @param buildings lista budynków
     */
    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }
}
