package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AdminCategoryDto
import pl.edu.ur.blokur.dtos.CategoryCreateRequest
import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.dtos.SlaRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryService @Inject constructor(
    private val adminApi: CategoryApiService,
    private val ticketApi: TicketApiService  // do pobierania listy kategorii (GET /api/categories)
) {

    /** Pobiera listę aktywnych kategorii (dostępne dla wszystkich zalogowanych). */
    suspend fun getCategories(): List<CategoryDto> {
        return runCatching {
            ApiResponseHandler.requireSuccess(ticketApi.getCategories(), "Błąd pobierania kategorii")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Tworzy nową kategorię. Dostęp: ZARZADCA. */
    suspend fun createCategory(name: String): AdminCategoryDto {
        return runCatching {
            ApiResponseHandler.requireSuccess(adminApi.createCategory(CategoryCreateRequest(name.trim())), "Błąd tworzenia kategorii")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Aktualizuje istniejącą kategorię. Dostęp: ZARZADCA. */
    suspend fun updateCategory(id: String, name: String): AdminCategoryDto {
        return runCatching {
            ApiResponseHandler.requireSuccess(adminApi.updateCategory(id, CategoryCreateRequest(name.trim())), "Błąd aktualizacji kategorii")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Deaktywuje kategorię (soft delete). Dostęp: ZARZADCA. */
    suspend fun deactivateCategory(id: String) {
        runCatching {
            ApiResponseHandler.requireSuccessNoBody(adminApi.deactivateCategory(id), "Błąd deaktywacji kategorii")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Ustawia godziny SLA dla kategorii. Dostęp: ZARZADCA. */
    suspend fun setSla(id: String, hours: Int) {
        runCatching {
            ApiResponseHandler.requireSuccessNoBody(adminApi.setSla(id, SlaRequest(hours)), "Błąd ustawiania SLA")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
