package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

/** Odpowiedź z danymi profilu zalogowanego użytkownika — GET /api/users/me */
data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("apartmentId") val apartmentId: String?
) {
    val fullName: String get() = "$firstName $lastName"
}
