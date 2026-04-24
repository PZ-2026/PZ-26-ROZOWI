package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** DTO z danymi odczytu licznika przesyłanymi przez klienta. */
public class MeterReadingRequest {

    @NotNull(message = "Identyfikator licznika jest wymagany")
    private UUID meterId;

    @NotNull(message = "Wartość odczytu jest wymagana")
    @DecimalMin(value = "0.0", inclusive = true, message = "Wartość odczytu nie może być ujemna")
    private BigDecimal value;

    @NotNull(message = "Data odczytu jest wymagana")
    private LocalDate readingDate;

    /** Konstruktor bezargumentowy wymagany przez deserializację Jacksona. */
    public MeterReadingRequest() {}

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

    /**
     * Zwraca identyfikator licznika, z którego pochodzi odczyt.
     *
     * @return identyfikator UUID licznika
     */
    public UUID getMeterId() {
        return meterId;
    }

    /**
     * Ustawia identyfikator licznika, z którego pochodzi odczyt.
     *
     * @param meterId identyfikator UUID licznika
     */
    public void setMeterId(UUID meterId) {
        this.meterId = meterId;
    }

    /**
     * Zwraca wartość odczytu.
     *
     * @return wartość odczytu
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Ustawia wartość odczytu.
     *
     * @param value wartość odczytu
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Zwraca datę odczytu licznika.
     *
     * @return data odczytu
     */
    public LocalDate getReadingDate() {
        return readingDate;
    }

    /**
     * Ustawia datę odczytu licznika.
     *
     * @param readingDate data odczytu
     */
    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }
}
