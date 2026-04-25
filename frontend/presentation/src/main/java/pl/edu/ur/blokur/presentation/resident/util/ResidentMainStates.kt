package pl.edu.ur.blokur.presentation.resident.util

sealed interface ResidentMainState {
    data object Loading : ResidentMainState
    data class Error(val message : String) : ResidentMainState

    data object ViewingWelcome : ResidentMainState
    data object ViewingAnnouncements : ResidentMainState
    data object ViewingFinances : ResidentMainState
    data object ViewingProfile : ResidentMainState
    data object ViewingTickets : ResidentMainState
}

sealed interface ResidentMainEvent {
    data class ChangeResidentView(var option: NavBarOption) : ResidentMainEvent

    /** Emitowany po pomyślnym wylogowaniu – nawigacja powinna wrócić do ekranu logowania. */
    data object Logout : ResidentMainEvent
}