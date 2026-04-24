package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Encja reprezentująca ogłoszenie lub komunikat przypisany do konkretnego zasięgu (globalny, dla
 * budynku, klatki lub mieszkania).
 */
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "type", length = 50)
    @ColumnDefault("'OGLOSZENIE'")
    private String type = "OGLOSZENIE";

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_building_id")
    private Building targetBuilding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_staircase_id")
    private Staircase targetStaircase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_apartment_id")
    private Apartment targetApartment;

    @Column(name = "planned_date")
    private LocalDateTime plannedDate;

    @CreationTimestamp
    @Column(name = "created_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * Zwraca unikalny identyfikator ogłoszenia.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator ogłoszenia.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca typ ogłoszenia.
     *
     * @return typ ogłoszenia (np. OGLOSZENIE, KOMUNIKAT)
     */
    public String getType() {
        return type;
    }

    /**
     * Ustawia typ ogłoszenia.
     *
     * @param type typ ogłoszenia
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Zwraca tytuł ogłoszenia.
     *
     * @return tytuł ogłoszenia
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł ogłoszenia.
     *
     * @param title tytuł ogłoszenia
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca treść ogłoszenia.
     *
     * @return treść ogłoszenia
     */
    public String getContent() {
        return content;
    }

    /**
     * Ustawia treść ogłoszenia.
     *
     * @param content treść ogłoszenia
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Zwraca autora ogłoszenia.
     *
     * @return encja użytkownika będącego autorem
     */
    public User getAuthor() {
        return author;
    }

    /**
     * Ustawia autora ogłoszenia.
     *
     * @param author encja użytkownika będącego autorem
     */
    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Zwraca budynek docelowy ogłoszenia.
     *
     * @return encja budynku lub null jeśli zasięg jest szerszy
     */
    public Building getTargetBuilding() {
        return targetBuilding;
    }

    /**
     * Ustawia budynek docelowy ogłoszenia.
     *
     * @param targetBuilding encja budynku docelowego
     */
    public void setTargetBuilding(Building targetBuilding) {
        this.targetBuilding = targetBuilding;
    }

    /**
     * Zwraca klatkę schodową docelową ogłoszenia.
     *
     * @return encja klatki schodowej lub null jeśli zasięg jest szerszy
     */
    public Staircase getTargetStaircase() {
        return targetStaircase;
    }

    /**
     * Ustawia klatkę schodową docelowego ogłoszenia.
     *
     * @param targetStaircase encja klatki schodowej docelowej
     */
    public void setTargetStaircase(Staircase targetStaircase) {
        this.targetStaircase = targetStaircase;
    }

    /**
     * Zwraca lokal docelowy ogłoszenia.
     *
     * @return encja lokalu lub null jeśli zasięg jest szerszy
     */
    public Apartment getTargetApartment() {
        return targetApartment;
    }

    /**
     * Ustawia lokal docelowy ogłoszenia.
     *
     * @param targetApartment encja lokalu docelowego
     */
    public void setTargetApartment(Apartment targetApartment) {
        this.targetApartment = targetApartment;
    }

    /**
     * Zwraca planowaną datę publikacji ogłoszenia.
     *
     * @return data planowanej publikacji lub null jeśli nie określono
     */
    public LocalDateTime getPlannedDate() {
        return plannedDate;
    }

    /**
     * Ustawia planowaną datę publikacji ogłoszenia.
     *
     * @param plannedDate data planowanej publikacji
     */
    public void setPlannedDate(LocalDateTime plannedDate) {
        this.plannedDate = plannedDate;
    }

    /**
     * Zwraca datę i czas automatycznego zapisu rekordu przez Hibernate.
     *
     * @return data i czas utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia ogłoszenia.
     *
     * @param createdAt data i czas utworzenia rekordu
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
