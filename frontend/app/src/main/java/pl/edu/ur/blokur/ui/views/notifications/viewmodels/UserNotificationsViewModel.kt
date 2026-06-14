package pl.edu.ur.blokur.ui.views.notifications.viewmodels

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
import pl.edu.ur.blokur.dtos.NotificationConfigDto
import pl.edu.ur.blokur.services.NotificationService
import javax.inject.Inject

@HiltViewModel
class UserNotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _events = Channel<NotificationsEvent>()
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _state.value = NotificationsUiState.Loading
            runCatching { notificationService.getMySettings() }
                .onSuccess { _state.value = NotificationsUiState.Success(it) }
                .onFailure { _state.value = NotificationsUiState.Error(it.message ?: "Błąd ładowania") }
        }
    }

    fun toggleSetting(eventType: String, enabled: Boolean) {
        val current = _state.value as? NotificationsUiState.Success ?: return
        viewModelScope.launch {
            _state.value = current.copy(updatingEventType = eventType)
            runCatching { notificationService.updateMySetting(eventType, enabled) }
                .onSuccess { updated ->
                    _state.value = NotificationsUiState.Success(
                        settings = current.settings.map {
                            if (it.eventType == eventType) updated else it
                        }
                    )
                    val status = if (enabled) "włączone" else "wyłączone"
                    _events.send(NotificationsEvent.ShowSnackbar("${updated.label}: $status"))
                }
                .onFailure { e ->
                    _state.value = current.copy(updatingEventType = null)
                    _events.send(NotificationsEvent.ShowSnackbar(e.message ?: "Błąd aktualizacji"))
                }
        }
    }
}
