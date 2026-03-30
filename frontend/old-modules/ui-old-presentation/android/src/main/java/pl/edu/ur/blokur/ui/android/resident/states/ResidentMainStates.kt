package pl.edu.ur.blokur.ui.android.resident.states

import pl.edu.ur.blokur.ui.android.resident.NavBarOption

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
}