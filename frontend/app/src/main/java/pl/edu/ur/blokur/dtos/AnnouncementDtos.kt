package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

// ── Enums ─────────────────────────────────────────────────────────────────────

/** Typ zasięgu ogłoszenia — odpowiada AnnouncementTargetType z backendu. */
enum class AnnouncementTargetType(val label: String) {
    WSZYSCY("Wszyscy"),
    BUDYNEK("Budynek"),
    KLATKA("Klatka"),
    LOKAL("Lokal")
}

// ── Response ──────────────────────────────────────────────────────────────────

/** GET /api/announcements — pojedyncze ogłoszenie. */
data class AnnouncementDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("authorName") val authorName: String?,
    @SerializedName("targetType") val targetType: String?,
    @SerializedName("attachmentUrl") val attachmentUrl: String?,   // URL do /api/announcements/{id}/attachment
    @SerializedName("plannedDate") val plannedDate: String?,       // ISO LocalDateTime
    @SerializedName("createdAt") val createdAt: String?
) {
    val hasAttachment: Boolean get() = !attachmentUrl.isNullOrBlank()
}

// ── Requests ──────────────────────────────────────────────────────────────────

/**
 * Ciało JSON dla POST/PUT /api/announcements (wysyłane jako part "data" w multipart).
 * Pole targetId wymagane gdy targetType != WSZYSCY.
 */
data class AnnouncementRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("targetType") val targetType: String,   // AnnouncementTargetType.name
    @SerializedName("targetId") val targetId: String?,
    @SerializedName("plannedDate") val plannedDate: String? // "YYYY-MM-DDTHH:MM:SS"
)
