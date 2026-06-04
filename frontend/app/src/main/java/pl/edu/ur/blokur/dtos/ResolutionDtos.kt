package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName

// ── Lista uchwał ──────────────────────────────────────────────────────────────

data class ResolutionDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("endDate") val endDate: String,          // ISO: "2026-05-10T12:00:00"
    @SerializedName("buildingId") val buildingId: String?,
    @SerializedName("authorName") val authorName: String?
) {
    /** Czy uchwała jest aktywna (data zakończenia w przyszłości). */
    val isActive: Boolean
        get() = try {
            java.time.LocalDateTime.parse(endDate).isAfter(java.time.LocalDateTime.now())
        } catch (_: Exception) { false }
}

// ── Szczegóły uchwały ─────────────────────────────────────────────────────────

data class ResolutionDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("buildingId") val buildingId: String?,
    @SerializedName("authorName") val authorName: String?,
    @SerializedName("options") val options: List<ResolutionOptionDto>,
    @SerializedName("results") val results: List<ResolutionOptionResultDto>?,
    @SerializedName("userVoted") val userVoted: Boolean?
) {
    val isActive: Boolean
        get() = try {
            java.time.LocalDateTime.parse(endDate).isAfter(java.time.LocalDateTime.now())
        } catch (_: Exception) { false }

    val totalVotes: Long get() = results?.sumOf { it.votesCount } ?: 0L
}

data class ResolutionOptionDto(
    @SerializedName("id") val id: String,
    @SerializedName("optionText") val optionText: String
)

data class ResolutionOptionResultDto(
    @SerializedName("optionId") val optionId: String,
    @SerializedName("optionText") val optionText: String,
    @SerializedName("votesCount") val votesCount: Long
)

// ── Żądania ───────────────────────────────────────────────────────────────────

data class CreateResolutionRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("endDate") val endDate: String,          // "YYYY-MM-DDTHH:MM:SS"
    @SerializedName("options") val options: List<String>,
    @SerializedName("targetBuildingId") val targetBuildingId: String
)

data class CastVoteRequest(
    @SerializedName("optionId") val optionId: String
)
