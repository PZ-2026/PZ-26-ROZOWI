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
import pl.edu.ur.blokur.dtos.AuthException
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordEvent
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordFormFields
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordState
import javax.inject.Inject

/**
 * ViewModel ekranu resetowania hasła.
 *
 * Email może być wstępnie wypełniony z poprzedniego ekranu (Forgot Password). Użytkownik wpisuje
 * 6-cyfrowy kod otrzymany e-mailem oraz nowe hasło.
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authService: AuthService
) : ViewModel() {

    private val initialEmail: String = savedStateHandle.get<String>("email") ?: ""

    private val _state = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(ResetPasswordFormFields(email = initialEmail))
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

        if (fields.email.isBlank()) {
            _state.value = ResetPasswordState.Error("Podaj adres e-mail")
            return
        }
        if (!fields.code.matches(Regex("^\\d{6}$"))) {
            _state.value = ResetPasswordState.Error("Kod musi składać się z 6 cyfr")
            return
        }
        if (fields.newPassword.isBlank() || fields.confirmPassword.isBlank()) {
            _state.value = ResetPasswordState.Error("Wypełnij oba pola hasła")
            return
        }
        if (fields.newPassword.length < 8) {
            _state.value = ResetPasswordState.Error("Hasło musi mieć co najmniej 8 znaków")
            return
        }
        if (!fields.newPassword.any { it.isUpperCase() }) {
            _state.value = ResetPasswordState.Error("Hasło musi zawierać co najmniej jedną wielką literę")
            return
        }
        if (!fields.newPassword.any { it.isDigit() }) {
            _state.value = ResetPasswordState.Error("Hasło musi zawierać co najmniej jedną cyfrę")
            return
        }
        if (fields.newPassword != fields.confirmPassword) {
            _state.value = ResetPasswordState.Error("Hasła nie są identyczne")
            return
        }

        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading
            runCatching {
                authService.resetPassword(fields.email.trim(), fields.code, fields.newPassword)
            }
                .onSuccess { message ->
                    _state.value = ResetPasswordState.Success(message)
                }
                .onFailure { e ->
                    _state.value = when (e) {
                        is AuthException.TokenExpired -> ResetPasswordState.TokenExpired(
                            e.message ?: "Kod resetowania hasła wygasł."
                        )
                        is AuthException.RateLimited -> ResetPasswordState.Error(
                            e.message ?: "Zbyt wiele prób."
                        )
                        else -> ResetPasswordState.Error(
                            e.message ?: "Wystąpił błąd. Spróbuj ponownie."
                        )
                    }
                }
        }
    }

    fun navigateToLogin() {
        viewModelScope.launch { _events.send(ResetPasswordEvent.NavigateToLogin) }
    }

    fun navigateToForgotPassword() {
        viewModelScope.launch { _events.send(ResetPasswordEvent.NavigateToForgotPassword) }
    }
}
