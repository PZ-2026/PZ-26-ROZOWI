package pl.edu.ur.blokur.ui.views.finances.utils

import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import pl.edu.ur.blokur.dtos.UserDocumentDto
import java.math.BigDecimal

sealed interface FinancesState {
    data object Loading : FinancesState
    data class Error(val message: String) : FinancesState
    data class Data(
        val currentBalance: BigDecimal,
        val transactions: List<FinancialTransactionDto>,
        val documents: List<UserDocumentDto>
    ) : FinancesState
}

sealed interface FinancesEvent {
    data object NavigateToTransactions : FinancesEvent
    data object NavigateToDocuments : FinancesEvent
    data object NavigateToLedger : FinancesEvent
    data object NavigateToBalances : FinancesEvent
    data class OpenPdf(val uri: android.net.Uri) : FinancesEvent
    data class ShowSnackbar(val message: String) : FinancesEvent
}
