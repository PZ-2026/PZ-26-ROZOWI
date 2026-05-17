package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import pl.edu.ur.blokur.models.MediumType;

/** DTO z danymi odczytu licznika zwracanymi przez API. */
@Data
public class MeterReadingResponse {

    private UUID id;
    private UUID apartmentId;
    private UUID meterId;
    private String meterSerialNumber;
    private MediumType mediumType;
    private BigDecimal value;
    private LocalDate readingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String recordedBy;

    /**
     * Tworzy odpowiedź z kompletem danych odczytu licznika.
     *
     * @param id identyfikator odczytu
     * @param apartmentId identyfikator lokalu, którego dotyczy odczyt
     * @param meterId identyfikator licznika
     * @param meterSerialNumber numer seryjny licznika
     * @param mediumType typ medium mierzonego przez licznik
     * @param value wartość odczytu
     * @param readingDate data odczytu
     * @param createdAt data i czas utworzenia rekordu
     * @param updatedAt data i czas ostatniej modyfikacji
     * @param recordedBy identyfikator użytkownika, który zapisał odczyt
     */
    public MeterReadingResponse(
            UUID id,
            UUID apartmentId,
            UUID meterId,
            String meterSerialNumber,
            MediumType mediumType,
            BigDecimal value,
            LocalDate readingDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String recordedBy) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.meterId = meterId;
        this.meterSerialNumber = meterSerialNumber;
        this.mediumType = mediumType;
        this.value = value;
        this.readingDate = readingDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.recordedBy = recordedBy;
    }
}
