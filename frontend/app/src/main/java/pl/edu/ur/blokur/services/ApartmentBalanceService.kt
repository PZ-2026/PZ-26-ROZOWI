package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.ApartmentBalanceItemDto
import javax.inject.Inject
import javax.inject.Singleton

/** Serwis pobierający zestawienie sald i zaległości lokali dla widoku zarządcy. */
@Singleton
class ApartmentBalanceService @Inject constructor(
    private val api: FinancialApiService
) {
    /**
     * Pobiera zestawienie sald lokali z opcjonalnym filtrowaniem.
     *
     * @param propertyId filtr po nieruchomości (null = wszystkie)
     * @param minDebt minimalna kwota zaległości PLN jako tekst (null = bez filtru)
     * @param minDaysOverdue minimalna liczba dni zalegania (null = bez filtru)
     * @param sort kierunek sortowania ("debt_asc" lub "debt_desc")
     */
    suspend fun getBalances(
        propertyId: String? = null,
        minDebt: String? = null,
        minDaysOverdue: Long? = null,
        sort: String = "debt_desc"
    ): List<ApartmentBalanceItemDto> {
        return runCatching {
            val resp = api.getApartmentBalances(propertyId, minDebt, minDaysOverdue, sort)
            if (!resp.isSuccessful) throw Exception(
                when (resp.code()) {
                    403 -> "Brak uprawnień do pobierania zestawienia."
                    else -> "Błąd pobierania zestawienia (${resp.code()})"
                }
            )
            resp.body() ?: emptyList()
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    /** Pobiera zestawienie sald w formacie PDF (bajty). */
    suspend fun getBalancesPdf(
        propertyId: String? = null,
        minDebt: String? = null,
        minDaysOverdue: Long? = null,
        sort: String = "debt_desc"
    ): ByteArray {
        val resp = api.getApartmentBalancesPdf(propertyId, minDebt, minDaysOverdue, sort)
        if (!resp.isSuccessful) throw Exception(
            when (resp.code()) {
                403 -> "Brak uprawnień do pobierania PDF."
                else -> "Błąd pobierania PDF (${resp.code()})"
            }
        )
        return resp.body()?.bytes() ?: throw Exception("Pusta odpowiedź PDF")
    }
}
