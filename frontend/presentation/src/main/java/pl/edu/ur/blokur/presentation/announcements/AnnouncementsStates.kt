package pl.edu.ur.blokur.presentation.announcements

sealed interface AnnouncementsState {
    data object Loading : AnnouncementsState
}

sealed interface AnnouncementsEvent {

}