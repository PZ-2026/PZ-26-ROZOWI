package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/**
 * DTO reprezentujące wpis w historii zgłoszenia — zawiera pola ze złączenia tabel ticket_history
 * oraz users (użytkownik zmieniający status).
 */
@Data
public class TicketHistoryDto {

    private UUID id;
    private UUID ticketId;
    private String status;
    private String changedByName;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Konstruktor parametryczny tworzący kompletne DTO wpisu historii.
     *
     * @param id identyfikator wpisu historii
     * @param ticketId identyfikator zgłoszenia
     * @param status status zapisany w tym wpisie
     * @param changedByName imię i nazwisko osoby zmieniającej status
     * @param comment komentarz do zmiany statusu lub null
     * @param createdAt data i czas zmiany statusu
     */
    public TicketHistoryDto(
            UUID id,
            UUID ticketId,
            String status,
            String changedByName,
            String comment,
            LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.status = status;
        this.changedByName = changedByName;
        this.comment = comment;
        this.createdAt = createdAt;
    }
}
