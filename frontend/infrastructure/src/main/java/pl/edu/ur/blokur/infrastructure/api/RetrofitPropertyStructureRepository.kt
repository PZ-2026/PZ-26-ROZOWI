package pl.edu.ur.blokur.infrastructure.api

import pl.edu.ur.blokur.domain.model.ApartmentDraft
import pl.edu.ur.blokur.domain.model.ApartmentOwnershipType
import pl.edu.ur.blokur.domain.model.BuildingDraft
import pl.edu.ur.blokur.domain.model.ManagedApartment
import pl.edu.ur.blokur.domain.model.ManagedBuilding
import pl.edu.ur.blokur.domain.model.ManagedProperty
import pl.edu.ur.blokur.domain.model.ManagedStaircase
import pl.edu.ur.blokur.domain.model.PropertyDraft
import pl.edu.ur.blokur.domain.model.StaircaseDraft
import pl.edu.ur.blokur.domain.repository.PropertyStructureRepository
import pl.edu.ur.blokur.infrastructure.api.dto.ApartmentRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.ApartmentResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.ApartmentTreeDto
import pl.edu.ur.blokur.infrastructure.api.dto.BuildingRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.BuildingResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.BuildingTreeDto
import pl.edu.ur.blokur.infrastructure.api.dto.PropertyRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.PropertyResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.StaircaseRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.StaircaseResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.StaircaseTreeDto
import javax.inject.Inject
import javax.inject.Named

/**
 * Backend nie zwraca identyfikatora wspólnoty w `GET /api/buildings/tree`, dlatego repozytorium
 * scala dane z `/api/properties` oraz `/api/buildings/tree`, grupując budynki po nazwie wspólnoty.
 */
