package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Parametry filtrowania listy zgłoszeń (GET /api/tickets).
 *
 * <p>Wszystkie pola są opcjonalne — przekazanie {@code null} oznacza brak filtra dla danego
 * kryterium.
 */
public class TicketFilterParams {

    private String status;
    private UUID categoryId;
    private UUID buildingId;
    private UUID staircaseId;
    private UUID assignedTo;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    /** Fraza fulltext przeszukiwana w numerze zgłoszenia, tytule i opisie. */
    private String search;

    /** Konstruktor bezargumentowy wymagany przez mechanizmy deserializacji. */
    public TicketFilterParams() {}

    /**
     * Zwraca filtr statusu zgłoszenia.
     *
     * @return nazwa statusu lub {@code null}
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ustawia filtr statusu zgłoszenia.
     *
     * @param status nazwa statusu
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Zwraca filtr kategorii zgłoszenia.
     *
     * @return UUID kategorii lub {@code null}
     */
    public UUID getCategoryId() {
        return categoryId;
    }

    /**
     * Ustawia filtr kategorii zgłoszenia.
     *
     * @param categoryId UUID kategorii
     */
    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Zwraca filtr budynku.
     *
     * @return UUID budynku lub {@code null}
     */
    public UUID getBuildingId() {
        return buildingId;
    }

    /**
     * Ustawia filtr budynku — zwraca zgłoszenia dotyczące tego budynku i jego klatek/lokali.
     *
     * @param buildingId UUID budynku
     */
    public void setBuildingId(UUID buildingId) {
        this.buildingId = buildingId;
    }

    /**
     * Zwraca filtr klatki schodowej.
     *
     * @return UUID klatki lub {@code null}
     */
    public UUID getStaircaseId() {
        return staircaseId;
    }

    /**
     * Ustawia filtr klatki schodowej.
     *
     * @param staircaseId UUID klatki schodowej
     */
    public void setStaircaseId(UUID staircaseId) {
        this.staircaseId = staircaseId;
    }

    /**
     * Zwraca filtr konserwatora.
     *
     * @return UUID konserwatora lub {@code null}
     */
    public UUID getAssignedTo() {
        return assignedTo;
    }

    /**
     * Ustawia filtr konserwatora — zwraca zgłoszenia przypisane do podanego użytkownika.
     *
     * @param assignedTo UUID konserwatora
     */
    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Zwraca dolną granicę daty utworzenia zgłoszenia.
     *
     * @return data od lub {@code null}
     */
    public LocalDateTime getDateFrom() {
        return dateFrom;
    }

    /**
     * Ustawia dolną granicę daty utworzenia zgłoszenia.
     *
     * @param dateFrom data od
     */
    public void setDateFrom(LocalDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * Zwraca górną granicę daty utworzenia zgłoszenia.
     *
     * @return data do lub {@code null}
     */
    public LocalDateTime getDateTo() {
        return dateTo;
    }

    /**
     * Ustawia górną granicę daty utworzenia zgłoszenia.
     *
     * @param dateTo data do
     */
    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * Zwraca frazę wyszukiwania fulltext.
     *
     * @return fraza wyszukiwania lub {@code null}
     */
    public String getSearch() {
        return search;
    }

    /**
     * Ustawia frazę wyszukiwania fulltext (przeszukuje numer, tytuł i opis).
     *
     * @param search fraza wyszukiwania
     */
    public void setSearch(String search) {
        this.search = search;
    }
}
