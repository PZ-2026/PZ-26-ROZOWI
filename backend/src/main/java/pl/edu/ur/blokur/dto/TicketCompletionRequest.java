package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO do żądania zakończenia prac przez konserwatora. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCompletionRequest {

    @NotBlank(message = "Opis prac jest wymagany")
    @Size(max = 1000, message = "Opis prac nie może przekraczać 1000 znaków")
    private String workDescription;
}
