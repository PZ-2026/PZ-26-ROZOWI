package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO zbiorczej odpowiedzi dla endpointu GET transakcji lokalu. Zawiera zbuforowane saldo ({@code
 * currentBalance}) oraz historię transakcji.
 */
public class ApartmentTransactionsResponse {

    private BigDecimal currentBalance;
    private List<FinancialTransactionResponse> transactions;

    public ApartmentTransactionsResponse(
            BigDecimal currentBalance, List<FinancialTransactionResponse> transactions) {
        this.currentBalance = currentBalance;
        this.transactions = transactions;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<FinancialTransactionResponse> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FinancialTransactionResponse> transactions) {
        this.transactions = transactions;
    }
}
