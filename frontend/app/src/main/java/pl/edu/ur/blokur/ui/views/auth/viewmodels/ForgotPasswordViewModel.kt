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
import pl.edu.ur.blokur.ui.views.auth.utils.ForgotPasswordEvent
import pl.edu.ur.blokur.ui.views.auth.utils.ForgotPasswordFormFields
import pl.edu.ur.blokur.ui.views.auth.utils.ForgotPasswordState
import javax.inject.Inject

/**
 * ViewModel ekranu „Zapomniałem hasła".
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(ForgotPasswordFormFields())
    val formFields: StateFlow<ForgotPasswordFormFields> = _formFields.asStateFlow()

    private val _events = Channel<ForgotPasswordEvent>()
    val events: Flow<ForgotPasswordEvent> = _events.receiveAsFlow()

    fun onFormChanged(fields: ForgotPasswordFormFields) {
        _formFields.value = fields
        if (_state.value is ForgotPasswordState.Error) {
            _state.value = ForgotPasswordState.Idle
        }
    }

    fun submit() {
        val email = _formFields.value.email.trim()

        if (email.isBlank()) {
            _state.value = ForgotPasswordState.Error("Podaj adres e-mail")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = ForgotPasswordState.Error("Podaj poprawny adres e-mail")
            return
        }

        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            runCatching { authService.forgotPassword(email) }
                .onSuccess { message ->
                    _state.value = ForgotPasswordState.Success(message)
                }
                .onFailure { e ->
                    _state.value = ForgotPasswordState.Error(
                        e.message ?: "Wystąpił błąd. Spróbuj ponownie."
                    )
                }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch { _events.send(ForgotPasswordEvent.NavigateBack) }
    }
}
