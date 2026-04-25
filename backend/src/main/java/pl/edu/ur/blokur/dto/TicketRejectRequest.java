package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketRejectRequest {

    @NotBlank(message = "Powód odrzucenia jest wymagany")
    private String reason;

    public TicketRejectRequest() {}

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
