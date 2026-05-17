package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/**
 * DTO reprezentujące komentarz do zgłoszenia — zawiera pola ze złączenia tabel ticket_comments oraz
 * users (autor komentarza).
 */
@Data
public class TicketCommentDto {

    private UUID id;
    private UUID ticketId;
    private String authorName;
    private String content;
    private pl.edu.ur.blokur.models.TicketCommentType commentType;
    private LocalDateTime createdAt;

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
            UUID id,
            UUID ticketId,
            String authorName,
            String content,
            pl.edu.ur.blokur.models.TicketCommentType commentType,
            LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.authorName = authorName;
        this.content = content;
        this.commentType = commentType;
        this.createdAt = createdAt;
    }
}
