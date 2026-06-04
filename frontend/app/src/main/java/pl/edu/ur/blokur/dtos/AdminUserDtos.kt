package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

// ─── Paging ────────────────────────────────────────────────────────────────

/** Generyczny model strony zwracany przez Spring Data */
data class PageDto<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("last") val last: Boolean,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("first") val first: Boolean,
    @SerializedName("numberOfElements") val numberOfElements: Int,
    @SerializedName("empty") val empty: Boolean
)

// ─── Admin Users ───────────────────────────────────────────────────────────

/** Odpowiedź z danymi użytkownika — GET /api/admin/users */
data class AdminUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("apartmentId") val apartmentId: String?
) {
    val fullName: String get() = "$firstName $lastName"
}

/** Request do POST /api/admin/users */
data class CreateAdminUserRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("apartmentId") val apartmentId: String
)

/** Request do PATCH /api/admin/users/{id} */
data class UpdateAdminUserRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("apartmentId") val apartmentId: String?
)
