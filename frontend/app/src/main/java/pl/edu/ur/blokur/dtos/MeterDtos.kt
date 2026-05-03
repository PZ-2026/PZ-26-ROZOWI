package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

enum class MediumType(val label: String) {
    ZIMNA_WODA("Zimna woda"),
    CIEPLA_WODA("Ciepła woda"),
    GAZ("Gaz"),
    CIEPLO("Ciepło")
}

// ── Liczniki ──────────────────────────────────────────────────────────────────

data class MeterRequestDto(
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("mediumType") val mediumType: String,
    @SerializedName("installationDate") val installationDate: String // YYYY-MM-DD
)

data class MeterResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("apartmentId") val apartmentId: String,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("mediumType") val mediumType: String,
    @SerializedName("installationDate") val installationDate: String, // YYYY-MM-DD
    @SerializedName("active") val active: Boolean
) {
    val mediumTypeLabel: String
        get() = try {
            MediumType.valueOf(mediumType).label
        } catch (_: Exception) { mediumType }
}

// ── Odczyty ───────────────────────────────────────────────────────────────────

data class MeterReadingRequestDto(
    @SerializedName("meterId") val meterId: String,
    @SerializedName("value") val value: Double,
    @SerializedName("readingDate") val readingDate: String // YYYY-MM-DD
)

data class MeterReadingResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("apartmentId") val apartmentId: String,
    @SerializedName("meterId") val meterId: String,
    @SerializedName("meterSerialNumber") val meterSerialNumber: String,
    @SerializedName("mediumType") val mediumType: String,
    @SerializedName("value") val value: Double,
    @SerializedName("readingDate") val readingDate: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("recordedBy") val recordedBy: String?
)

data class PaginatedResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int
)
