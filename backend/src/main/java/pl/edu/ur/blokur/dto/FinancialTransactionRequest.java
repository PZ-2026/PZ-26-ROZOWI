package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** DTO z danymi transakcji finansowej przesyłanymi przez klienta. */
@Data
public class FinancialTransactionRequest {

    @NotBlank(message = "Typ transakcji jest wymagany")
    private String type;

    @NotNull(message = "Kwota transakcji jest wymagana")
    private BigDecimal amount;

    @NotBlank(message = "Opis transakcji jest wymagany")
    private String description;

    @NotNull(message = "Data transakcji jest wymagana")
    private LocalDate transactionDate;

    public FinancialTransactionRequest() {}

    public FinancialTransactionRequest(
            String type, BigDecimal amount, String description, LocalDate transactionDate) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }
}
