package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO do żądania zakończenia prac przez konserwatora. */
public class TicketCompletionRequest {

    @NotBlank(message = "Opis prac jest wymagany")
    @Size(max = 1000, message = "Opis prac nie może przekraczać 1000 znaków")
    private String workDescription;

    public TicketCompletionRequest() {}

    public TicketCompletionRequest(String workDescription) {
        this.workDescription = workDescription;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }
}
