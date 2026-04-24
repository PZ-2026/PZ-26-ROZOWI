package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** DTO z danymi pojedynczej transakcji finansowej zwracanymi przez API. */
public class FinancialTransactionResponse {

    private UUID id;
    private UUID apartmentId;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private String recordedByEmail;

    public FinancialTransactionResponse(
            UUID id,
            UUID apartmentId,
            String type,
            BigDecimal amount,
            String description,
            LocalDate transactionDate,
            String recordedByEmail) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.recordedByEmail = recordedByEmail;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getRecordedByEmail() {
        return recordedByEmail;
    }

    public void setRecordedByEmail(String recordedByEmail) {
        this.recordedByEmail = recordedByEmail;
    }
}
