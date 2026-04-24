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

/** Encja reprezentująca dokument przechowywany w systemie. */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_id")
    private Resolution resolution;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Zwraca identyfikator dokumentu.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator dokumentu.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca typ dokumentu (np. PROTOKOL, UCHWALA).
     *
     * @return typ dokumentu
     */
    public String getType() {
        return type;
    }

    /**
     * Ustawia typ dokumentu.
     *
     * @param type typ dokumentu
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Zwraca tytuł dokumentu.
     *
     * @return tytuł
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł dokumentu.
     *
     * @param title tytuł
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca ścieżkę lub URL do pliku.
     *
     * @return ścieżka do pliku
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Ustawia ścieżkę lub URL do pliku.
     *
     * @param fileUrl ścieżka do pliku
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Zwraca przypisanego właściciela dokumentu (użytkownika).
     *
     * @return użytkownik będący właścicielem
     */
    public User getOwnerUser() {
        return ownerUser;
    }

    /**
     * Ustawia przypisanego właściciela dokumentu.
     *
     * @param ownerUser użytkownik
     */
    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    /**
     * Zwraca przypisane mieszkanie, do którego odnosi się dokument.
     *
     * @return mieszkanie
     */
    public Apartment getApartment() {
        return apartment;
    }

    /**
     * Ustawia przypisane mieszkanie.
     *
     * @param apartment mieszkanie
     */
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    /**
     * Zwraca zgłoszenie powiązane z dokumentem.
     *
     * @return zgłoszenie
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Ustawia zgłoszenie powiązane z dokumentem.
     *
     * @param ticket zgłoszenie
     */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Zwraca uchwałę powiązaną z dokumentem.
     *
     * @return uchwała
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * Ustawia uchwałę powiązaną z dokumentem.
     *
     * @param resolution uchwała
     */
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * Zwraca datę i czas utworzenia dokumentu.
     *
     * @return data utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia dokumentu.
     *
     * @param createdAt data utworzenia
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
