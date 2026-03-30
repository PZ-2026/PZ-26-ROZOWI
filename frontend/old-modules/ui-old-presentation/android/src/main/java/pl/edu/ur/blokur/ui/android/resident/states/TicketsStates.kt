package pl.edu.ur.blokur.ui.android.resident.states

sealed interface TicketsState {
    data object Loading : TicketsState
}

sealed interface TicketsEvent {

}