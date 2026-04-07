package pl.edu.ur.blokur.presentation.auth.util

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Success : AuthState
    data class Error(val message: String) : AuthState
}

sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}

//tutaj moze jakos inaczej to trzymac chociaz poki co jest chyba okej
data class LoginFormFields (
    val email: String,
    val password: String,
    val passwordVisible: Boolean
)