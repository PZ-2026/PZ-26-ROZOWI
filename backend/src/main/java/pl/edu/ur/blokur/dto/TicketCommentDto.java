package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO reprezentujące komentarz do zgłoszenia — zawiera pola ze złączenia tabel ticket_comments oraz
 * users (autor komentarza).
 */
public class TicketCommentDto {

    private UUID id;
    private UUID ticketId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    /** Konstruktor bezargumentowy wymagany przez mechanizmy serializacji. */
    public TicketCommentDto() {}

    /**
     * Konstruktor parametryczny tworzący kompletne DTO komentarza.
     *
     * @param id identyfikator komentarza
     * @param ticketId identyfikator zgłoszenia
     * @param authorName imię i nazwisko autora komentarza
     * @param content treść komentarza
     * @param createdAt data i czas dodania komentarza
     */
    public TicketCommentDto(
            UUID id, UUID ticketId, String authorName, String content, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

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
     * Zwraca identyfikator zgłoszenia powiązanego z komentarzem.
     *
     * @return identyfikator UUID zgłoszenia
     */
    public UUID getTicketId() {
        return ticketId;
    }

    /**
     * Ustawia identyfikator zgłoszenia powiązanego z komentarzem.
     *
     * @param ticketId identyfikator UUID zgłoszenia
     */
    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * Zwraca imię i nazwisko autora komentarza.
     *
     * @return imię i nazwisko autora
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Ustawia imię i nazwisko autora komentarza.
     *
     * @param authorName imię i nazwisko autora
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
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
     * Zwraca datę i czas dodania komentarza.
     *
     * @return data i czas dodania
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas dodania komentarza.
     *
     * @param createdAt data i czas dodania
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
