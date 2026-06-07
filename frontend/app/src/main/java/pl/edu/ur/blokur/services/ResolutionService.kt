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
        return ApiResponseHandler.requireSuccess(api.getResolutions(), "Błąd pobierania uchwał")
    }

    suspend fun getResolutionDetails(id: String): ResolutionDetailDto {
        return ApiResponseHandler.requireSuccess(api.getResolutionDetails(id), "Błąd pobierania szczegółów uchwały")
    }

    suspend fun createResolution(request: CreateResolutionRequest) {
        ApiResponseHandler.requireSuccessNoBody(api.createResolution(request), "Błąd tworzenia uchwały")
    }

    suspend fun castVote(resolutionId: String, optionId: String) {
        ApiResponseHandler.requireSuccessNoBody(api.castVote(resolutionId, CastVoteRequest(optionId = optionId)), "Błąd oddawania głosu")
    }

    /** Zwraca bajty PDF raportu. */
    suspend fun getResolutionReport(resolutionId: String): ByteArray {
        val resp = api.getResolutionReport(resolutionId)
        if (!resp.isSuccessful) throw Exception(ApiResponseHandler.mapHttpError(resp, "Błąd generowania raportu"))
        return resp.body()?.bytes() ?: throw Exception("Pusta odpowiedź PDF")
    }
}
