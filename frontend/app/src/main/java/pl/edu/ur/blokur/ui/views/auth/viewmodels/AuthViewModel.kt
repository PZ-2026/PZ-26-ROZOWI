package pl.edu.ur.blokur.ui.views.auth.viewmodels

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
import pl.edu.ur.blokur.ui.views.auth.utils.AuthEvent
import pl.edu.ur.blokur.ui.views.auth.utils.AuthState
import pl.edu.ur.blokur.ui.views.auth.utils.LoginFormFields
import javax.inject.Inject

/**
 * ViewModel ekranu logowania.
 *
 * @property authService serwis autoryzacji.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(LoginFormFields("", "", false))
    val formFields: StateFlow<LoginFormFields> = _formFields.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onFormChanged(fields: LoginFormFields) {
        _formFields.value = fields
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }

    fun login() {
        val fields = _formFields.value
        if (fields.email.isBlank() || fields.password.isBlank()) {
            _state.value = AuthState.Error("Wypełnij wszystkie pola")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching { authService.login(fields.email, fields.password) }
                .onSuccess { role ->
                    _state.value = AuthState.Success
                    _events.send(AuthEvent.NavigateToMain(role))
                }
                .onFailure { e ->
                    _state.value = AuthState.Error(e.message ?: "Błąd logowania")
                }
        }
    }
}
