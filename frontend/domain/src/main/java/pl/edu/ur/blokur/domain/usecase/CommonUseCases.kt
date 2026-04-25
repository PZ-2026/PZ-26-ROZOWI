package pl.edu.ur.blokur.domain.usecase

import pl.edu.ur.blokur.domain.model.UserRole
import pl.edu.ur.blokur.domain.repository.AuthRepository
import pl.edu.ur.blokur.domain.services.LoggingService
import javax.inject.Inject

/**
 * Wylogowuje użytkownika – usuwa tokeny z lokalnego magazynu.
 *
 * @property authRepository repozytorium autoryzacji wstrzykiwane przez Hilt.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /** Czyści tokeny sesji; po wykonaniu użytkownik musi zalogować się ponownie. */
    suspend operator fun invoke() = authRepository.logout()
}

/**
 * Zwraca rolę aktualnie zalogowanego użytkownika odczytaną z lokalnego magazynu.
 *
 * @property authRepository repozytorium autoryzacji wstrzykiwane przez Hilt.
 */
class GetCurrentUserRoleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * @return [UserRole] zalogowanego użytkownika lub `null` gdy sesja nie istnieje.
     */
    suspend operator fun invoke(): UserRole? = authRepository.getCurrentUserRole()
}

class TestAUseCase @Inject constructor(
    private val loggingService: LoggingService
) {
    suspend operator fun invoke(msg: String) {
        loggingService.LogMessage(msg)
    }
}

class TestBUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}

class TestCUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}

class TestDUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}

class TestEUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}