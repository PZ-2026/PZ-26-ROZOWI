package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** DTO z danymi przypisania konserwatora do zgłoszenia serwisowego. */
@Data
public class TicketAssignRequest {

    @NotNull(message = "ID konserwatora jest wymagane")
    private UUID assignedTo;

    @NotNull(message = "Data planowanej wizyty jest wymagana")
    private LocalDateTime plannedVisitAt;

    private String internalNote;
}
