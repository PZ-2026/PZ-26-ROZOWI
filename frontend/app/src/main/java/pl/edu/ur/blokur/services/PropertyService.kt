package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis zarządzania nieruchomościami — pobieranie drzewa, CRUD na każdym poziomie hierarchii.
 */
@Singleton
class PropertyService @Inject constructor(
    private val api: PropertyApiService
) {

    /** Pobiera pełne drzewo budynków z klatkami i lokalami. */
    suspend fun getBuildingTree(): List<BuildingTreeNodeDto> {
        val response = api.getBuildingTree()
        return ApiResponseHandler.requireSuccess(response, "Błąd pobierania drzewa")
    }

    /** Pobiera listę nieruchomości (wspólnot). */
    suspend fun getProperties(): List<PropertyResponseDto> {
        val response = api.getProperties()
        return ApiResponseHandler.requireSuccess(response, "Błąd pobierania nieruchomości")
    }

    /** Pobiera szczegóły nieruchomości (wspólnoty) po ID. */
    suspend fun getPropertyById(id: String): PropertyResponseDto {
        val response = api.getPropertyById(id)
        return ApiResponseHandler.requireSuccess(response, "Błąd pobierania nieruchomości")
    }

    suspend fun createProperty(request: PropertyRequestDto): PropertyResponseDto {
        return ApiResponseHandler.requireSuccess(api.createProperty(request), "Błąd tworzenia nieruchomości")
    }

    suspend fun updateProperty(id: String, request: PropertyRequestDto): PropertyResponseDto {
        return ApiResponseHandler.requireSuccess(api.updateProperty(id, request), "Błąd edycji nieruchomości")
    }

    suspend fun createBuilding(request: BuildingRequestDto): BuildingResponseDto {
        return ApiResponseHandler.requireSuccess(api.createBuilding(request), "Błąd tworzenia budynku")
    }

    suspend fun updateBuilding(id: String, request: BuildingRequestDto): BuildingResponseDto {
        return ApiResponseHandler.requireSuccess(api.updateBuilding(id, request), "Błąd edycji budynku")
    }

    suspend fun deleteBuilding(id: String) {
        val response = api.deleteBuilding(id)
        if (response.code() == 409) {
            throw ApiException(
                "Nie można usunąć budynku, ponieważ ma powiązane elementy (np. klatki lub lokale).",
                409
            )
        }
        ApiResponseHandler.requireSuccess(response, "Błąd usuwania budynku")
    }

    suspend fun createStaircase(buildingId: String, request: StaircaseRequestDto): StaircaseResponseDto {
        return ApiResponseHandler.requireSuccess(api.createStaircase(buildingId, request), "Błąd tworzenia klatki")
    }

    suspend fun updateStaircase(buildingId: String, staircaseId: String, request: StaircaseRequestDto): StaircaseResponseDto {
        return ApiResponseHandler.requireSuccess(
            api.updateStaircase(buildingId, staircaseId, request),
            "Błąd edycji klatki"
        )
    }

    suspend fun deleteStaircase(buildingId: String, staircaseId: String) {
        val response = api.deleteStaircase(buildingId, staircaseId)
        if (response.code() == 409) {
            throw ApiException("Nie można usunąć klatki, ponieważ ma powiązane lokale.", 409)
        }
        ApiResponseHandler.requireSuccess(response, "Błąd usuwania klatki")
    }

    suspend fun createApartment(staircaseId: String, request: ApartmentRequestDto): ApartmentResponseDto {
        return ApiResponseHandler.requireSuccess(api.createApartment(staircaseId, request), "Błąd tworzenia lokalu")
    }

    suspend fun updateApartment(staircaseId: String, apartmentId: String, request: ApartmentRequestDto): ApartmentResponseDto {
        return ApiResponseHandler.requireSuccess(
            api.updateApartment(staircaseId, apartmentId, request),
            "Błąd edycji lokalu"
        )
    }

    suspend fun deleteApartment(staircaseId: String, apartmentId: String) {
        val response = api.deleteApartment(staircaseId, apartmentId)
        if (response.code() == 409) {
            throw ApiException(
                "Nie można usunąć lokalu, jest on powiązany z istniejącymi umowami lub zgłoszeniami.",
                409
            )
        }
        ApiResponseHandler.requireSuccess(response, "Błąd usuwania lokalu")
    }
}
