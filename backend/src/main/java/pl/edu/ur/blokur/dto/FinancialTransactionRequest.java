package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO z danymi transakcji finansowej przesyłanymi przez klienta.
 */
public class FinancialTransactionRequest {

    @NotBlank(message = "Typ transakcji jest wymagany")
    private String type;

    @NotNull(message = "Kwota transakcji jest wymagana")
    private BigDecimal amount;

    @NotBlank(message = "Opis transakcji jest wymagany")
    private String description;

    @NotNull(message = "Data transakcji jest wymagana")
    private LocalDate transactionDate;

    public FinancialTransactionRequest() {
    }

    public FinancialTransactionRequest(String type, BigDecimal amount,
                                       String description, LocalDate transactionDate) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
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
}
