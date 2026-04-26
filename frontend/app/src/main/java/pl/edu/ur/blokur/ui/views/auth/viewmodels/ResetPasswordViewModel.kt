package pl.edu.ur.blokur.ui.views.auth.viewmodels

import androidx.lifecycle.SavedStateHandle
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
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordEvent
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordFormFields
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordState
import javax.inject.Inject

/**
 * ViewModel ekranu resetowania hasła.
 *
 * Token odczytywany jest z SavedStateHandle (nawigacja type-safe).
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authService: AuthService
) : ViewModel() {

    /** Token z linku mailowego — przekazywany przez nawigację. */
    val token: String = savedStateHandle.get<String>("token") ?: ""

    private val _state = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(ResetPasswordFormFields())
    val formFields: StateFlow<ResetPasswordFormFields> = _formFields.asStateFlow()

    private val _events = Channel<ResetPasswordEvent>()
    val events: Flow<ResetPasswordEvent> = _events.receiveAsFlow()

    fun onFormChanged(fields: ResetPasswordFormFields) {
        _formFields.value = fields
        if (_state.value is ResetPasswordState.Error) {
            _state.value = ResetPasswordState.Idle
        }
    }

    fun submit() {
        val fields = _formFields.value

        if (fields.newPassword.isBlank() || fields.confirmPassword.isBlank()) {
            _state.value = ResetPasswordState.Error("Wypełnij oba pola")
            return
        }
        if (fields.newPassword.length < 8) {
            _state.value = ResetPasswordState.Error("Hasło musi mieć co najmniej 8 znaków")
            return
        }
        if (fields.newPassword != fields.confirmPassword) {
            _state.value = ResetPasswordState.Error("Hasła nie są identyczne")
            return
        }
        if (token.isBlank()) {
            _state.value = ResetPasswordState.Error("Brak tokenu resetowania. Otwórz link z e-maila.")
            return
        }

        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading
            runCatching { authService.resetPassword(token, fields.newPassword) }
                .onSuccess { message ->
                    _state.value = ResetPasswordState.Success(message)
                }
                .onFailure { e ->
                    _state.value = ResetPasswordState.Error(
                        e.message ?: "Wystąpił błąd. Spróbuj ponownie."
                    )
                }
        }
    }

    fun navigateToLogin() {
        viewModelScope.launch { _events.send(ResetPasswordEvent.NavigateToLogin) }
    }
}
