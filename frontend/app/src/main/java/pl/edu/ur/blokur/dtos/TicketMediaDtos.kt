package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

// ── Ticket Comments ───────────────────────────────────────────────────────────

/** Typ komentarza — odpowiada TicketCommentType z backendu. */
enum class TicketCommentType(val label: String) {
    PUBLICZNY("Publiczny"),
    WEWNETRZNY("Wewnętrzny")
}

/** GET /api/tickets/{id}/comments — pojedynczy komentarz. */
data class TicketCommentDto(
    @SerializedName("id") val id: String,
    @SerializedName("ticketId") val ticketId: String,
    @SerializedName("authorName") val authorName: String,
    @SerializedName("content") val content: String,
    @SerializedName("commentType") val commentType: String,   // PUBLICZNY | WEWNETRZNY
    @SerializedName("createdAt") val createdAt: String?
) {
    val isInternal: Boolean get() = commentType == "WEWNETRZNY"
    val commentTypeLabel: String
        get() = TicketCommentType.entries
            .firstOrNull { it.name == commentType }?.label ?: commentType
}

/** POST /api/tickets/{id}/comments — ciało żądania. */
data class TicketCommentRequestDto(
    @SerializedName("content") val content: String,
    @SerializedName("commentType") val commentType: String  // TicketCommentType.name
)

// ── Ticket Images ─────────────────────────────────────────────────────────────

/** Typ zdjęcia zgłoszenia — odpowiada TicketImageType z backendu. */
enum class TicketImageType(val label: String) {
    BEFORE("Przed pracami"),
    AFTER("Po pracach")
}

/** GET /api/tickets/{id}/images — pojedyncze zdjęcie. */
data class TicketImageDto(
    @SerializedName("id") val id: String,
    @SerializedName("ticketId") val ticketId: String,
    @SerializedName("uploaderId") val uploaderId: String?,
    @SerializedName("imageType") val imageType: String,   // BEFORE | AFTER
    @SerializedName("originalFilename") val originalFilename: String?,
    @SerializedName("uploadedAt") val uploadedAt: String?,
    @SerializedName("url") val url: String?               // URL do GET /api/images/{id}
) {
    val imageTypeLabel: String
        get() = TicketImageType.entries
            .firstOrNull { it.name == imageType }?.label ?: imageType
}
