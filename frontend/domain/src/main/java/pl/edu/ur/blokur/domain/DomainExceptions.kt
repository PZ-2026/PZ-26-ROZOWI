package pl.edu.ur.blokur.domain

/** Rzucany przez stub UseCase'ów, które nie mają jeszcze implementacji. */
class UseCaseNotImplementedException(className: String?) : Exception(className + " is not implemented")

/**
 * Hierarchia wyjątków domenowych związanych z autoryzacją.
 *
 * Rzucane przez [pl.edu.ur.blokur.domain.repository.AuthRepository]
 * i propagowane przez UseCase'y aż do ViewModelu.
 */
sealed class AuthException(message: String) : Exception(message) {

    /** Błędny e-mail lub hasło (HTTP 401). */
    data object InvalidCredentials : AuthException("Nieprawidłowy e-mail lub hasło")

    /** Konto zablokowane po wielokrotnych próbach logowania (HTTP 423). */
    data object AccountLocked : AuthException("Konto zostało zablokowane. Spróbuj ponownie za 15 minut")

    /** Nieoczekiwany kod błędu HTTP. */
    data class ApiError(val code: Int) : AuthException("Błąd serwera: $code")

    /** Serwer zwrócił pustą odpowiedź mimo kodu 2xx. */
    data object EmptyResponse : AuthException("Brak danych w odpowiedzi serwera")

    /** Serwer zwrócił nierozpoznaną rolę. */
    data class UnknownRole(val role: String) : AuthException("Nieznana rola użytkownika: $role")
}