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

/** Encja reprezentująca plik multimedialny (zdjęcie, film) dołączony do zgłoszenia. */
@Entity
@Table(name = "ticket_media")
public class TicketMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @Column(name = "media_type", nullable = false, length = 50)
    private String mediaType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Zwraca identyfikator pliku multimedialnego.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator pliku multimedialnego.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca zgłoszenie, do którego należy ten plik.
     *
     * @return instancja Ticket
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Ustawia zgłoszenie powiązane z plikiem multimedialnym.
     *
     * @param ticket instancja Ticket
     */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Zwraca URL do pliku multimedialnego.
     *
     * @return URL pliku
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Ustawia URL do pliku multimedialnego.
     *
     * @param fileUrl URL pliku
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Zwraca typ pliku multimedialnego (np. IMAGE, VIDEO).
     *
     * @return typ mediów
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Ustawia typ pliku multimedialnego.
     *
     * @param mediaType typ mediów
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Zwraca datę i czas przesłania pliku.
     *
     * @return data i czas przesłania
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas przesłania pliku.
     *
     * @param createdAt data i czas
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
