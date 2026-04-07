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
import pl.edu.ur.blokur.presentation.tickets.util.CreateTicketFormState
import pl.edu.ur.blokur.presentation.tickets.util.CreateTicketScreenEvent
import pl.edu.ur.blokur.presentation.tickets.util.CreateTicketSubmitState
import javax.inject.Inject

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val createServiceTicketUseCase: CreateServiceTicketUseCase
) : ViewModel() {

    val categories = listOf("Hydraulika", "Elektryka", "Domofony", "Części wspólne", "Winda", "Inne")

    private val _formState = MutableStateFlow(CreateTicketFormState())
    val formState: StateFlow<CreateTicketFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<CreateTicketSubmitState>(CreateTicketSubmitState.Idle)
    val submitState: StateFlow<CreateTicketSubmitState> = _submitState.asStateFlow()

    private val _events = Channel<CreateTicketScreenEvent>()
    val events: Flow<CreateTicketScreenEvent> = _events.receiveAsFlow()

    fun onFormChanged(state: CreateTicketFormState) {
        _formState.value = state
        if (_submitState.value is CreateTicketSubmitState.Error) {
            _submitState.value = CreateTicketSubmitState.Idle
        }
    }

    fun submit() {
        val form = _formState.value
        if (form.title.isBlank() || form.selectedCategory.isBlank() || form.description.isBlank()) {
            _submitState.value = CreateTicketSubmitState.Error("Wypełnij wszystkie pola")
            return
        }
        viewModelScope.launch {
            _submitState.value = CreateTicketSubmitState.Submitting
            delay(800) // UseCase niezaimplementowany
            _submitState.value = CreateTicketSubmitState.Success
            _events.send(CreateTicketScreenEvent.NavigateBack)
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch { _events.send(CreateTicketScreenEvent.NavigateBack) }
    }
}