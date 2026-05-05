package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.CastVoteRequest
import pl.edu.ur.blokur.dtos.CreateResolutionRequest
import pl.edu.ur.blokur.dtos.ResolutionDetailDto
import pl.edu.ur.blokur.dtos.ResolutionDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolutionService @Inject constructor(
    private val api: ResolutionApiService
) {
    suspend fun getResolutions(): List<ResolutionDto> {
        val resp = api.getResolutions()
        if (!resp.isSuccessful) throw Exception("Błąd pobierania uchwał (${resp.code()})")
        return resp.body() ?: emptyList()
    }

    suspend fun getResolutionDetails(id: String): ResolutionDetailDto {
        val resp = api.getResolutionDetails(id)
        if (!resp.isSuccessful) throw Exception("Błąd pobierania szczegółów uchwały (${resp.code()})")
        return resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
    }

    suspend fun createResolution(request: CreateResolutionRequest) {
        val resp = api.createResolution(request)
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                400 -> "Nieprawidłowe dane uchwały."
                403 -> "Brak uprawnień do tworzenia uchwał."
                else -> "Błąd tworzenia uchwały (${resp.code()})"
            }
        )
    }

    suspend fun castVote(resolutionId: String, optionId: String) {
        val resp = api.castVote(resolutionId, CastVoteRequest(optionId = optionId))
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                409 -> "Głos już został oddany w tej uchwale."
                403 -> "Nie masz uprawnień do głosowania."
                404 -> "Uchwała nie istnieje."
                else -> "Błąd oddawania głosu (${resp.code()})"
            }
        )
    }

    /** Zwraca bajty PDF raportu. */
    suspend fun getResolutionReport(resolutionId: String): ByteArray {
        val resp = api.getResolutionReport(resolutionId)
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                403 -> "Brak uprawnień do raportu."
                404 -> "Uchwała nie istnieje."
                else -> "Błąd generowania raportu (${resp.code()})"
            }
        )
        return resp.body()?.bytes() ?: throw Exception("Pusta odpowiedź PDF")
    }
}