internal class RetrofitPropertyStructureRepository @Inject constructor(
    @Named("propertyStructure") private val apiService: PropertyStructureApiService
) : PropertyStructureRepository {

    override suspend fun getPropertyTree(): List<ManagedProperty> {
        val properties = apiService.getProperties()
        val buildings = apiService.getBuildingTree()

        val buildingsByEstate = buildings.groupBy { it.estateName.trim() }
        val mappedProperties = properties.map { property ->
            property.toDomain(
                buildings = buildingsByEstate[property.name.trim()].orEmpty().map {
                    it.toDomain(property.id)
                }
            )
        }

        val orphanProperties = buildingsByEstate
            .filterKeys { estateName -> properties.none { it.name.trim() == estateName } }
            .map { (estateName, estateBuildings) ->
                ManagedProperty(
                    id = "",
                    name = estateName,
                    address = estateBuildings.firstOrNull()?.address.orEmpty(),
                    nip = "",
                    managerPhone = null,
                    managerEmail = null,
                    logoPath = null,
                    buildings = estateBuildings.map { it.toDomain(propertyId = null) }
                )
            }

        return (mappedProperties + orphanProperties).sortedBy { it.name.lowercase() }
    }

    override suspend fun createProperty(draft: PropertyDraft): ManagedProperty =
        apiService.createProperty(draft.toDto()).toDomain()

    override suspend fun updateProperty(propertyId: String, draft: PropertyDraft): ManagedProperty =
        apiService.updateProperty(propertyId, draft.toDto()).toDomain()

    override suspend fun createBuilding(draft: BuildingDraft): ManagedBuilding =
        apiService.createBuilding(draft.toDto()).toDomain()

    override suspend fun updateBuilding(buildingId: String, draft: BuildingDraft): ManagedBuilding =
        apiService.updateBuilding(buildingId, draft.toDto()).toDomain()

    override suspend fun deleteBuilding(buildingId: String) =
        apiService.deleteBuilding(buildingId)

    override suspend fun createStaircase(
        buildingId: String,
        draft: StaircaseDraft
    ): ManagedStaircase = apiService.createStaircase(buildingId, draft.toDto()).toDomain()

    override suspend fun updateStaircase(
        buildingId: String,
        staircaseId: String,
        draft: StaircaseDraft
    ): ManagedStaircase = apiService.updateStaircase(buildingId, staircaseId, draft.toDto()).toDomain()

    override suspend fun deleteStaircase(buildingId: String, staircaseId: String) =
        apiService.deleteStaircase(buildingId, staircaseId)

    override suspend fun createApartment(
        staircaseId: String,
        draft: ApartmentDraft
    ): ManagedApartment = apiService.createApartment(staircaseId, draft.toDto()).toDomain()

    override suspend fun updateApartment(
        staircaseId: String,
        apartmentId: String,
        draft: ApartmentDraft
    ): ManagedApartment = apiService.updateApartment(staircaseId, apartmentId, draft.toDto()).toDomain()

    override suspend fun deleteApartment(staircaseId: String, apartmentId: String) =
        apiService.deleteApartment(staircaseId, apartmentId)

    private fun PropertyResponseDto.toDomain(
        buildings: List<ManagedBuilding> = emptyList()
    ): ManagedProperty = ManagedProperty(
        id = id,
        name = name,
        address = address,
        nip = nip,
        managerPhone = managerPhone,
        managerEmail = managerEmail,
        logoPath = logoPath,
        buildings = buildings.sortedBy { it.name.lowercase() }
    )

    private fun BuildingResponseDto.toDomain(): ManagedBuilding = ManagedBuilding(
        id = id,
        propertyId = propertyId,
        estateName = estateName,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        staircases = emptyList()
    )

    private fun StaircaseResponseDto.toDomain(): ManagedStaircase = ManagedStaircase(
        id = id,
        buildingId = buildingId,
        label = label,
        apartments = emptyList()
    )

    private fun ApartmentResponseDto.toDomain(): ManagedApartment = ManagedApartment(
        id = id,
        staircaseId = staircaseId,
        number = number,
        floor = floor,
        areaM2 = areaM2,
        ownershipType = ownershipType.toOwnershipType(),
        currentBalance = currentBalance
    )

    private fun BuildingTreeDto.toDomain(propertyId: String?): ManagedBuilding = ManagedBuilding(
        id = id,
        propertyId = propertyId,
        estateName = estateName,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        staircases = staircases.map { it.toDomain(id) }.sortedBy { it.label.lowercase() }
    )

    private fun StaircaseTreeDto.toDomain(buildingId: String): ManagedStaircase = ManagedStaircase(
        id = id,
        buildingId = buildingId,
        label = label,
        apartments = apartments.map { it.toDomain(id) }.sortedBy { it.number.lowercase() }
    )

    private fun ApartmentTreeDto.toDomain(staircaseId: String): ManagedApartment = ManagedApartment(
        id = id,
        staircaseId = staircaseId,
        number = number,
        floor = floor,
        areaM2 = areaM2,
        ownershipType = ownershipType.toOwnershipType(),
        currentBalance = currentBalance
    )

    private fun PropertyDraft.toDto(): PropertyRequestDto = PropertyRequestDto(
        name = name.trim(),
        address = address.trim(),
        nip = nip.trim(),
        managerPhone = managerPhone?.trim()?.ifBlank { null },
        managerEmail = managerEmail?.trim()?.ifBlank { null }
    )

    private fun BuildingDraft.toDto(): BuildingRequestDto = BuildingRequestDto(
        estateName = estateName.trim(),
        name = name.trim(),
        address = address.trim(),
        latitude = latitude,
        longitude = longitude,
        propertyId = propertyId
    )

    private fun StaircaseDraft.toDto(): StaircaseRequestDto = StaircaseRequestDto(
        label = label.trim()
    )

    private fun ApartmentDraft.toDto(): ApartmentRequestDto = ApartmentRequestDto(
        number = number.trim(),
        floor = floor,
        areaM2 = areaM2,
        ownershipType = ownershipType?.name
    )

    private fun String?.toOwnershipType(): ApartmentOwnershipType? =
        ApartmentOwnershipType.entries.firstOrNull { it.name == this }
}
