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
        val resp = api.getAll()
        if (!resp.isSuccessful) throw Exception("Błąd pobierania przeglądów (${resp.code()})")
        return resp.body() ?: emptyList()
    }

    suspend fun create(request: InspectionRequestDto): InspectionResponseDto {
        val resp = api.create(request)
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                400 -> "Nieprawidłowe dane przeglądu."
                403 -> "Brak uprawnień do tworzenia przeglądów."
                404 -> "Nie znaleziono encji o podanym zasięgu."
                else -> "Błąd tworzenia przeglądu (${resp.code()})"
            }
        )
        return resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
    }

    suspend fun update(id: String, request: InspectionRequestDto): InspectionResponseDto {
        val resp = api.update(id, request)
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                400 -> "Nieprawidłowe dane przeglądu."
                403 -> "Brak uprawnień."
                404 -> "Przegląd lub zasięg nie istnieje."
                else -> "Błąd aktualizacji przeglądu (${resp.code()})"
            }
        )
        return resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
    }

    suspend fun delete(id: String) {
        val resp = api.delete(id)
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                403 -> "Brak uprawnień do usunięcia."
                404 -> "Przegląd nie istnieje."
                else -> "Błąd usuwania przeglądu (${resp.code()})"
            }
        )
    }
}
