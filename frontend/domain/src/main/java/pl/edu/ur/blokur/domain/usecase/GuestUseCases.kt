package pl.edu.ur.blokur.domain.usecase

import pl.edu.ur.blokur.domain.UseCaseNotImplementedException
import pl.edu.ur.blokur.domain.model.UserRole
import pl.edu.ur.blokur.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Uwierzytelnia użytkownika i zwraca jego rolę w systemie.
 *
 * Orkiestruje wywołanie [AuthRepository.login] i przekazuje wynik do ViewModelu
 * bez ujawniania szczegółów infrastruktury.
 *
 * @property authRepository repozytorium autoryzacji wstrzykiwane przez Hilt.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * @param email    adres e-mail użytkownika.
     * @param password hasło użytkownika.
     * @return [UserRole] zalogowanego użytkownika.
     * @throws pl.edu.ur.blokur.domain.AuthException w przypadku błędu logowania.
     */
    suspend operator fun invoke(email: String, password: String): UserRole =
        authRepository.login(email, password)
}

/**
 * Inicjuje proces resetowania hasła – wysyła link na adres e-mail.
 *
 * @property authRepository repozytorium autoryzacji wstrzykiwane przez Hilt.
 */
class PasswordResetUseCase @Inject constructor() {
    /** @throws UseCaseNotImplementedException zawsze – stub oczekujący na implementację. */
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

/**
 * Ustawia nowe hasło po weryfikacji tokena z e-maila.
 *
 * @property authRepository repozytorium autoryzacji wstrzykiwane przez Hilt.
 */
class SetNewPasswordUseCase @Inject constructor() {
    /** @throws UseCaseNotImplementedException zawsze – stub oczekujący na implementację. */
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

/**
 * Wysyła link do resetu hasła na podany adres e-mail.
 */
class SendPasswordResetLinkUseCase @Inject constructor() {
    /** @throws UseCaseNotImplementedException zawsze – stub oczekujący na implementację. */
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}
