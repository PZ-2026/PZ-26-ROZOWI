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
            ApiResponseHandler.requireSuccess(api.getTransactions(apartmentId), "Błąd pobierania transakcji")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd pobierania transakcji") }
    }

    suspend fun createTransaction(
        apartmentId: String,
        request: CreateTransactionRequest
    ): FinancialTransactionDto {
        return runCatching {
            ApiResponseHandler.requireSuccess(api.createTransaction(apartmentId, request), "Błąd tworzenia transakcji")
        }.getOrElse { throw ApiResponseHandler.wrapException(it, "Błąd tworzenia transakcji") }
    }
}
