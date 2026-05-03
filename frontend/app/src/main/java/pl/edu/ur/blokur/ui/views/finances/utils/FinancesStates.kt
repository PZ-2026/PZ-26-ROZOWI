package pl.edu.ur.blokur.ui.views.finances.utils

import pl.edu.ur.blokur.dtos.ApartmentBalanceDto
import pl.edu.ur.blokur.dtos.DocumentDto
import pl.edu.ur.blokur.dtos.TransactionDto

sealed interface FinancesState {
    data object Loading : FinancesState
    data class Error(val message: String) : FinancesState
    data class Data(
        val balance: ApartmentBalanceDto,
        val transactions: List<TransactionDto>,
        val documents: List<DocumentDto>
    ) : FinancesState
}

sealed interface FinancesEvent {
    data object NavigateToTransactions : FinancesEvent
    data object NavigateToDocuments : FinancesEvent
    data object NavigateToLedger : FinancesEvent
}