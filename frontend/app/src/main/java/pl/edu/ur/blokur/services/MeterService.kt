package pl.edu.ur.blokur.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.ur.blokur.dtos.MeterReadingRequestDto
import pl.edu.ur.blokur.dtos.MeterReadingResponseDto
import pl.edu.ur.blokur.dtos.MeterRequestDto
import pl.edu.ur.blokur.dtos.MeterResponseDto
import pl.edu.ur.blokur.dtos.PaginatedResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeterService @Inject constructor(
    private val api: MeterApiService
) {
    suspend fun getMetersByApartment(apartmentId: String): List<MeterResponseDto> = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.getMetersByApartment(apartmentId), "Błąd pobierania liczników")
    }

    suspend fun createMeter(apartmentId: String, request: MeterRequestDto): MeterResponseDto = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.createMeter(apartmentId, request), "Błąd dodawania licznika")
    }

    suspend fun deactivateMeter(id: String): MeterResponseDto = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.deactivateMeter(id), "Błąd dezaktywacji licznika")
    }

    suspend fun getMeterReadingsByApartment(apartmentId: String, meterId: String? = null, page: Int = 0, size: Int = 15): PaginatedResponse<MeterReadingResponseDto> = withContext(Dispatchers.IO) {
        val body = ApiResponseHandler.requireSuccess(api.getMeterReadingsByApartment(apartmentId, 0, 1000), "Błąd pobierania odczytów")

        val filteredContent = if (meterId != null) {
            body.content.filter { it.meterId == meterId }
        } else {
            body.content
        }

        val start = page * size
        val end = minOf(start + size, filteredContent.size)
        val pagedContent = if (start < filteredContent.size) filteredContent.subList(start, end) else emptyList()
        val totalPages = (filteredContent.size + size - 1) / size

        PaginatedResponse(
            content = pagedContent,
            totalElements = filteredContent.size.toLong(),
            totalPages = if (totalPages == 0) 1 else totalPages,
            number = page,
            size = size
        )
    }

    suspend fun createMeterReading(apartmentId: String, request: MeterReadingRequestDto): MeterReadingResponseDto = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.createMeterReading(apartmentId, request), "Błąd dodawania odczytu")
    }

    suspend fun deleteMeterReading(id: String) = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccessNoBody(api.deleteMeterReading(id), "Błąd usuwania odczytu")
    }

    suspend fun getMeterReadingById(id: String): MeterReadingResponseDto = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.getMeterReadingById(id), "Błąd pobierania odczytu")
    }

    suspend fun updateMeterReading(id: String, request: MeterReadingRequestDto): MeterReadingResponseDto = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.updateMeterReading(id, request), "Błąd aktualizacji odczytu")
    }
}
