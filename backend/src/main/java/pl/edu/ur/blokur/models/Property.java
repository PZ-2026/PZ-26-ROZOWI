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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * Encja reprezentująca nieruchomość (wspólnotę mieszkaniową) — najwyższy poziom hierarchii danych.
 * Nieruchomość grupuje budynki i przechowuje dane identyfikacyjne (NIP) oraz kontaktowe zarządcy.
 * Logo nieruchomości jest używane w nagłówkach generowanych dokumentów PDF.
 */
@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
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
}
