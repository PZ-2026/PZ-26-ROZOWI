package pl.edu.ur.blokur.ui.views.profile.utils

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
    data class Data(
        val role: String = "",
        val email: String = "",
        val name: String = "",
        val phone: String = ""
    ) : ProfileState
}

sealed interface ProfileEvent {
    data class ShowSnackbar(val message: String) : ProfileEvent
}
