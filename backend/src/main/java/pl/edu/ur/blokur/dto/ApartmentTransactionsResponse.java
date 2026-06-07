package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * DTO zbiorczej odpowiedzi dla endpointu GET transakcji lokalu. Zawiera zbuforowane saldo ({@code
 * currentBalance}) oraz historię transakcji.
 */
@Data
public class ApartmentTransactionsResponse {

    private BigDecimal currentBalance;
    private List<FinancialTransactionResponse> transactions;

    public ApartmentTransactionsResponse(
            BigDecimal currentBalance, List<FinancialTransactionResponse> transactions) {
        this.currentBalance = currentBalance;
        this.transactions = transactions;
    }
}
