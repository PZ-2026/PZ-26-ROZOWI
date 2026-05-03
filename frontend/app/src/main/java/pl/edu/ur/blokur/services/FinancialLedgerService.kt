package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.ApartmentTransactionsDto
import pl.edu.ur.blokur.dtos.CreateTransactionRequest
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialLedgerService @Inject constructor(
    private val api: FinancialApiService
) {
    suspend fun getTransactions(apartmentId: String): ApartmentTransactionsDto {
        return runCatching {
            val resp = api.getTransactions(apartmentId)
            if (!resp.isSuccessful) throw Exception("Błąd pobierania transakcji (${resp.code()})")
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }

    suspend fun createTransaction(
        apartmentId: String,
        request: CreateTransactionRequest
    ): FinancialTransactionDto {
        return runCatching {
            val resp = api.createTransaction(apartmentId, request)
            if (!resp.isSuccessful) throw Exception(
                when (resp.code()) {
                    400 -> "Nieprawidłowe dane transakcji."
                    403 -> "Brak uprawnień do dodawania transakcji."
                    404 -> "Nie znaleziono lokalu."
                    else -> "Błąd tworzenia transakcji (${resp.code()})"
                }
            )
            resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
        }.getOrElse { throw Exception(it.message ?: "Błąd połączenia") }
    }
}
