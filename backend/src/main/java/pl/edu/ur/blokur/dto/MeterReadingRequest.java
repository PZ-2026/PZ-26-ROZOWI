package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO z danymi odczytu licznika przesyłanymi przez klienta.
 */
public class MeterReadingRequest {

    @NotNull(message = "Identyfikator licznika jest wymagany")
    private UUID meterId;

    @NotNull(message = "Wartość odczytu jest wymagana")
    @DecimalMin(value = "0.0", inclusive = true, message = "Wartość odczytu nie może być ujemna")
    private BigDecimal value;

    @NotNull(message = "Data odczytu jest wymagana")
    private LocalDate readingDate;

    public MeterReadingRequest() {
    }

    public MeterReadingRequest(UUID meterId, BigDecimal value, LocalDate readingDate) {
        this.meterId = meterId;
        this.value = value;
        this.readingDate = readingDate;
    }

    public UUID getMeterId() {
        return meterId;
    }

    public void setMeterId(UUID meterId) {
        this.meterId = meterId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDate getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }
}
