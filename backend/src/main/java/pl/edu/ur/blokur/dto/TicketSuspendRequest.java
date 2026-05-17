package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO do żądania wstrzymania zgłoszenia. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSuspendRequest {

    @NotBlank(message = "Powód wstrzymania jest wymagany")
    private String reason;
}
