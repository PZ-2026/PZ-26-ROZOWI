package pl.edu.ur.blokur.ui.android.resident.states

sealed interface ProfileState {
    data object Loading : ProfileState
}

sealed interface ProfileEvent {

}