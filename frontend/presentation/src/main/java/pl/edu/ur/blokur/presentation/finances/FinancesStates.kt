package pl.edu.ur.blokur.presentation.finances
sealed interface FinancesState {
    data object Loading : FinancesState
}

sealed interface FinancesEvent {

}