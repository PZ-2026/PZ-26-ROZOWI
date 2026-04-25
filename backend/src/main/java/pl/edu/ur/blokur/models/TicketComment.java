package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Encja reprezentująca komentarz dodany do zgłoszenia przez użytkownika systemu. */
@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false, length = 20)
    private TicketCommentType commentType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Zwraca identyfikator komentarza.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator komentarza.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca zgłoszenie, do którego należy ten komentarz.
     *
     * @return instancja Ticket
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Ustawia zgłoszenie powiązane z komentarzem.
     *
     * @param ticket instancja Ticket
     */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Zwraca autora komentarza.
     *
     * @return instancja User
     */
    public User getAuthor() {
        return author;
    }

    /**
     * Ustawia autora komentarza.
     *
     * @param author instancja User
     */
    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Zwraca treść komentarza.
     *
     * @return treść komentarza
     */
    public String getContent() {
        return content;
    }

    /**
     * Ustawia treść komentarza.
     *
     * @param content treść komentarza
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Zwraca typ komentarza.
     *
     * @return typ komentarza (PUBLICZNY/WEWNETRZNY)
     */
    public TicketCommentType getCommentType() {
        return commentType;
    }

    /**
     * Ustawia typ komentarza.
     *
     * @param commentType typ komentarza
     */
    public void setCommentType(TicketCommentType commentType) {
        this.commentType = commentType;
    }

    /**
     * Zwraca datę i czas dodania komentarza.
     *
     * @return data i czas utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas dodania komentarza.
     *
     * @param createdAt data i czas
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
