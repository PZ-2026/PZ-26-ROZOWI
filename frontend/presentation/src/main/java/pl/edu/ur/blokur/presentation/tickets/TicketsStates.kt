package pl.edu.ur.blokur.presentation.tickets

sealed interface TicketsState {
    data object Loading : TicketsState
}

sealed interface TicketsEvent {

}