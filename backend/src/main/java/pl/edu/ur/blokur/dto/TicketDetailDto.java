package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ze szczegółami zgłoszenia (GET /api/tickets/{id}). Zawiera pełne dane zgłoszenia, w tym pola
 * widoczne tylko dla zarządcy i konserwatora.
 */
public class TicketDetailDto {

    private UUID id;
    private String ticketNumber;
    private String title;
    private String description;
    private String status;
    private String categoryName;
    private UUID categoryId;
    private String authorName;
    private UUID authorId;
    private String assignedToName;
    private UUID assignedToId;
    private String locationLabel;
    private UUID apartmentId;
    private LocalDateTime plannedVisitAt;
    private String internalNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    /** Konstruktor bezargumentowy wymagany przez mechanizmy serializacji. */
    public TicketDetailDto() {}

    /**
     * Zwraca identyfikator zgłoszenia.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator zgłoszenia.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca numer zgłoszenia.
     *
     * @return numer w formacie ZGL-RRRR-NNNN
     */
    public String getTicketNumber() {
        return ticketNumber;
    }

    /**
     * Ustawia numer zgłoszenia.
     *
     * @param ticketNumber numer w formacie ZGL-RRRR-NNNN
     */
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    /**
     * Zwraca tytuł zgłoszenia.
     *
     * @return tytuł zgłoszenia
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł zgłoszenia.
     *
     * @param title tytuł zgłoszenia
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca opis zgłoszenia.
     *
     * @return opis zgłoszenia
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ustawia opis zgłoszenia.
     *
     * @param description opis zgłoszenia
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Zwraca aktualny status zgłoszenia.
     *
     * @return nazwa statusu
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ustawia status zgłoszenia.
     *
     * @param status nazwa statusu
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Zwraca nazwę kategorii zgłoszenia.
     *
     * @return nazwa kategorii
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Ustawia nazwę kategorii zgłoszenia.
     *
     * @param categoryName nazwa kategorii
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Zwraca identyfikator kategorii zgłoszenia.
     *
     * @return UUID kategorii
     */
    public UUID getCategoryId() {
        return categoryId;
    }

    /**
     * Ustawia identyfikator kategorii zgłoszenia.
     *
     * @param categoryId UUID kategorii
     */
    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Zwraca imię i nazwisko autora zgłoszenia.
     *
     * @return imię i nazwisko autora
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Ustawia imię i nazwisko autora zgłoszenia.
     *
     * @param authorName imię i nazwisko autora
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Zwraca identyfikator autora zgłoszenia.
     *
     * @return UUID autora
     */
    public UUID getAuthorId() {
        return authorId;
    }

    /**
     * Ustawia identyfikator autora zgłoszenia.
     *
     * @param authorId UUID autora
     */
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    /**
     * Zwraca imię i nazwisko przypisanego konserwatora.
     *
     * @return imię i nazwisko konserwatora lub {@code null}
     */
    public String getAssignedToName() {
        return assignedToName;
    }

    /**
     * Ustawia imię i nazwisko przypisanego konserwatora.
     *
     * @param assignedToName imię i nazwisko konserwatora
     */
    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    /**
     * Zwraca identyfikator przypisanego konserwatora.
     *
     * @return UUID konserwatora lub {@code null}
     */
    public UUID getAssignedToId() {
        return assignedToId;
    }

    /**
     * Ustawia identyfikator przypisanego konserwatora.
     *
     * @param assignedToId UUID konserwatora
     */
    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    /**
     * Zwraca etykietę lokalizacji zgłoszenia (numer lokalu, klatka lub budynek).
     *
     * @return opis lokalizacji
     */
    public String getLocationLabel() {
        return locationLabel;
    }

    /**
     * Ustawia etykietę lokalizacji zgłoszenia.
     *
     * @param locationLabel opis lokalizacji
     */
    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
    }

    /**
     * Zwraca identyfikator lokalu powiązanego ze zgłoszeniem.
     *
     * @return UUID lokalu lub {@code null}
     */
    public UUID getApartmentId() {
        return apartmentId;
    }

    /**
     * Ustawia identyfikator lokalu powiązanego ze zgłoszeniem.
     *
     * @param apartmentId UUID lokalu
     */
    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }

    /**
     * Zwraca planowaną datę wizyty.
     *
     * @return data planowanej wizyty lub {@code null}
     */
    public LocalDateTime getPlannedVisitAt() {
        return plannedVisitAt;
    }

    /**
     * Ustawia planowaną datę wizyty.
     *
     * @param plannedVisitAt data planowanej wizyty
     */
    public void setPlannedVisitAt(LocalDateTime plannedVisitAt) {
        this.plannedVisitAt = plannedVisitAt;
    }

    /**
     * Zwraca wewnętrzną notatkę do zgłoszenia (widoczna tylko dla zarządcy i konserwatora).
     *
     * @return treść notatki lub {@code null}
     */
    public String getInternalNote() {
        return internalNote;
    }

    /**
     * Ustawia wewnętrzną notatkę do zgłoszenia.
     *
     * @param internalNote treść notatki wewnętrznej
     */
    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }

    /**
     * Zwraca datę i czas utworzenia zgłoszenia.
     *
     * @return data utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia zgłoszenia.
     *
     * @param createdAt data utworzenia
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Zwraca datę i czas ostatniej modyfikacji zgłoszenia.
     *
     * @return data ostatniej aktualizacji lub {@code null}
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Ustawia datę i czas ostatniej modyfikacji zgłoszenia.
     *
     * @param updatedAt data ostatniej aktualizacji
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Zwraca datę i czas zamknięcia zgłoszenia.
     *
     * @return data zamknięcia lub {@code null}
     */
    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    /**
     * Ustawia datę i czas zamknięcia zgłoszenia.
     *
     * @param closedAt data zamknięcia
     */
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
