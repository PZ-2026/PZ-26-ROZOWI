package pl.edu.ur.blokur.ui.android.resident.states
sealed interface FinancesState {
    data object Loading : FinancesState
}

sealed interface FinancesEvent {

}