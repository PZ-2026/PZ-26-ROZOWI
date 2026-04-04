package pl.edu.ur.blokur.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.data.UserPreferences
import pl.edu.ur.blokur.network.RetrofitClient
import pl.edu.ur.blokur.network.dto.LoginRequest
import pl.edu.ur.blokur.repository.AuthRepository

sealed class AuthState {
    object Idle : AuthState()

    object Loading : AuthState()

    object Success : AuthState()

    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(
        email: String,
        pass: String,
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(username = email, password = pass))
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Wystąpił nieznany błąd")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

object AuthViewModelFactory {
    fun provideFactory(context: Context): ViewModelProvider.Factory =
        viewModelFactory {
            initializer {
                val userPrefs = UserPreferences(context)
                val repository = AuthRepository(RetrofitClient.apiService, userPrefs)
                AuthViewModel(repository)
            }
        }
}
