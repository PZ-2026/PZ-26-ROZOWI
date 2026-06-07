package pl.edu.ur.blokur.ui.views.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.services.UserService
import pl.edu.ur.blokur.ui.views.profile.utils.ProfileEvent
import pl.edu.ur.blokur.ui.views.profile.utils.ProfileState
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

    private val _events = Channel<ProfileEvent>()
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    init {
        loadUserProfile()
    }

    fun reload() {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _state.value = ProfileState.Loading
                val profile = userService.getMe()
                val displayRole = when (profile.role) {
                    "MIESZKANIEC" -> "Mieszkaniec"
                    "KONSERWATOR" -> "Konserwator"
                    "ZARZADCA" -> "Zarządca"
                    else -> profile.role
                }
                _state.value = ProfileState.Data(
                    role = displayRole,
                    email = profile.email,
                    name = profile.fullName,
                    phone = profile.phone.orEmpty()
                )
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Błąd ładowania profilu")
            }
        }
    }
}
