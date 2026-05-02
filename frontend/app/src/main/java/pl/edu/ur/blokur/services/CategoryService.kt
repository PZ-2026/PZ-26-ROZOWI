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
            val resp = ticketApi.getCategories()
            if (!resp.isSuccessful) throw Exception("Błąd pobierania kategorii (${resp.code()})")
            resp.body() ?: emptyList()
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Tworzy nową kategorię. Dostęp: ZARZADCA. */
    suspend fun createCategory(name: String): AdminCategoryDto {
        return runCatching {
            val resp = adminApi.createCategory(CategoryCreateRequest(name.trim()))
            if (!resp.isSuccessful) throw Exception(
                when (resp.code()) {
                    400 -> "Nieprawidłowa nazwa kategorii."
                    409 -> "Kategoria o tej nazwie już istnieje."
                    else -> "Błąd tworzenia kategorii (${resp.code()})"
                }
            )
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Aktualizuje istniejącą kategorię. Dostęp: ZARZADCA. */
    suspend fun updateCategory(id: String, name: String): AdminCategoryDto {
        return runCatching {
            val resp = adminApi.updateCategory(id, CategoryCreateRequest(name.trim()))
            if (!resp.isSuccessful) throw Exception(
                when (resp.code()) {
                    400 -> "Nieprawidłowa nazwa kategorii."
                    404 -> "Nie znaleziono kategorii."
                    409 -> "Kategoria o tej nazwie już istnieje."
                    else -> "Błąd aktualizacji kategorii (${resp.code()})"
                }
            )
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Deaktywuje kategorię (soft delete). Dostęp: ZARZADCA. */
    suspend fun deactivateCategory(id: String) {
        runCatching {
            val resp = adminApi.deactivateCategory(id)
            if (!resp.isSuccessful && resp.code() != 204) {
                throw Exception("Błąd deaktywacji kategorii (${resp.code()})")
            }
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
