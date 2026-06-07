package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Żądanie dystrybucji zawiadomienia o zmianie stawek opłat. */
public class RateChangeDistributionRequest {

    @NotBlank(message = "Temat zawiadomienia jest wymagany")
    private String subject;

    @NotBlank(message = "Treść zawiadomienia jest wymagana")
    private String body;

    @NotBlank(message = "Data wejścia w życie jest wymagana")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Data musi być w formacie YYYY-MM-DD")
    private String effectiveDate;

    /** Zakres odbiorców: ALL, BUILDING, APARTMENT. */
    private String scope;

    /** UUID budynku lub lokalu (wymagane gdy scope != ALL). */
    private String targetId;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Zwraca treść zawiadomienia o zmianie stawek.
     *
     * @return treść wiadomości
     */
    public String getBody() {
        return body;
    }

    /**
     * Ustawia treść zawiadomienia o zmianie stawek.
     *
     * @param body treść wiadomości
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Zwraca datę wejścia w życie nowych stawek w formacie YYYY-MM-DD.
     *
     * @return data wejścia w życie
     */
    public String getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * Ustawia datę wejścia w życie nowych stawek w formacie YYYY-MM-DD.
     *
     * @param effectiveDate data wejścia w życie
     */
    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
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
