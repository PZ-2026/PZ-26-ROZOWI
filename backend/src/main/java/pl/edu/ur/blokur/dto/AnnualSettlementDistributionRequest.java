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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
