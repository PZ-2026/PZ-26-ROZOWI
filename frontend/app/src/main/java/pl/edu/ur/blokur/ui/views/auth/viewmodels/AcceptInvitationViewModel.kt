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
import pl.edu.ur.blokur.ui.views.auth.utils.AcceptInvitationEvent
import pl.edu.ur.blokur.ui.views.auth.utils.AcceptInvitationFormFields
import pl.edu.ur.blokur.ui.views.auth.utils.AcceptInvitationState
import javax.inject.Inject

/**
 * ViewModel ekranu akceptacji zaproszenia.
 *
 * Użytkownik wpisuje swój email, 6-cyfrowy kod aktywacyjny otrzymany e-mailem oraz nowe hasło.
 */
@HiltViewModel
class AcceptInvitationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authService: AuthService
) : ViewModel() {

    private val initialEmail: String = savedStateHandle.get<String>("email") ?: ""

    private val _state = MutableStateFlow<AcceptInvitationState>(AcceptInvitationState.Idle)
    val state: StateFlow<AcceptInvitationState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(AcceptInvitationFormFields(email = initialEmail))
    val formFields: StateFlow<AcceptInvitationFormFields> = _formFields.asStateFlow()

    private val _events = Channel<AcceptInvitationEvent>()
    val events: Flow<AcceptInvitationEvent> = _events.receiveAsFlow()

    fun onFormChanged(fields: AcceptInvitationFormFields) {
        _formFields.value = fields
        if (_state.value is AcceptInvitationState.Error) {
            _state.value = AcceptInvitationState.Idle
        }
    }

    fun submit() {
        val fields = _formFields.value

        if (fields.email.isBlank()) {
            _state.value = AcceptInvitationState.Error("Podaj adres e-mail")
            return
        }
        if (!fields.code.matches(Regex("^\\d{6}$"))) {
            _state.value = AcceptInvitationState.Error("Kod musi składać się z 6 cyfr")
            return
        }
        if (fields.newPassword.isBlank() || fields.confirmPassword.isBlank()) {
            _state.value = AcceptInvitationState.Error("Wypełnij oba pola hasła")
            return
        }
        if (fields.newPassword.length < 8) {
            _state.value = AcceptInvitationState.Error("Hasło musi mieć co najmniej 8 znaków")
            return
        }
        if (!fields.newPassword.any { it.isUpperCase() }) {
            _state.value = AcceptInvitationState.Error("Hasło musi zawierać co najmniej jedną wielką literę")
            return
        }
        if (!fields.newPassword.any { it.isDigit() }) {
            _state.value = AcceptInvitationState.Error("Hasło musi zawierać co najmniej jedną cyfrę")
            return
        }
        if (fields.newPassword != fields.confirmPassword) {
            _state.value = AcceptInvitationState.Error("Hasła nie są identyczne")
            return
        }

        viewModelScope.launch {
            _state.value = AcceptInvitationState.Loading
            runCatching {
                authService.acceptInvitation(fields.email.trim(), fields.code, fields.newPassword)
            }
                .onSuccess { message ->
                    _state.value = AcceptInvitationState.Success(message)
                }
                .onFailure { e ->
                    _state.value = when (e) {
                        is AuthException.TokenExpired -> AcceptInvitationState.TokenExpired(
                            e.message ?: "Kod zaproszenia wygasł. Poproś zarządcę o nowe zaproszenie."
                        )
                        else -> AcceptInvitationState.Error(
                            e.message ?: "Wystąpił błąd. Spróbuj ponownie."
                        )
                    }
                }
        }
    }

    fun navigateToLogin() {
        viewModelScope.launch { _events.send(AcceptInvitationEvent.NavigateToLogin) }
    }
}
