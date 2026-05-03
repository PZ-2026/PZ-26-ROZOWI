package pl.edu.ur.blokur.ui.views.main.viewmodels

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
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.ui.views.main.utils.BottomNavItem
import pl.edu.ur.blokur.ui.views.main.utils.NavBarOption
import pl.edu.ur.blokur.ui.views.main.utils.ResidentMainEvent
import pl.edu.ur.blokur.ui.views.main.utils.ResidentMainState
import pl.edu.ur.blokur.ui.views.main.utils.bottomNavItems
import pl.edu.ur.blokur.ui.views.main.utils.navItemsForRole
import javax.inject.Inject

/**
 * ViewModel głównego ekranu po zalogowaniu.
 *
 * @property authService serwis autoryzacji (logout + rola).
 */
@HiltViewModel
class ResidentMainViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow<ResidentMainState>(ResidentMainState.Loading)
    val state: StateFlow<ResidentMainState> = _state.asStateFlow()

    private val _availableNavItems = MutableStateFlow(bottomNavItems)
    val availableNavItems: StateFlow<List<BottomNavItem>> = _availableNavItems.asStateFlow()

    private val _events = Channel<ResidentMainEvent>()
    val events: Flow<ResidentMainEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val role = authService.getCurrentUserRole()
            _availableNavItems.value = navItemsForRole(role)
            _state.value = ResidentMainState.ViewingWelcome
        }
    }

    fun onOptionClicked(option: NavBarOption) {
        viewModelScope.launch {
            _events.send(ResidentMainEvent.ChangeResidentView(option))
            _state.value = when (option) {
                NavBarOption.NONE -> ResidentMainState.Loading
                NavBarOption.ANNOUNCEMENTS -> ResidentMainState.ViewingAnnouncements
                NavBarOption.FINANCES -> ResidentMainState.ViewingFinances
                NavBarOption.PROFILE -> ResidentMainState.ViewingProfile
                NavBarOption.TICKETS -> ResidentMainState.ViewingTickets
                NavBarOption.PROPERTIES -> ResidentMainState.ViewingProperties
                NavBarOption.USERS -> ResidentMainState.ViewingUsers
                NavBarOption.CATEGORIES -> ResidentMainState.ViewingCategories
                NavBarOption.RESOLUTIONS -> ResidentMainState.ViewingResolutions
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _events.send(ResidentMainEvent.Logout)
        }
    }
}
