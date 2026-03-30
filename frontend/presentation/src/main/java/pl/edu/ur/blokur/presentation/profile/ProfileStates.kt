package pl.edu.ur.blokur.presentation.profile

sealed interface ProfileState {
    data object Loading : ProfileState
}

sealed interface ProfileEvent {

}