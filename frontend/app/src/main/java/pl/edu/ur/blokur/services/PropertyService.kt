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

    // ─── Pomocnicza metoda do obsługi błędów ─────────────────────────
    private fun <T> handleResponse(response: retrofit2.Response<T>, defaultErrorMessage: String): T {
        if (!response.isSuccessful) {
            val message = when (response.code()) {
                400 -> "Błąd walidacji danych. Sprawdź poprawność wprowadzonych informacji (np. NIP musi mieć dokładnie 10 cyfr, adres email musi być poprawny)."
                403 -> "Brak uprawnień do wykonania tej operacji."
                404 -> "Nie znaleziono wybranego zasobu."
                422 -> "Niezgodność danych (np. podany NIP jest już zarejestrowany dla innej nieruchomości)."
                else -> "$defaultErrorMessage (Kod: ${response.code()})"
            }
            throw Exception(message)
        }
        return response.body() ?: throw Exception("Pusta odpowiedź z serwera")
    }

    // ─── Tree ────────────────────────────────────────────────────────

    /** Pobiera pełne drzewo budynków z klatkami i lokalami. */
    suspend fun getBuildingTree(): List<BuildingTreeNodeDto> {
        val response = api.getBuildingTree()
        return handleResponse(response, "Błąd pobierania drzewa")
    }

    /** Pobiera listę nieruchomości (wspólnot). */
    suspend fun getProperties(): List<PropertyResponseDto> {
        val response = api.getProperties()
        return handleResponse(response, "Błąd pobierania nieruchomości")
    }

    // ─── Property CRUD ───────────────────────────────────────────────

    suspend fun createProperty(request: PropertyRequestDto): PropertyResponseDto {
        return handleResponse(api.createProperty(request), "Błąd tworzenia nieruchomości")
    }

    suspend fun updateProperty(id: String, request: PropertyRequestDto): PropertyResponseDto {
        return handleResponse(api.updateProperty(id, request), "Błąd edycji nieruchomości")
    }

    // ─── Building CRUD ───────────────────────────────────────────────

    suspend fun createBuilding(request: BuildingRequestDto): BuildingResponseDto {
        return handleResponse(api.createBuilding(request), "Błąd tworzenia budynku")
    }

    suspend fun updateBuilding(id: String, request: BuildingRequestDto): BuildingResponseDto {
        return handleResponse(api.updateBuilding(id, request), "Błąd edycji budynku")
    }

    suspend fun deleteBuilding(id: String) {
        val response = api.deleteBuilding(id)
        if (response.code() == 409) {
            throw Exception("Nie można usunąć budynku, ponieważ ma powiązane elementy (np. klatki lub lokale).")
        }
        handleResponse(response, "Błąd usuwania budynku")
    }

    // ─── Staircase CRUD ──────────────────────────────────────────────

    suspend fun createStaircase(buildingId: String, request: StaircaseRequestDto): StaircaseResponseDto {
        return handleResponse(api.createStaircase(buildingId, request), "Błąd tworzenia klatki")
    }

    suspend fun updateStaircase(buildingId: String, staircaseId: String, request: StaircaseRequestDto): StaircaseResponseDto {
        return handleResponse(api.updateStaircase(buildingId, staircaseId, request), "Błąd edycji klatki")
    }

    suspend fun deleteStaircase(buildingId: String, staircaseId: String) {
        val response = api.deleteStaircase(buildingId, staircaseId)
        if (response.code() == 409) {
            throw Exception("Nie można usunąć klatki, ponieważ ma powiązane lokale.")
        }
        handleResponse(response, "Błąd usuwania klatki")
    }

    // ─── Apartment CRUD ──────────────────────────────────────────────

    suspend fun createApartment(staircaseId: String, request: ApartmentRequestDto): ApartmentResponseDto {
        return handleResponse(api.createApartment(staircaseId, request), "Błąd tworzenia lokalu")
    }

    suspend fun updateApartment(staircaseId: String, apartmentId: String, request: ApartmentRequestDto): ApartmentResponseDto {
        return handleResponse(api.updateApartment(staircaseId, apartmentId, request), "Błąd edycji lokalu")
    }

    suspend fun deleteApartment(staircaseId: String, apartmentId: String) {
        val response = api.deleteApartment(staircaseId, apartmentId)
        if (response.code() == 409) {
            throw Exception("Nie można usunąć lokalu, jest on powiązany z istniejącymi umowami lub zgłoszeniami.")
        }
        handleResponse(response, "Błąd usuwania lokalu")
    }
}
