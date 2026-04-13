package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO z danymi odczytu licznika przesyłanymi przez klienta.
 */
public class MeterReadingRequest {

    @NotBlank
    private String meterType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Wartość odczytu nie może być ujemna")
    private BigDecimal value;

    private LocalDate readingDate;

    public MeterReadingRequest() {
    }

    public MeterReadingRequest(String meterType, BigDecimal value, LocalDate readingDate) {
        this.meterType = meterType;
        this.value = value;
        this.readingDate = readingDate;
    }

    public String getMeterType() {
        return meterType;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
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
