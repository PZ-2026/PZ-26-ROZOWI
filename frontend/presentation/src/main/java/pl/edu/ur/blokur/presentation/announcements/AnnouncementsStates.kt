package pl.edu.ur.blokur.presentation.announcements

sealed interface AnnouncementsState {
    data object Loading : AnnouncementsState
    data object Empty : AnnouncementsState
}

sealed interface AnnouncementsEvent