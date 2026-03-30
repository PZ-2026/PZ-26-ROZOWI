package pl.edu.ur.blokur.presentation.auth.viewmodel

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
import pl.edu.ur.blokur.domain.usecase.LoginUseCase
import pl.edu.ur.blokur.presentation.auth.util.AuthEvent
import pl.edu.ur.blokur.presentation.auth.util.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Wypełnij wszystkie pola")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            // LoginUseCase is not yet implemented — simulate success for UI demo
            delay(800)
            _state.value = AuthState.Success
            _events.send(AuthEvent.NavigateToMain)
        }
    }

    fun resetError() {
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }
}