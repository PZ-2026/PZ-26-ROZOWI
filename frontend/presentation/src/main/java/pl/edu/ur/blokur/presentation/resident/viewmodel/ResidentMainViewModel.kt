package pl.edu.ur.blokur.presentation.resident.viewmodel

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
import pl.edu.ur.blokur.domain.model.UserRole
import pl.edu.ur.blokur.domain.usecase.GetCurrentUserRoleUseCase
import pl.edu.ur.blokur.domain.usecase.LogoutUseCase
import pl.edu.ur.blokur.domain.usecase.TestAUseCase
import pl.edu.ur.blokur.presentation.resident.util.BottomNavItem
import pl.edu.ur.blokur.presentation.resident.util.NavBarOption
import pl.edu.ur.blokur.presentation.resident.util.ResidentMainEvent
import pl.edu.ur.blokur.presentation.resident.util.ResidentMainState
import pl.edu.ur.blokur.presentation.resident.util.bottomNavItems
import pl.edu.ur.blokur.presentation.resident.util.navItemsForRole
import javax.inject.Inject

/**
 * ViewModel głównego ekranu po zalogowaniu.
 *
 * Odpowiada za:
 * - wczytanie roli użytkownika i dobranie odpowiedniego zestawu zakładek,
 * - obsługę kliknięć w dolną nawigację,
 * - wylogowanie użytkownika.
 *
 * @property testAUseCase   tymczasowy UseCase testowy (do usunięcia przy finalizacji).
 * @property logoutUseCase  UseCase wylogowania – czyści tokeny sesji.
 * @property getCurrentUserRoleUseCase UseCase pobierający rolę z DataStore.
 */
@HiltViewModel
class ResidentMainViewModel @Inject constructor(
    private val testAUseCase: TestAUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserRoleUseCase: GetCurrentUserRoleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ResidentMainState>(ResidentMainState.Loading)

    /** Aktualny stan ekranu – określa tytuł TopBar i wybraną zakładkę. */
    val state: StateFlow<ResidentMainState> = _state.asStateFlow()

    private val _availableNavItems = MutableStateFlow(bottomNavItems)

    /** Lista zakładek dolnej nawigacji filtrowana według roli użytkownika. */
    val availableNavItems: StateFlow<List<BottomNavItem>> = _availableNavItems.asStateFlow()

    private val _events = Channel<ResidentMainEvent>()

    /** Jednorazowe zdarzenia nawigacyjne (zmiana widoku, wylogowanie). */
    val events: Flow<ResidentMainEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            testAUseCase("1")
            testAUseCase("2")
            val role = getCurrentUserRoleUseCase()
            _availableNavItems.value = navItemsForRole(role)
            val initialOption = when (role) {
                UserRole.ZARZADCA -> NavBarOption.PROPERTY_TREE
                UserRole.KONSERWATOR -> NavBarOption.TICKETS
                else -> NavBarOption.PROFILE
            }
            onOptionClicked(initialOption)
        }
    }

    /**
     * Obsługuje kliknięcie zakładki w dolnej nawigacji.
     * Emituje [ResidentMainEvent.ChangeResidentView] i aktualizuje stan ekranu.
     *
     * @param option wybrana opcja nawigacji.
     */
    fun onOptionClicked(option: NavBarOption) {
        viewModelScope.launch {
            _events.send(ResidentMainEvent.ChangeResidentView(option))
            _state.value = when (option) {
                NavBarOption.NONE -> ResidentMainState.Loading
                NavBarOption.ANNOUNCEMENTS -> ResidentMainState.ViewingAnnouncements
                NavBarOption.FINANCES -> ResidentMainState.ViewingFinances
                NavBarOption.PROPERTY_TREE -> ResidentMainState.ViewingPropertyTree
                NavBarOption.PROFILE -> ResidentMainState.ViewingProfile
                NavBarOption.TICKETS -> ResidentMainState.ViewingTickets
            }
        }
    }

    /**
     * Wylogowuje użytkownika – czyści tokeny i emituje [ResidentMainEvent.Logout].
     * Nawigacja do ekranu logowania obsługiwana jest przez Screen/AppNavHost.
     */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _events.send(ResidentMainEvent.Logout)
        }
    }
}
