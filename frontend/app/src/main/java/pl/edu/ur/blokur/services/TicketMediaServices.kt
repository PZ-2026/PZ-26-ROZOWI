package pl.edu.ur.blokur.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
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
        ApiResponseHandler.requireSuccess(api.getComments(ticketId), "Błąd pobierania komentarzy")
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
        ApiResponseHandler.requireSuccess(api.addComment(ticketId, TicketCommentRequestDto(content, commentType)), "Błąd dodawania komentarza")
    }
}

// ── TicketImageService ────────────────────────────────────────────────────────

@Singleton
class TicketImageService @Inject constructor(
    private val api: TicketImageApiService
) {
    /** GET /api/tickets/{id}/images — lista metadanych zdjęć do zgłoszenia. */
    suspend fun getImages(ticketId: String): List<TicketImageDto> = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.getImagesForTicket(ticketId), "Błąd pobierania zdjęć")
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
        // imageType przekazywany jako multipart form field (nie query param)
        val imageTypeBody: RequestBody = imageType.toRequestBody("text/plain".toMediaTypeOrNull())
        ApiResponseHandler.requireSuccess(api.uploadImage(ticketId, imageTypeBody, part), "Błąd wgrywania zdjęcia")
    }

    /** GET /api/images/{id} — pobiera surowe bajty zdjęcia. */
    suspend fun serveImage(imageId: String): ResponseBody = withContext(Dispatchers.IO) {
        ApiResponseHandler.requireSuccess(api.serveImage(imageId), "Błąd pobierania zdjęcia")
    }
}
