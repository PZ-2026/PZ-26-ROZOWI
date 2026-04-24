package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO reprezentujące wpis w historii zgłoszenia — zawiera pola ze złączenia
 * tabel ticket_history oraz users (użytkownik zmieniający status).
 */
public class TicketHistoryDto {

    private UUID id;
    private UUID ticketId;
    private String status;
    private String changedByName;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Konstruktor bezargumentowy wymagany przez mechanizmy serializacji.
     */
    public TicketHistoryDto() {
    }

    /**
     * Konstruktor parametryczny tworzący kompletne DTO wpisu historii.
     *
     * @param id            identyfikator wpisu historii
     * @param ticketId      identyfikator zgłoszenia
     * @param status        status zapisany w tym wpisie
     * @param changedByName imię i nazwisko osoby zmieniającej status
     * @param comment       komentarz do zmiany statusu lub null
     * @param createdAt     data i czas zmiany statusu
     */
    public TicketHistoryDto(UUID id, UUID ticketId, String status,
            String changedByName, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.status = status;
        this.changedByName = changedByName;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /**
     * Zwraca identyfikator wpisu historii.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator wpisu historii.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator zgłoszenia powiązanego z wpisem historii.
     *
     * @return identyfikator UUID zgłoszenia
     */
    public UUID getTicketId() {
        return ticketId;
    }

    /**
     * Ustawia identyfikator zgłoszenia powiązanego z wpisem historii.
     *
     * @param ticketId identyfikator UUID zgłoszenia
     */
    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * Zwraca status zapisany w tym wpisie historii.
     *
     * @return status zgłoszenia
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ustawia status zapisany w tym wpisie historii.
     *
     * @param status status zgłoszenia
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Zwraca imię i nazwisko osoby, która zmieniła status.
     *
     * @return imię i nazwisko
     */
    public String getChangedByName() {
        return changedByName;
    }

    /**
     * Ustawia imię i nazwisko osoby zmieniającej status.
     *
     * @param changedByName imię i nazwisko
     */
    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    /**
     * Zwraca komentarz dodany przy zmianie statusu.
     *
     * @return treść komentarza lub null
     */
    public String getComment() {
        return comment;
    }

    /**
     * Ustawia komentarz dodany przy zmianie statusu.
     *
     * @param comment treść komentarza
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Zwraca datę i czas zmiany statusu.
     *
     * @return data i czas zmiany
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas zmiany statusu.
     *
     * @param createdAt data i czas zmiany
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
