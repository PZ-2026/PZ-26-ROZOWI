package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.edu.ur.blokur.models.TicketCommentType;

/** DTO służące do tworzenia nowego komentarza pod zgłoszeniem. */
@Data
public class TicketCommentRequest {

    @NotBlank(message = "Treść komentarza nie może być pusta")
    private String content;

    @NotNull(message = "Typ komentarza musi zostać określony")
    private TicketCommentType commentType;
}
