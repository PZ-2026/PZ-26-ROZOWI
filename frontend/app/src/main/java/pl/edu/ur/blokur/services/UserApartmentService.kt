package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.UserRole
import javax.inject.Inject
import javax.inject.Singleton

/** Informacja o lokalu przypisanym mieszkańcowi (rozwiązywana z API profilu). */
data class ResidentApartmentInfo(
    val apartmentId: String,
    val label: String
)

/** Brak możliwości ustalenia lokalu mieszkańca bez poprawnych danych profilowych. */
class UserApartmentException(message: String) : Exception(message)

/**
 * Ustalanie lokalu mieszkańca za pomocą dedykowanego endpointu profilu użytkownika:
 * GET /api/users/me.
 */
@Singleton
class UserApartmentService @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService
) {

    @Volatile
    private var cached: ResidentApartmentInfo? = null

    suspend fun resolveForResident(): ResidentApartmentInfo {
        cached?.let { return it }

        val role = authService.getCurrentUserRole()
        if (role != UserRole.MIESZKANIEC) {
            throw UserApartmentException("Usługa dostępna tylko dla roli MIESZKANIEC.")
        }

        val profile = userService.getMe()
        val apartmentId = profile.apartmentId
            ?: throw UserApartmentException("Brak przypisanego lokalu w profilu użytkownika.")

        val label = "Twój lokal"
        return ResidentApartmentInfo(apartmentId, label).also { cached = it }
    }

    fun clearCache() {
        cached = null
    }
}
