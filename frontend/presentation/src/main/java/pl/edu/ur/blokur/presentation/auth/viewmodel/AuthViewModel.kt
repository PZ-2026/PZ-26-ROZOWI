package pl.edu.ur.blokur.presentation.auth.viewmodel

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
import pl.edu.ur.blokur.domain.usecase.LoginUseCase
import pl.edu.ur.blokur.presentation.auth.util.AuthEvent
import pl.edu.ur.blokur.presentation.auth.util.AuthState
import pl.edu.ur.blokur.presentation.auth.util.LoginFormFields
import javax.inject.Inject

/**
 * ViewModel ekranu logowania.
 *
 * Zarządza stanem formularza ([formFields]) oraz stanem żądania ([state]).
 * Jednorazowe zdarzenia nawigacyjne i błędy emituje przez [events] (Channel).
 *
 * @property loginUseCase UseCase wykonujący logowanie przez domenę.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)

    /** Reaktywny stan ekranu logowania – obserwowany przez Screen. */
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(LoginFormFields("", "", false))

    /** Aktualny stan pól formularza (e-mail, hasło, widoczność hasła). */
    val formFields: StateFlow<LoginFormFields> = _formFields.asStateFlow()

    private val _events = Channel<AuthEvent>()

    /** Jednorazowe zdarzenia nawigacyjne i feedback (odbierane dokładnie raz). */
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    /**
     * Aktualizuje stan pól formularza i kasuje ewentualny błąd walidacji.
     *
     * @param fields nowy stan pól formularza.
     */
    fun onFormChanged(fields: LoginFormFields) {
        _formFields.value = fields
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }

    /**
     * Inicjuje logowanie po walidacji pól formularza.
     *
     * Po sukcesie emituje [AuthEvent.NavigateToMain] z rolą użytkownika.
     * Po błędzie ustawia stan [AuthState.Error] z komunikatem z wyjątku domenowego.
     */
    fun login() {
        val fields = _formFields.value
        if (fields.email.isBlank() || fields.password.isBlank()) {
            _state.value = AuthState.Error("Wypełnij wszystkie pola")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching { loginUseCase(fields.email, fields.password) }
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
