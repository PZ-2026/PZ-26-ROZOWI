package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Building Tree (GET /api/buildings/tree) ─────────────────────────

/** Węzeł drzewa — budynek z klatkami i lokalami. */
data class BuildingTreeNodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("estateName") val estateName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: BigDecimal?,
    @SerializedName("longitude") val longitude: BigDecimal?,
    @SerializedName("propertyId") val propertyId: String?,
    @SerializedName("staircases") val staircases: List<StaircaseNodeDto>
)

/** Węzeł drzewa — klatka schodowa z lokalami. */
data class StaircaseNodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("apartments") val apartments: List<ApartmentNodeDto>
)

/** Węzeł drzewa — lokal. */
data class ApartmentNodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("number") val number: String,
    @SerializedName("floor") val floor: Int?,
    @SerializedName("areaM2") val areaM2: BigDecimal?,
    @SerializedName("ownershipType") val ownershipType: String?,
    @SerializedName("currentBalance") val currentBalance: BigDecimal?
)

// ─── Property (Wspólnota / Nieruchomość) ─────────────────────────────

/** GET /api/properties — odpowiedź. */
data class PropertyResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("nip") val nip: String?,
    @SerializedName("managerPhone") val managerPhone: String?,
    @SerializedName("managerEmail") val managerEmail: String?,
    @SerializedName("logoPath") val logoPath: String?
)

/** POST/PUT /api/properties — ciało żądania. */
data class PropertyRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("nip") val nip: String,
    @SerializedName("managerPhone") val managerPhone: String?,
    @SerializedName("managerEmail") val managerEmail: String?
)

// ─── Building ────────────────────────────────────────────────────────

/** POST/PUT /api/buildings — ciało żądania. */
data class BuildingRequestDto(
    @SerializedName("estateName") val estateName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: BigDecimal?,
    @SerializedName("longitude") val longitude: BigDecimal?,
    @SerializedName("propertyId") val propertyId: String?
)

/** POST/PUT /api/buildings — odpowiedź. */
data class BuildingResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("estateName") val estateName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: BigDecimal?,
    @SerializedName("longitude") val longitude: BigDecimal?,
    @SerializedName("propertyId") val propertyId: String?
)

// ─── Staircase ───────────────────────────────────────────────────────

/** POST/PUT staircases — ciało żądania. */
data class StaircaseRequestDto(
    @SerializedName("label") val label: String
)

/** POST/PUT staircases — odpowiedź. */
data class StaircaseResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("buildingId") val buildingId: String,
    @SerializedName("label") val label: String
)

// ─── Apartment ───────────────────────────────────────────────────────

/** POST/PUT apartments — ciało żądania. */
data class ApartmentRequestDto(
    @SerializedName("number") val number: String,
    @SerializedName("floor") val floor: Int?,
    @SerializedName("areaM2") val areaM2: BigDecimal?,
    @SerializedName("ownershipType") val ownershipType: String?
)

/** POST/PUT apartments — odpowiedź. */
data class ApartmentResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("staircaseId") val staircaseId: String,
    @SerializedName("number") val number: String,
    @SerializedName("floor") val floor: Int?,
    @SerializedName("areaM2") val areaM2: BigDecimal?,
    @SerializedName("ownershipType") val ownershipType: String?,
    @SerializedName("currentBalance") val currentBalance: BigDecimal?
)
