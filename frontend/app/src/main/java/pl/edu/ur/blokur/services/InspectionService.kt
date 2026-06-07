package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.InspectionRequestDto
import pl.edu.ur.blokur.dtos.InspectionResponseDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InspectionService @Inject constructor(
    private val api: InspectionApiService
) {
    suspend fun getAll(): List<InspectionResponseDto> {
        return ApiResponseHandler.requireSuccess(api.getAll(), "Błąd pobierania przeglądów")
    }

    suspend fun create(request: InspectionRequestDto): InspectionResponseDto {
        return ApiResponseHandler.requireSuccess(api.create(request), "Błąd tworzenia przeglądu")
    }

    suspend fun update(id: String, request: InspectionRequestDto): InspectionResponseDto {
        return ApiResponseHandler.requireSuccess(api.update(id, request), "Błąd aktualizacji przeglądu")
    }

    suspend fun delete(id: String) {
        ApiResponseHandler.requireSuccessNoBody(api.delete(id), "Błąd usuwania przeglądu")
    }
}
