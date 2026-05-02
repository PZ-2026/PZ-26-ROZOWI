package pl.edu.ur.blokur.dtos

/**
 * Pełna odpowiedź kategorii używana w panelu admina (zarządcy).
 * GET /api/categories zwraca tylko aktywne, ale admin widzi też nieaktywne
 * przez osobne zapytanie lub przez ogólny endpoint.
 */
data class AdminCategoryDto(
    val id: String,
    val name: String
)

/** Request do POST /api/admin/categories i PUT /api/admin/categories/{id} */
data class CategoryCreateRequest(
    val name: String
)

/** Request do PATCH /api/admin/categories/{id}/sla */
data class SlaRequest(
    val slaHours: Int
)
