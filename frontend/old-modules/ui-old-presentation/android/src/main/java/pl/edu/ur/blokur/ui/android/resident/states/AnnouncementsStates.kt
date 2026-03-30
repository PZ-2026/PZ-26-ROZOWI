package pl.edu.ur.blokur.ui.android.resident.states

sealed interface AnnouncementsState {
    data object Loading : AnnouncementsState
}

sealed interface AnnouncementsEvent {

}