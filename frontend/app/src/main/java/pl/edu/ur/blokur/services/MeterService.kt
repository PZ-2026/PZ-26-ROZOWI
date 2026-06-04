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
        val response = api.getMetersByApartment(apartmentId)
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception(handleError(response.code(), "pobierania liczników"))
        }
    }

    suspend fun createMeter(apartmentId: String, request: MeterRequestDto): MeterResponseDto = withContext(Dispatchers.IO) {
        val response = api.createMeter(apartmentId, request)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak danych w odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "dodawania licznika"))
        }
    }

    suspend fun deactivateMeter(id: String): MeterResponseDto = withContext(Dispatchers.IO) {
        val response = api.deactivateMeter(id)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak danych w odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "dezaktywacji licznika"))
        }
    }

    suspend fun getMeterReadingsByApartment(apartmentId: String, page: Int = 0, size: Int = 50): PaginatedResponse<MeterReadingResponseDto> = withContext(Dispatchers.IO) {
        val response = api.getMeterReadingsByApartment(apartmentId, page, size)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Pusta odpowiedź z serwera")
        } else {
            throw Exception(handleError(response.code(), "pobierania odczytów"))
        }
    }

    suspend fun createMeterReading(apartmentId: String, request: MeterReadingRequestDto): MeterReadingResponseDto = withContext(Dispatchers.IO) {
        val response = api.createMeterReading(apartmentId, request)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak danych w odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "dodawania odczytu"))
        }
    }

    suspend fun deleteMeterReading(id: String) = withContext(Dispatchers.IO) {
        val response = api.deleteMeterReading(id)
        if (!response.isSuccessful) {
            throw Exception(handleError(response.code(), "usuwania odczytu"))
        }
    }

    suspend fun getMeterReadingById(id: String): MeterReadingResponseDto = withContext(Dispatchers.IO) {
        val response = api.getMeterReadingById(id)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak danych w odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "pobierania odczytu"))
        }
    }

    suspend fun updateMeterReading(id: String, request: MeterReadingRequestDto): MeterReadingResponseDto = withContext(Dispatchers.IO) {
        val response = api.updateMeterReading(id, request)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak danych w odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "aktualizacji odczytu"))
        }
    }

    private fun handleError(code: Int, action: String): String = when (code) {
        400 -> "Błędne dane podczas $action."
        401 -> "Brak autoryzacji. Zaloguj się ponownie."
        403 -> "Brak uprawnień do $action."
        404 -> "Nie znaleziono powiązanego lokalu lub licznika."
        else -> "Wystąpił błąd serwera ($code) podczas $action."
    }
}
