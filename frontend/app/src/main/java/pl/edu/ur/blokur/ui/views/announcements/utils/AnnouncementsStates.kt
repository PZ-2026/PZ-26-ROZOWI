package pl.edu.ur.blokur.ui.views.announcements.utils

import pl.edu.ur.blokur.dtos.AnnouncementDto

sealed interface AnnouncementsState {
    data object Loading : AnnouncementsState
    data object Empty : AnnouncementsState
    data class Success(val announcements: List<AnnouncementDto>) : AnnouncementsState
    data class Error(val message: String) : AnnouncementsState
}

sealed interface AnnouncementsEvent {
    data class ShowError(val message: String) : AnnouncementsEvent
}