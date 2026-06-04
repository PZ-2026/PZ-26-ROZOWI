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

import pl.edu.ur.blokur.services.UserService
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthService,
    private val userService: UserService
) : ViewModel() {

    /** Zwraca true jeśli zalogowany użytkownik ma rolę ZARZADCA. */
    suspend fun isManager(): Boolean =
        authService.getCurrentUserRole() == UserRole.ZARZADCA

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _state.value = ProfileState.Loading
                val userProfile = userService.getMe()
                _state.value = ProfileState.Data(
                    name = userProfile.fullName,
                    email = userProfile.email,
                    phone = userProfile.phone ?: ""
                )
            } catch (e: Exception) {
                _events.send(ProfileEvent.ShowSnackbar(e.message ?: "Błąd ładowania profilu"))
                // Można dodać stan Error, lub zostawić na pustym z fallbackiem
                _state.value = ProfileState.Data(name = "Błąd", email = "", phone = "")
            }
        }
    }

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