package pl.edu.ur.blokur.ui.views.auth.viewmodels

import android.util.Log
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
import pl.edu.ur.blokur.services.DeviceService
import pl.edu.ur.blokur.services.FcmTokenProvider
import pl.edu.ur.blokur.ui.views.auth.utils.AuthEvent
import pl.edu.ur.blokur.ui.views.auth.utils.AuthState
import pl.edu.ur.blokur.ui.views.auth.utils.LoginFormFields
import javax.inject.Inject

/**
 * ViewModel ekranu logowania.
 *
 * Po pomyślnym zalogowaniu próbuje pobrać aktualny FCM token (przez FcmTokenProvider)
 * i zarejestrować urządzenie w backendzie. Błędy rejestracji są łagodnie logowane
 * — nie blokują nawigacji do ekranu głównego.
 *
 * @property authService      serwis autoryzacji
 * @property deviceService    serwis rejestracji urządzenia FCM
 * @property fcmTokenProvider dostawca tokenu FCM (NoOpFcmTokenProvider gdy Firebase niedostępne)
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val deviceService: DeviceService,
    private val fcmTokenProvider: FcmTokenProvider
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _formFields = MutableStateFlow(LoginFormFields("", "", false))
    val formFields: StateFlow<LoginFormFields> = _formFields.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onFormChanged(fields: LoginFormFields) {
        _formFields.value = fields
        // Resetuj błąd przy każdej zmianie formularza (również dla AccountLocked)
        if (_state.value is AuthState.Error || _state.value is AuthState.AccountLocked) {
            _state.value = AuthState.Idle
        }
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
                    // Rejestracja FCM token — fire-and-forget, błędy nie blokują nawigacji
                    tryRegisterFcmToken()
                }
                .onFailure { e ->
                    // HTTP 423 — konto zablokowane po przekroczeniu limitu prób
                    _state.value = if (e is AuthException.AccountLocked) {
                        AuthState.AccountLocked(
                            e.message ?: "Konto zostało zablokowane. Spróbuj ponownie za 15 minut."
                        )
                    } else {
                        AuthState.Error(e.message ?: "Błąd logowania")
                    }
                }
        }
    }

    /**
     * Pobiera token FCM i rejestruje urządzenie w backendzie.
     * Gdy FcmTokenProvider zwraca null (np. NoOpFcmTokenProvider), operacja jest pomijana.
     */
    private fun tryRegisterFcmToken() {
        viewModelScope.launch {
            try {
                val token = fcmTokenProvider.getToken()
                if (token != null) {
                    deviceService.registerDevice(token)
                } else {
                    Log.d(TAG, "FCM token niedostępny — pominięto rejestrację urządzenia")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Nie udało się zarejestrować FCM token: ${e.message}")
            }
        }
    }
}

