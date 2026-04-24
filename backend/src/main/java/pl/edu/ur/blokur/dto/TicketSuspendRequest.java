package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO do żądania wstrzymania zgłoszenia. */
public class TicketSuspendRequest {

    @NotBlank(message = "Powód wstrzymania jest wymagany")
    private String reason;

    public TicketSuspendRequest() {}

    public TicketSuspendRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
