package pl.edu.ur.blokur.ui.views.announcements.utils

sealed interface AnnouncementsState {
    data object Loading : AnnouncementsState
    data object Empty : AnnouncementsState
}

sealed interface AnnouncementsEvent