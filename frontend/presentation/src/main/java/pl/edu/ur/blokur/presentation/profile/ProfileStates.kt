package pl.edu.ur.blokur.presentation.profile

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Data(val name: String = "") : ProfileState
}

sealed interface ProfileEvent {
    data class ShowSnackbar(val message: String) : ProfileEvent
    data object ShowSaveDialog : ProfileEvent
    data object SaveSuccess : ProfileEvent
}