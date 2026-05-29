package pl.edu.ur.blokur.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.edu.ur.blokur.dtos.TicketCommentDto
import pl.edu.ur.blokur.dtos.TicketCommentRequestDto
import pl.edu.ur.blokur.dtos.TicketImageDto
import javax.inject.Inject
import javax.inject.Singleton

// ── TicketCommentService ───────────────────────────────────────────────────────

@Singleton
class TicketCommentService @Inject constructor(
    private val api: TicketCommentApiService
) {
    /** GET /api/tickets/{id}/comments — lista komentarzy do zgłoszenia. */
    suspend fun getComments(ticketId: String): List<TicketCommentDto> = withContext(Dispatchers.IO) {
        val response = api.getComments(ticketId)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception(handleError(response.code(), "pobierania komentarzy"))
    }

    /**
     * POST /api/tickets/{id}/comments — dodaj komentarz.
     * @param commentType "PUBLICZNY" lub "WEWNETRZNY"
     */
    suspend fun addComment(
        ticketId: String,
        content: String,
        commentType: String = "PUBLICZNY"
    ): TicketCommentDto = withContext(Dispatchers.IO) {
        val response = api.addComment(ticketId, TicketCommentRequestDto(content, commentType))
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Pusta odpowiedź z serwera")
        } else {
            throw Exception(handleError(response.code(), "dodawania komentarza"))
        }
    }

    private fun handleError(code: Int, action: String): String = when (code) {
        403 -> "Brak uprawnień do $action."
        404 -> "Nie znaleziono zgłoszenia."
        else -> "Błąd serwera ($code) podczas $action."
    }
}

// ── TicketImageService ────────────────────────────────────────────────────────

@Singleton
class TicketImageService @Inject constructor(
    private val api: TicketImageApiService
) {
    /** GET /api/tickets/{id}/images — lista metadanych zdjęć do zgłoszenia. */
    suspend fun getImages(ticketId: String): List<TicketImageDto> = withContext(Dispatchers.IO) {
        val response = api.getImagesForTicket(ticketId)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception(handleError(response.code(), "pobierania zdjęć"))
    }

    /**
     * POST /api/tickets/{id}/images — wgraj zdjęcie (JPEG lub PNG).
     * @param imageType "BEFORE" lub "AFTER"
     * @param imageBytes bajty pliku
     * @param filename   oryginalna nazwa pliku (np. "photo.jpg")
     * @param mimeType   "image/jpeg" lub "image/png"
     */
    suspend fun uploadImage(
        ticketId: String,
        imageType: String,
        imageBytes: ByteArray,
        filename: String,
        mimeType: String = "image/jpeg"
    ): TicketImageDto = withContext(Dispatchers.IO) {
        val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", filename, requestBody)
        val response = api.uploadImage(ticketId, imageType, part)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Pusta odpowiedź z serwera")
        } else {
            throw Exception(handleError(response.code(), "wgrywania zdjęcia"))
        }
    }

    /** GET /api/images/{id} — pobiera surowe bajty zdjęcia. */
    suspend fun serveImage(imageId: String): ResponseBody = withContext(Dispatchers.IO) {
        val response = api.serveImage(imageId)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Brak treści odpowiedzi")
        } else {
            throw Exception(handleError(response.code(), "pobierania zdjęcia"))
        }
    }

    private fun handleError(code: Int, action: String): String = when (code) {
        403 -> "Brak uprawnień do $action."
        404 -> "Nie znaleziono zgłoszenia lub zdjęcia."
        413 -> "Plik jest zbyt duży."
        415 -> "Nieobsługiwany format pliku (dozwolone: JPEG, PNG)."
        else -> "Błąd serwera ($code) podczas $action."
    }
}
