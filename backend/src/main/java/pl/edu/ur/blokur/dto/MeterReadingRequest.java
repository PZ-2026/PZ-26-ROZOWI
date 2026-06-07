package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/** DTO z danymi odczytu licznika przesyłanymi przez klienta. */
@Data
public class MeterReadingRequest {

    @NotNull(message = "Identyfikator licznika jest wymagany")
    private UUID meterId;

    @NotNull(message = "Wartość odczytu jest wymagana")
    @DecimalMin(value = "0.0", inclusive = true, message = "Wartość odczytu nie może być ujemna")
    private BigDecimal value;

    @NotNull(message = "Data odczytu jest wymagana")
    private LocalDate readingDate;

    /**
     * Tworzy żądanie odczytu licznika z podanymi danymi.
     *
     * @param meterId identyfikator licznika, z którego pochodzi odczyt
     * @param value wartość odczytu
     * @param readingDate data odczytu
     */
    public MeterReadingRequest(UUID meterId, BigDecimal value, LocalDate readingDate) {
        this.meterId = meterId;
        this.value = value;
        this.readingDate = readingDate;
    }
}
