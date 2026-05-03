package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

// ── ScopeType enum ────────────────────────────────────────────────────────────

enum class ScopeType(val label: String) {
    NIERUCHOMOSC("Nieruchomość"),
    BUDYNEK("Budynek"),
    KLATKA("Klatka")
}

// ── Response ──────────────────────────────────────────────────────────────────

/** Przegląd techniczny zwracany przez API. */
data class InspectionResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("scheduledAt") val scheduledAt: String,   // "YYYY-MM-DDTHH:MM:SS"
    @SerializedName("scopeType") val scopeType: String,       // NIERUCHOMOSC | BUDYNEK | KLATKA
    @SerializedName("scopeId") val scopeId: String,
    @SerializedName("createdByName") val createdByName: String?,
    @SerializedName("createdAt") val createdAt: String?
) {
    val isUpcoming: Boolean
        get() = try {
            java.time.LocalDateTime.parse(scheduledAt).isAfter(java.time.LocalDateTime.now())
        } catch (_: Exception) { false }

    val scopeTypeLabel: String
        get() = when (scopeType) {
            "NIERUCHOMOSC" -> "Nieruchomość"
            "BUDYNEK" -> "Budynek"
            "KLATKA" -> "Klatka"
            else -> scopeType
        }
}

// ── Request ───────────────────────────────────────────────────────────────────

/** Ciało żądania tworzenia / aktualizacji przeglądu. */
data class InspectionRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("scheduledAt") val scheduledAt: String,   // "YYYY-MM-DDTHH:MM:SS"
    @SerializedName("scopeType") val scopeType: String,
    @SerializedName("scopeId") val scopeId: String
)
