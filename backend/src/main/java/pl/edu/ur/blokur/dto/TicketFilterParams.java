package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/**
 * Parametry filtrowania listy zgłoszeń (GET /api/tickets).
 *
 * <p>Wszystkie pola są opcjonalne — przekazanie {@code null} oznacza brak filtra dla danego
 * kryterium.
 */
@Data
public class TicketFilterParams {

    private String status;
    private UUID categoryId;
    private UUID buildingId;
    private UUID staircaseId;
    private UUID assignedTo;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    /** Fraza fulltext przeszukiwana w numerze zgłoszenia, tytule i opisie. */
    private String search;
}
