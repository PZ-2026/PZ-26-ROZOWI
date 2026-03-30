package pl.edu.ur.blokur.presentation.tickets.viewmodel

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
import pl.edu.ur.blokur.domain.usecase.CreateServiceTicketUseCase
import pl.edu.ur.blokur.presentation.tickets.util.CreateTicketEvent
import pl.edu.ur.blokur.presentation.tickets.util.CreateTicketState
import javax.inject.Inject

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val createServiceTicketUseCase: CreateServiceTicketUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreateTicketState>(CreateTicketState.Idle)
    val state: StateFlow<CreateTicketState> = _state.asStateFlow()

    private val _events = Channel<CreateTicketEvent>()
    val events: Flow<CreateTicketEvent> = _events.receiveAsFlow()

    val categories = listOf("Hydraulika", "Elektryka", "Domofony", "Części wspólne", "Winda", "Inne")

    fun submit(title: String, category: String, description: String) {
        viewModelScope.launch {
            _state.value = CreateTicketState.Submitting
            delay(800) // Symulacja zapisu — UseCase niezaimplementowany
            _state.value = CreateTicketState.Success
            _events.send(CreateTicketEvent.NavigateBack)
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch { _events.send(CreateTicketEvent.NavigateBack) }
    }
}