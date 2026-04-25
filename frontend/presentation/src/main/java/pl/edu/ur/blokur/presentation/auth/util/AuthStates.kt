package pl.edu.ur.blokur.presentation.auth.util

import pl.edu.ur.blokur.domain.model.UserRole

/**
 * Stany ekranu logowania.
 *
 * Obserwowane przez [pl.edu.ur.blokur.presentation.auth.screen.LoginScreen]
 * przez `StateFlow` z ViewModelu.
 */
sealed interface AuthState {
    /** Formularz bezczynny – gotowy do wprowadzenia danych. */
    data object Idle : AuthState

    /** Trwa żądanie do serwera – formularz jest zablokowany. */
    data object Loading : AuthState

    /** Logowanie zakończone sukcesem. */
    data object Success : AuthState

    /** Logowanie zakończone błędem. */
    data class Error(val message: String) : AuthState
}

/**
 * Jednorazowe zdarzenia nawigacyjne i feedback emitowane przez AuthViewModel.
 *
 * Przesyłane przez `Channel` – każde zdarzenie odbierane jest dokładnie raz.
 */
sealed interface AuthEvent {

    /**
     * Logowanie powiodło się – przekieruj do odpowiedniego panelu.
     *
     * @property role rola zdekodowana z odpowiedzi serwera, decyduje o docelowym ekranie.
     */
    data class NavigateToMain(val role: UserRole) : AuthEvent

    /** Wyświetl błąd w Snackbarze (alternatywna ścieżka do [AuthState.Error]). */
    data class ShowError(val message: String) : AuthEvent
}

/**
 * Stan pól formularza logowania.
 *
 * Immutable data class – każda zmiana tworzy nową kopię przez `copy()`.
 *
 * @property email           wpisany adres e-mail.
 * @property password        wpisane hasło.
 * @property passwordVisible czy hasło jest widoczne w polu tekstowym.
 */
data class LoginFormFields(
    val email: String,
    val password: String,
    val passwordVisible: Boolean
)
