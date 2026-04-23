package pl.edu.ur.blokur.dto;

import pl.edu.ur.blokur.models.MediumType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO z danymi odczytu licznika zwracanymi przez API.
 */
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
        String recordedBy
    ) {
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
    }

    public UUID getMeterId() {
        return meterId;
    }

    public void setMeterId(UUID meterId) {
        this.meterId = meterId;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public MediumType getMediumType() {
        return mediumType;
    }

    public void setMediumType(MediumType mediumType) {
        this.mediumType = mediumType;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }
}
