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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
