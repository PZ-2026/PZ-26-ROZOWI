package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.edu.ur.blokur.models.TicketCommentType;

/** DTO służące do tworzenia nowego komentarza pod zgłoszeniem. */
public class TicketCommentRequest {

    @NotBlank(message = "Treść komentarza nie może być pusta")
    private String content;

    @NotNull(message = "Typ komentarza musi zostać określony")
    private TicketCommentType commentType;

    public TicketCommentRequest() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TicketCommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(TicketCommentType commentType) {
        this.commentType = commentType;
    }
}
