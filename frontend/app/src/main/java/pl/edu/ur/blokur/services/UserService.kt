package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.UserProfileDto
import javax.inject.Inject
import javax.inject.Singleton

/** Serwis zarządzający pobieraniem danych profilowych użytkownika. */
@Singleton
class UserService @Inject constructor(
    private val api: UserApiService
) {

    /** Pobiera profil zalogowanego użytkownika z serwera. */
    suspend fun getMe(): UserProfileDto {
        return runCatching {
            ApiResponseHandler.requireSuccess(api.getMe(), "Błąd pobierania profilu użytkownika")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd pobierania profilu użytkownika") }
    }
}
