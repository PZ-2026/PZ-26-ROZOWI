package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotNull;
import pl.edu.ur.blokur.models.TicketStatus;

/** DTO z żądaniem zmiany statusu zgłoszenia przez konserwatora lub zarządcę. */
public class TicketStatusChangeRequest {

    @NotNull(message = "Status nie może być pusty")
    private TicketStatus status;

    private String comment;

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public TicketStatusChangeRequest() {}

    /**
     * Zwraca docelowy status zgłoszenia.
     *
     * @return wartość enum TicketStatus
     */
    public TicketStatus getStatus() {
        return status;
    }

    /**
     * Ustawia docelowy status zgłoszenia.
     *
     * @param status wartość enum TicketStatus
     */
    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    /**
     * Zwraca opcjonalny komentarz do zmiany statusu.
     *
     * @return treść komentarza lub null
     */
    public String getComment() {
        return comment;
    }

    /**
     * Ustawia opcjonalny komentarz do zmiany statusu.
     *
     * @param comment treść komentarza
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
