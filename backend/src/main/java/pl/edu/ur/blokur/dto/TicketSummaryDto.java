package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO reprezentujące zgłoszenie na liście — zawiera pola ze złączenia tabel: tickets, users
 * (autor), users (konserwator), ticket_categories oraz apartments/staircases/buildings
 * (lokalizacja).
 */
@Data
@NoArgsConstructor
public class TicketSummaryDto {

    private UUID id;
    private String ticketNumber;
    private String title;
    private String status;
    private String categoryName;
    private String authorName;
    private String assignedToName;
    private String locationLabel;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private boolean slaBreached;

    /**
     * Konstruktor parametryczny tworzący kompletne DTO zgłoszenia.
     *
     * @param id identyfikator zgłoszenia
     * @param ticketNumber unikalny numer zgłoszenia
     * @param title tytuł zgłoszenia
     * @param status aktualny status
     * @param categoryName nazwa kategorii
     * @param authorName imię i nazwisko autora
     * @param assignedToName imię i nazwisko konserwatora lub null
     * @param locationLabel opis lokalizacji (lokal / klatka / budynek)
     * @param createdAt data utworzenia
     * @param closedAt data zamknięcia lub null
     */
    public TicketSummaryDto(
            UUID id,
            String ticketNumber,
            String title,
            String status,
            String categoryName,
            String authorName,
            String assignedToName,
            String locationLabel,
            LocalDateTime createdAt,
            LocalDateTime closedAt) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.status = status;
        this.categoryName = categoryName;
        this.authorName = authorName;
        this.assignedToName = assignedToName;
        this.locationLabel = locationLabel;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }
}
