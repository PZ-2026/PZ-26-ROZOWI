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

/**
 * Encja reprezentująca wejściową uchwałę lub głosowanie do której w aplikacji lokatorzy mają
 * możliwość oddawania głosu.
 */
@Entity
@Table(name = "resolutions")
public class Resolution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Zwraca unikalny identyfikator ucwały.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator uchwały.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca budynek, do którego przypisana jest uchwała.
     *
     * @return instancja encji Building
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * Ustawia budynek dla uchwały.
     *
     * @param building instancja encji Building
     */
    public void setBuilding(Building building) {
        this.building = building;
    }

    /**
     * Zwraca tytuł uchwały.
     *
     * @return Tytuł w postaci String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł uchwały.
     *
     * @param title Tytuł do ustawienia
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca opis i pełną treść głosowania / uchwały.
     *
     * @return Opis w postaci String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ustawia tekst w opisie uchwały.
     *
     * @param description Treść
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Zwraca autora, który stworzył tę uchwałę.
     *
     * @return User będący autorem
     */
    public User getAuthor() {
        return author;
    }

    /**
     * Ustawia użytkownika (najczęściej zarządcę), który stworzył uchwałę.
     *
     * @param author User twórca uchwały
     */
    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Zwraca rygorystyczny termin zakończenia przyjmowania głosów. po upływie którego oddanie głosu
     * będzie niemozliwe.
     *
     * @return Data do której można głosować
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * Ustawia czas, w którym kończy się głosowanie nad tą uchwałą.
     *
     * @param endDate Czas zakończenia ankiety.
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
