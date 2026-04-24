package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class TicketAssignRequest {

    @NotNull(message = "ID konserwatora jest wymagane")
    private UUID assignedTo;

    @NotNull(message = "Data planowanej wizyty jest wymagana")
    private LocalDateTime plannedVisitAt;

    private String internalNote;

    public TicketAssignRequest() {}

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getPlannedVisitAt() {
        return plannedVisitAt;
    }

    public void setPlannedVisitAt(LocalDateTime plannedVisitAt) {
        this.plannedVisitAt = plannedVisitAt;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }
}
