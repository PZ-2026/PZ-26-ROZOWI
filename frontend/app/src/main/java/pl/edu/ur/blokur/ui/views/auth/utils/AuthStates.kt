package pl.edu.ur.blokur.ui.views.auth.utils

import pl.edu.ur.blokur.dtos.UserRole

/**
 * Stany ekranu logowania.
 */
sealed interface AuthState {
    /** Formularz bezczynny – gotowy do wprowadzenia danych. */
    data object Idle : AuthState

    /** Trwa żądanie do serwera – formularz jest zablokowany. */
    data object Loading : AuthState

    /** Logowanie zakończone sukcesem. */
    data object Success : AuthState

    /** Logowanie zakończone błędem (np. złe hasło, błąd sieciowy). */
    data class Error(val message: String) : AuthState

    /**
     * Konto zablokowane (HTTP 423) — użytkownik przekroczył limit nieudanych prób.
     * UI powinno wyświetlić ikony kłódki i informację o czasie odblokowania.
     * @param message komunikat z serwera (np. "Konto zostało zablokowane na 15 minut")
     */
    data class AccountLocked(val message: String) : AuthState
}

/**
 * Jednorazowe zdarzenia nawigacyjne i feedback emitowane przez AuthViewModel.
 */
sealed interface AuthEvent {
    data class NavigateToMain(val role: UserRole) : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}

/**
 * Stan pól formularza logowania.
 */
data class LoginFormFields(
    val email: String,
    val password: String,
    val passwordVisible: Boolean
)

// ─── Zapomniałem hasła ───────────────────────────────────────────────

/** Stan ekranu „Zapomniałem hasła". */
sealed interface ForgotPasswordState {
    data object Idle : ForgotPasswordState
    data object Loading : ForgotPasswordState
    data class Success(val message: String) : ForgotPasswordState
    data class Error(val message: String) : ForgotPasswordState
}

/** Zdarzenia ekranu „Zapomniałem hasła". */
sealed interface ForgotPasswordEvent {
    data object NavigateBack : ForgotPasswordEvent
    data class ShowSnackbar(val message: String) : ForgotPasswordEvent
}

/** Pole formularza „Zapomniałem hasła". */
data class ForgotPasswordFormFields(
    val email: String = ""
)

// ─── Resetowanie hasła ───────────────────────────────────────────────

/** Stan ekranu „Resetuj hasło". */
sealed interface ResetPasswordState {
    data object Idle : ResetPasswordState
    data object Loading : ResetPasswordState
    data class Success(val message: String) : ResetPasswordState
    data class Error(val message: String) : ResetPasswordState
    /** Token z linku mailowego wygasł — użytkownik może poprosić o nowy link. */
    data class TokenExpired(val message: String) : ResetPasswordState
}

/** Zdarzenia ekranu „Resetuj hasło". */
sealed interface ResetPasswordEvent {
    data object NavigateToLogin : ResetPasswordEvent
    data object NavigateToForgotPassword : ResetPasswordEvent
    data class ShowSnackbar(val message: String) : ResetPasswordEvent
}

/** Pola formularza resetowania hasła. */
data class ResetPasswordFormFields(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false
)

// ─── Akceptacja zaproszenia ──────────────────────────────────────────

/** Stan ekranu „Akceptuj zaproszenie”. */
sealed interface AcceptInvitationState {
    data object Idle : AcceptInvitationState
    data object Loading : AcceptInvitationState
    data class Success(val message: String) : AcceptInvitationState
    data class Error(val message: String) : AcceptInvitationState
    /** Link zaproszenia wygasł. */
    data class TokenExpired(val message: String) : AcceptInvitationState
}

/** Zdarzenia ekranu „Akceptuj zaproszenie”. */
sealed interface AcceptInvitationEvent {
    data object NavigateToLogin : AcceptInvitationEvent
    data class ShowSnackbar(val message: String) : AcceptInvitationEvent
}

/** Pola formularza akceptacji zaproszenia. */
data class AcceptInvitationFormFields(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false
)

