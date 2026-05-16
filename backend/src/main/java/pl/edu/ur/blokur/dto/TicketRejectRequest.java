package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRejectRequest {

    @NotBlank(message = "Powód odrzucenia jest wymagany")
    private String reason;
}
