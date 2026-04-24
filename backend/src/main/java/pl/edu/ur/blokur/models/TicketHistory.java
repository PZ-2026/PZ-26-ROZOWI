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

/** Encja reprezentująca pojedynczy wpis w historii zmian statusu zgłoszenia. */
@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Zwraca identyfikator wpisu historii.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator wpisu historii.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca zgłoszenie, do którego należy ten wpis historii.
     *
     * @return instancja Ticket
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Ustawia zgłoszenie powiązane z wpisem historii.
     *
     * @param ticket instancja Ticket
     */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Zwraca status zgłoszenia zapisany w tym wpisie historii.
     *
     * @return status jako String
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ustawia status zapisywany w tym wpisie historii.
     *
     * @param status status zgłoszenia
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Zwraca użytkownika, który dokonał zmiany statusu.
     *
     * @return instancja User
     */
    public User getChangedBy() {
        return changedBy;
    }

    /**
     * Ustawia użytkownika odpowiedzialnego za zmianę statusu.
     *
     * @param changedBy instancja User
     */
    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    /**
     * Zwraca komentarz dołączony do zmiany statusu.
     *
     * @return treść komentarza lub null
     */
    public String getComment() {
        return comment;
    }

    /**
     * Ustawia komentarz do zmiany statusu.
     *
     * @param comment treść komentarza
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Zwraca datę i czas utworzenia wpisu historii.
     *
     * @return data i czas utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia wpisu historii.
     *
     * @param createdAt data i czas
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
