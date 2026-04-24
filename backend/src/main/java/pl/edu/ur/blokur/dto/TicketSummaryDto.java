package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO reprezentujące zgłoszenie na liście — zawiera pola ze złączenia tabel:
 * tickets, users (autor), users (konserwator), ticket_categories
 * oraz apartments/staircases/buildings (lokalizacja).
 */
public class TicketSummaryDto {

    private UUID id;
    private String ticketNumber;
    private String title;
    private String status;
    private String categoryName;
    private String authorName;
    private String assignedToName;
    private String locationLabel;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    /**
     * Konstruktor bezargumentowy wymagany przez mechanizmy serializacji.
     */
    public TicketSummaryDto() {
    }

    /**
     * Konstruktor parametryczny tworzący kompletne DTO zgłoszenia.
     *
     * @param id             identyfikator zgłoszenia
     * @param ticketNumber   unikalny numer zgłoszenia
     * @param title          tytuł zgłoszenia
     * @param status         aktualny status
     * @param categoryName   nazwa kategorii
     * @param authorName     imię i nazwisko autora
     * @param assignedToName imię i nazwisko konserwatora lub null
     * @param locationLabel  opis lokalizacji (lokal / klatka / budynek)
     * @param createdAt      data utworzenia
     * @param closedAt       data zamknięcia lub null
     */
    public TicketSummaryDto(UUID id, String ticketNumber, String title,
            String status, String categoryName, String authorName,
            String assignedToName, String locationLabel,
            LocalDateTime createdAt, LocalDateTime closedAt) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.status = status;
        this.categoryName = categoryName;
        this.authorName = authorName;
        this.assignedToName = assignedToName;
        this.locationLabel = locationLabel;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

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
     * @return numer zgłoszenia
     */
    public String getTicketNumber() {
        return ticketNumber;
    }

    /**
     * Ustawia numer zgłoszenia.
     *
     * @param ticketNumber numer zgłoszenia
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
     * Zwraca aktualny status zgłoszenia.
     *
     * @return status zgłoszenia
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ustawia status zgłoszenia.
     *
     * @param status status zgłoszenia
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
     * Zwraca imię i nazwisko przypisanego konserwatora.
     *
     * @return imię i nazwisko konserwatora lub null jeśli nieprzypisany
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
     * Zwraca opis lokalizacji zgłoszenia (lokal, klatka lub budynek).
     *
     * @return opis lokalizacji
     */
    public String getLocationLabel() {
        return locationLabel;
    }

    /**
     * Ustawia opis lokalizacji zgłoszenia.
     *
     * @param locationLabel opis lokalizacji
     */
    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
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
     * Zwraca datę i czas zamknięcia zgłoszenia.
     *
     * @return data zamknięcia lub null
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
