package pl.edu.ur.blokur.services

import com.google.gson.Gson
import pl.edu.ur.blokur.dtos.MessageResponseDto
import retrofit2.Response

/**
 * Wspólna obsługa kodów HTTP i mapowanie na czytelne komunikaty po polsku.
 */
object ApiResponseHandler {

    private val gson = Gson()

    /**
     * Mapuje kod HTTP na komunikat użytkownika.
     *
     * @param retryAfterSeconds wartość nagłówka Retry-After (np. przy 429).
     */
    fun mapHttpError(
        code: Int,
        defaultMessage: String,
        retryAfterSeconds: Int? = null
    ): String = when (code) {
        400 -> "Błąd walidacji danych."
        401 -> "Sesja wygasła. Zaloguj się ponownie."
        403 -> "Brak uprawnień do wykonania tej operacji."
        404 -> "Nie znaleziono żądanego zasobu."
        409 -> "Operacja niedozwolona w aktualnym stanie."
        413 -> "Plik jest za duży. Maksymalny dopuszczalny rozmiar to 10 MB."
        422 -> "Niezgodność danych z regułami biznesowymi."
        423 -> "Konto tymczasowo zablokowane. Spróbuj ponownie później."
        429 -> {
            val waitHint = retryAfterSeconds?.let { " Spróbuj za $it s." } ?: ""
            "Zbyt wiele prób.$waitHint"
        }
        in 500..599 -> "Błąd serwera. Spróbuj ponownie później."
        else -> "$defaultMessage (Kod: $code)"
    }

    fun mapHttpError(response: Response<*>, defaultMessage: String): String {
        val code = response.code()
        if (code == 413) {
            return "Plik jest za duży. Maksymalny dopuszczalny rozmiar to 10 MB."
        }
        
        val retryAfter = response.headers()["Retry-After"]?.toIntOrNull()
        val bodyMessage = response.errorBody()?.string()?.let { parseJsonMessage(it) }
        return bodyMessage ?: mapHttpError(code, defaultMessage, retryAfter)
    }

    fun <T> requireSuccess(response: Response<T>, defaultMessage: String): T {
        if (!response.isSuccessful) {
            throw ApiException(mapHttpError(response, defaultMessage), response.code())
        }
        return response.body() ?: throw ApiException("Pusta odpowiedź z serwera", response.code())
    }

    fun requireSuccessNoBody(response: Response<*>, defaultMessage: String) {
        if (!response.isSuccessful) {
            throw ApiException(mapHttpError(response, defaultMessage), response.code())
        }
    }

    fun wrapException(cause: Throwable, defaultMessage: String = "Błąd połączenia"): Exception {
        return if (cause is ApiException) cause else Exception(cause.message ?: defaultMessage, cause)
    }

    fun parseJsonMessage(raw: String): String? = try {
        gson.fromJson(raw, MessageResponseDto::class.java).message?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

/** Wyjątek API z kodem HTTP — ułatwia obsługę w ViewModelach. */
class ApiException(
    override val message: String,
    val httpCode: Int
) : Exception(message)
