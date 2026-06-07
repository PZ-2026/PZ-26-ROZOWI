package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/**
 * DTO ze szczegółami zgłoszenia (GET /api/tickets/{id}). Zawiera pełne dane zgłoszenia, w tym pola
 * widoczne tylko dla zarządcy i konserwatora.
 */
@Data
public class TicketDetailDto {

    private UUID id;
    private String ticketNumber;
    private String title;
    private String description;
    private String status;
    private String categoryName;
    private UUID categoryId;
    private String authorName;
    private UUID authorId;
    private String assignedToName;
    private UUID assignedToId;
    private String locationLabel;
    private UUID apartmentId;
    private LocalDateTime plannedVisitAt;
    private String internalNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
