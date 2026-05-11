package pl.edu.ur.blokur.ui.views.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.ui.views.profile.utils.ProfileEvent
import pl.edu.ur.blokur.ui.views.profile.utils.ProfileState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    /** Zwraca true jeśli zalogowany użytkownik ma rolę ZARZADCA. */
    suspend fun isManager(): Boolean =
        authService.getCurrentUserRole() == UserRole.ZARZADCA

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Data())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>()
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    fun onNameChanged(name: String) {
        val current = _state.value
        if (current is ProfileState.Data) {
            _state.value = current.copy(name = name)
        }
    }

    fun requestSave() {
        viewModelScope.launch { _events.send(ProfileEvent.ShowSaveDialog) }
    }

    fun confirmSave() {
        viewModelScope.launch {
            delay(300)
            _events.send(ProfileEvent.ShowSnackbar("Zapisano zmiany"))
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch { _events.send(ProfileEvent.ShowSnackbar("To jest przykładowy snackbar")) }
    }
}