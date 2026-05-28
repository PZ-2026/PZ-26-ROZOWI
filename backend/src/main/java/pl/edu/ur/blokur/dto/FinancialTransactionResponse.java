package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/** DTO z danymi pojedynczej transakcji finansowej zwracanymi przez API. */
@Data
public class FinancialTransactionResponse {

    private UUID id;
    private UUID apartmentId;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private String recordedByEmail;

    /**
     * Tworzy odpowiedź z danymi transakcji finansowej.
     *
     * @param id identyfikator transakcji
     * @param apartmentId identyfikator lokalu powiązanego z transakcją
     * @param type typ transakcji
     * @param amount kwota transakcji
     * @param description opis transakcji
     * @param transactionDate data transakcji
     * @param recordedByEmail email użytkownika, który zapisał transakcję
     */
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
}
