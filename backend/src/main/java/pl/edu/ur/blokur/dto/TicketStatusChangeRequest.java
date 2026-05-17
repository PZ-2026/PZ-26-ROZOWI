package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.edu.ur.blokur.models.TicketStatus;

/** DTO z żądaniem zmiany statusu zgłoszenia przez konserwatora lub zarządcę. */
@Data
public class TicketStatusChangeRequest {

    @NotNull(message = "Status nie może być pusty")
    private TicketStatus status;

    private String comment;
}
