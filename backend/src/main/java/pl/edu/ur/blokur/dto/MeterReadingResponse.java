package pl.edu.ur.blokur.dto;

import pl.edu.ur.blokur.models.MediumType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTO z danymi odczytu licznika zwracanymi przez API. */
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

    /**
     * Zwraca unikalny identyfikator odczytu.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator odczytu.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator lokalu, którego dotyczy odczyt.
     *
     * @return identyfikator UUID lokalu
     */
    public UUID getApartmentId() {
        return apartmentId;
    }

    /**
     * Ustawia identyfikator lokalu, którego dotyczy odczyt.
     *
     * @param apartmentId identyfikator UUID lokalu
     */
    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
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
     * Zwraca numer seryjny licznika.
     *
     * @return numer seryjny
     */
    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    /**
     * Ustawia numer seryjny licznika.
     *
     * @param meterSerialNumber numer seryjny
     */
    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    /**
     * Zwraca typ medium mierzonego przez licznik (np. zimna woda, energia elektryczna).
     *
     * @return typ medium
     */
    public MediumType getMediumType() {
        return mediumType;
    }

    /**
     * Ustawia typ medium mierzonego przez licznik.
     *
     * @param mediumType typ medium
     */
    public void setMediumType(MediumType mediumType) {
        this.mediumType = mediumType;
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

    /**
     * Zwraca datę i czas utworzenia rekordu.
     *
     * @return data i czas utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia rekordu.
     *
     * @param createdAt data i czas utworzenia
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Zwraca datę i czas ostatniej modyfikacji rekordu.
     *
     * @return data i czas ostatniej modyfikacji
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Ustawia datę i czas ostatniej modyfikacji rekordu.
     *
     * @param updatedAt data i czas ostatniej modyfikacji
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Zwraca identyfikator użytkownika, który zapisał odczyt.
     *
     * @return identyfikator użytkownika
     */
    public String getRecordedBy() {
        return recordedBy;
    }

    /**
     * Ustawia identyfikator użytkownika, który zapisał odczyt.
     *
     * @param recordedBy identyfikator użytkownika
     */
    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }
}
