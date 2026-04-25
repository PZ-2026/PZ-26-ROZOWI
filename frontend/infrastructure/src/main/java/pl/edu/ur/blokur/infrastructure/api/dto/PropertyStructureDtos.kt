package pl.edu.ur.blokur.infrastructure.api.dto

/**
 * DTO wspólnoty zwracany przez backend.
 */
internal data class PropertyResponseDto(
    val id: String,
    val name: String,
    val address: String,
    val nip: String,
    val managerPhone: String?,
    val managerEmail: String?,
    val logoPath: String?
)

/**
 * DTO wspólnoty wysyłany podczas tworzenia i aktualizacji.
 */
internal data class PropertyRequestDto(
    val name: String,
    val address: String,
    val nip: String,
    val managerPhone: String?,
    val managerEmail: String?
)

/**
 * DTO budynku zwracany po operacji zapisu.
 */
internal data class BuildingResponseDto(
    val id: String,
    val propertyId: String?,
    val estateName: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?
)

/**
 * DTO budynku wysyłany podczas tworzenia i aktualizacji.
 */
internal data class BuildingRequestDto(
    val estateName: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val propertyId: String
)

/**
 * DTO klatki schodowej zwracany po operacji zapisu.
 */
internal data class StaircaseResponseDto(
    val id: String,
    val buildingId: String?,
    val label: String
)

/**
 * DTO klatki schodowej wysyłany podczas tworzenia i aktualizacji.
 */
internal data class StaircaseRequestDto(
    val label: String
)

/**
 * DTO lokalu zwracany po operacji zapisu.
 */
internal data class ApartmentResponseDto(
    val id: String,
    val staircaseId: String?,
    val number: String,
    val floor: Int?,
    val areaM2: Double?,
    val ownershipType: String?,
    val currentBalance: Double?
)

/**
 * DTO lokalu wysyłany podczas tworzenia i aktualizacji.
 */
internal data class ApartmentRequestDto(
    val number: String,
    val floor: Int?,
    val areaM2: Double?,
    val ownershipType: String?
)

/**
 * DTO zagregowanego drzewa budynku zwracanego przez backend.
 */
internal data class BuildingTreeDto(
    val id: String,
    val estateName: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val staircases: List<StaircaseTreeDto> = emptyList()
)

/**
 * DTO klatki schodowej używane w zagnieżdżonym drzewie budynku.
 */
internal data class StaircaseTreeDto(
    val id: String,
    val label: String,
    val apartments: List<ApartmentTreeDto> = emptyList()
)

/**
 * DTO lokalu używane w zagnieżdżonym drzewie budynku.
 */
internal data class ApartmentTreeDto(
    val id: String,
    val number: String,
    val floor: Int?,
    val areaM2: Double?,
    val ownershipType: String?,
    val currentBalance: Double?
)
