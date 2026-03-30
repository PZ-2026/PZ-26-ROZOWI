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
import pl.edu.ur.blokur.presentation.auth.util.LoginFormFields
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(LoginFormFields("", "", false))
    val formFields: StateFlow<LoginFormFields> = _formFields.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onFormChanged(fields: LoginFormFields) {
        _formFields.value = fields
        // Clear error on any input change
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }

    fun login() {
        val fields = _formFields.value
        if (fields.email.isBlank() || fields.password.isBlank()) {
            _state.value = AuthState.Error("Wypełnij wszystkie pola")
            return
        }
        viewModelScope.launch {
            // LoginUseCase(fields.email,fields.password)
            //this is mock
            _state.value = AuthState.Loading
            delay(800)
            _state.value = AuthState.Success
            _events.send(AuthEvent.NavigateToMain)
        }
    }
}