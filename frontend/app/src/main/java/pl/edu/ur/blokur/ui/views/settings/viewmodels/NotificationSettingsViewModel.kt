package pl.edu.ur.blokur.ui.views.settings.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Typy zdarzeń powiadomień, dla których zarządca może konfigurować przełączniki.
 */
data class NotificationToggle(
    val key: String,
    val label: String,
    val description: String,
    val enabled: Boolean = true
)

data class NotificationSettingsState(
    val toggles: List<NotificationToggle> = defaultToggles(),
    val isSaving: Boolean = false
)

fun defaultToggles() = listOf(
    NotificationToggle("new_ticket", "Nowe zgłoszenie", "Powiadomienie o nowym zgłoszeniu serwisowym"),
    NotificationToggle("ticket_status", "Zmiana statusu zgłoszenia", "Powiadomienie o zmianie statusu zgłoszenia"),
    NotificationToggle("new_resolution", "Nowa uchwała", "Powiadomienie o wystawieniu nowej uchwały do głosowania"),
    NotificationToggle("vote_result", "Wynik głosowania", "Powiadomienie o zakończeniu głosowania nad uchwałą"),
    NotificationToggle("new_announcement", "Nowe ogłoszenie", "Powiadomienie o nowym ogłoszeniu zarządcy"),
    NotificationToggle("payment_overdue", "Zaległość w płatnościach", "Powiadomienie o zaległościach w opłatach"),
    NotificationToggle("inspection_reminder", "Nadchodzące przeglądy", "Przypomnienie o zaplanowanych przeglądach"),
    NotificationToggle("meter_reading", "Odczyt licznika", "Przypomnienie o terminie odczytu liczników")
)

/**
 * ViewModel ustawień powiadomień.
 *
 * WIP: Aktualnie przechowuje stan w pamięci. Docelowo powinien
 * komunikować się z backendem (endpoint do ustawień powiadomień per użytkownik).
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(NotificationSettingsState())
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    fun onToggleChanged(key: String, enabled: Boolean) {
        val current = _state.value
        _state.value = current.copy(
            toggles = current.toggles.map {
                if (it.key == key) it.copy(enabled = enabled) else it
            }
        )
        // TODO WIP: Zapisz ustawienie do backendu lub DataStore
    }
}
