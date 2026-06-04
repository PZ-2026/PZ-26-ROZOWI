package pl.edu.ur.blokur.ui.views.finances.utils

import pl.edu.ur.blokur.dtos.ApartmentBalanceDto
import pl.edu.ur.blokur.dtos.TransactionDto
import pl.edu.ur.blokur.dtos.UserDocumentDto

sealed interface FinancesState {
    data object Loading : FinancesState
    data class Error(val message: String) : FinancesState
    data class Data(
        val balance: ApartmentBalanceDto,
        val transactions: List<TransactionDto>,
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
