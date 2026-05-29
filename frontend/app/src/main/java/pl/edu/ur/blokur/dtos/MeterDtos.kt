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
    /**
     * Wartość odczytu jako BigDecimal — eliminuje błędy precyzji IEEE 754.
     * Gson serializuje BigDecimal jako liczba JSON (bez cudzysłowów).
     * Backend oczekuje BigDecimal — typy są teraz w pełni zgodne.
     */
    @SerializedName("value") val value: java.math.BigDecimal,
    @SerializedName("readingDate") val readingDate: String // YYYY-MM-DD
)


data class MeterReadingResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("apartmentId") val apartmentId: String,
    @SerializedName("meterId") val meterId: String,
    @SerializedName("meterSerialNumber") val meterSerialNumber: String,
    @SerializedName("mediumType") val mediumType: String,
    /**
     * Wartość odczytu licznika.
     *
     * Backend zwraca BigDecimal serializowany przez Jacksona jako liczba JSON.
     * Gson deserializuje ją do Double. Dla dokładnych obliczeń (np. finansowych)
     * użyj właściwości [asBigDecimal] zamiast pola [value].
     */
    @SerializedName("value") val value: Double,
    @SerializedName("readingDate") val readingDate: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("recordedBy") val recordedBy: String?
) {
    /**
     * Wartość odczytu jako BigDecimal — użyj zamiast [value] wszędzie gdzie
     * precyzja ma znaczenie (porównania, obliczenia różnic, wyświetlanie).
     */
    val asBigDecimal: java.math.BigDecimal
        get() = value.toBigDecimal()

    /** Wartość sformatowana jako łańcuch z maksymalnie 3 miejscami po przecinku. */
    val displayValue: String
        get() = asBigDecimal.stripTrailingZeros().toPlainString()
}

data class PaginatedResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int
)
