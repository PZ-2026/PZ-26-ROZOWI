package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Żądanie wygenerowania i dystrybucji rocznych rozliczeń kosztów lokali. */
public class AnnualSettlementDistributionRequest {

    @NotNull(message = "Rok rozliczeniowy jest wymagany")
    @Min(value = 2000, message = "Rok musi być >= 2000")
    @Max(value = 2100, message = "Rok musi być <= 2100")
    private Integer year;

    private String note;

    /** Zakres odbiorców: ALL, BUILDING, APARTMENT. */
    private String scope;

    /** UUID budynku lub lokalu (wymagane gdy scope != ALL). */
    private String targetId;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * Zwraca opcjonalną notatkę dołączaną do rozliczenia.
     *
     * @return treść notatki lub {@code null}
     */
    public String getNote() {
        return note;
    }

    /**
     * Ustawia opcjonalną notatkę dołączaną do rozliczenia.
     *
     * @param note treść notatki
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Zwraca zakres odbiorców (ALL, BUILDING lub APARTMENT).
     *
     * @return zakres dystrybucji
     */
    public String getScope() {
        return scope;
    }

    /**
     * Ustawia zakres odbiorców (ALL, BUILDING lub APARTMENT).
     *
     * @param scope zakres dystrybucji
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Zwraca UUID budynku lub lokalu docelowego (wymagane gdy scope != ALL).
     *
     * @return identyfikator docelowego zasobu
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Ustawia UUID budynku lub lokalu docelowego (wymagane gdy scope != ALL).
     *
     * @param targetId identyfikator docelowego zasobu
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
