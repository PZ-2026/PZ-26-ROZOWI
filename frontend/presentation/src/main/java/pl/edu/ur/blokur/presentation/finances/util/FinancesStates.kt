package pl.edu.ur.blokur.presentation.finances.util

import pl.edu.ur.blokur.domain.model.ApartmentBalance
import pl.edu.ur.blokur.domain.model.FinancialDocument
import pl.edu.ur.blokur.domain.model.FinancialTransaction

sealed interface FinancesState {
    data object Loading : FinancesState
    data class Error(val message: String) : FinancesState
    data class Data(
        val balance: ApartmentBalance,
        val transactions: List<FinancialTransaction>,
        val documents: List<FinancialDocument>
    ) : FinancesState
}

sealed interface FinancesEvent {
    data object NavigateToTransactions : FinancesEvent
    data object NavigateToDocuments : FinancesEvent
}