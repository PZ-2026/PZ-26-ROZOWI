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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encja reprezentująca zgłoszenie usterki lub awarii w systemie Blokur.
 */
@Entity
@Table(name = "tickets")
@SQLDelete(sql = "UPDATE tickets SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "ticket_number", unique = true, nullable = false, length = 100)
    private String ticketNumber;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ColumnDefault("'NOWE'")
    @Column(name = "status", length = 50)
    private String status = "NOWE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TicketCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staircase_id")
    private Staircase staircase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketComment> comments = new ArrayList<>();

    /**
     * Zwraca identyfikator zgłoszenia.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator zgłoszenia.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca unikalny numer zgłoszenia widoczny dla użytkownika.
     *
     * @return numer zgłoszenia
     */
    public String getTicketNumber() {
        return ticketNumber;
    }

    /**
     * Ustawia unikalny numer zgłoszenia.
     *
     * @param ticketNumber numer zgłoszenia
     */
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    /**
     * Zwraca tytuł zgłoszenia.
     *
     * @return tytuł zgłoszenia
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł zgłoszenia.
     *
     * @param title tytuł zgłoszenia
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca szczegółowy opis zgłoszenia.
     *
     * @return opis zgłoszenia
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ustawia opis zgłoszenia.
     *
     * @param description opis zgłoszenia
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Zwraca aktualny status zgłoszenia.
     *
     * @return status jako String (np. NOWE, W_REALIZACJI)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ustawia status zgłoszenia.
     *
     * @param status status zgłoszenia
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Zwraca kategorię przypisaną do zgłoszenia.
     *
     * @return instancja TicketCategory
     */
    public TicketCategory getCategory() {
        return category;
    }

    /**
     * Ustawia kategorię zgłoszenia.
     *
     * @param category instancja TicketCategory
     */
    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    /**
     * Zwraca użytkownika, który utworzył zgłoszenie.
     *
     * @return instancja User
     */
    public User getAuthor() {
        return author;
    }

    /**
     * Ustawia autora zgłoszenia.
     *
     * @param author instancja User
     */
    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Zwraca konserwatora przypisanego do realizacji zgłoszenia.
     *
     * @return instancja User lub null jeśli nieprzypisane
     */
    public User getAssignedTo() {
        return assignedTo;
    }

    /**
     * Przypisuje konserwatora do realizacji zgłoszenia.
     *
     * @param assignedTo instancja User
     */
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Zwraca mieszkanie powiązane ze zgłoszeniem.
     *
     * @return instancja Apartment lub null
     */
    public Apartment getApartment() {
        return apartment;
    }

    /**
     * Ustawia mieszkanie powiązane ze zgłoszeniem.
     *
     * @param apartment instancja Apartment
     */
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    /**
     * Zwraca klatkę schodową powiązaną ze zgłoszeniem.
     *
     * @return instancja Staircase lub null
     */
    public Staircase getStaircase() {
        return staircase;
    }

    /**
     * Ustawia klatkę schodową powiązaną ze zgłoszeniem.
     *
     * @param staircase instancja Staircase
     */
    public void setStaircase(Staircase staircase) {
        this.staircase = staircase;
    }

    /**
     * Zwraca budynek powiązany ze zgłoszeniem.
     *
     * @return instancja Building lub null
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * Ustawia budynek powiązany ze zgłoszeniem.
     *
     * @param building instancja Building
     */
    public void setBuilding(Building building) {
        this.building = building;
    }

    /**
     * Zwraca flagę soft delete zgłoszenia.
     *
     * @return true jeśli zgłoszenie jest usunięte
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Ustawia flagę soft delete zgłoszenia.
     *
     * @param deleted wartość flagi
     */
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    /**
     * Zwraca datę i czas utworzenia zgłoszenia.
     *
     * @return data utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia zgłoszenia.
     *
     * @param createdAt data utworzenia
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Zwraca datę i czas zamknięcia zgłoszenia.
     *
     * @return data zamknięcia lub null jeśli zgłoszenie jest otwarte
     */
    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    /**
     * Ustawia datę i czas zamknięcia zgłoszenia.
     *
     * @param closedAt data zamknięcia
     */
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    /**
     * Zwraca historię zmian statusu zgłoszenia.
     *
     * @return lista wpisów historii
     */
    public List<TicketHistory> getHistory() {
        return history;
    }

    /**
     * Ustawia historię zmian statusu zgłoszenia.
     *
     * @param history lista wpisów historii
     */
    public void setHistory(List<TicketHistory> history) {
        this.history = history;
    }

    /**
     * Zwraca listę mediów (zdjęć) dołączonych do zgłoszenia.
     *
     * @return lista mediów
     */
    public List<TicketMedia> getMedia() {
        return media;
    }

    /**
     * Ustawia listę mediów dołączonych do zgłoszenia.
     *
     * @param media lista mediów
     */
    public void setMedia(List<TicketMedia> media) {
        this.media = media;
    }

    /**
     * Zwraca listę komentarzy dodanych do zgłoszenia.
     *
     * @return lista komentarzy
     */
    public List<TicketComment> getComments() {
        return comments;
    }

    /**
     * Ustawia listę komentarzy do zgłoszenia.
     *
     * @param comments lista komentarzy
     */
    public void setComments(List<TicketComment> comments) {
        this.comments = comments;
    }
}
