package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.ApartmentTransactionsDto
import pl.edu.ur.blokur.dtos.CreateTransactionRequest
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit interface dla transakcji finansowych lokalu. */
interface FinancialApiService {

    @GET("api/apartments/{apartmentId}/transactions")
    suspend fun getTransactions(
        @Path("apartmentId") apartmentId: String
    ): Response<ApartmentTransactionsDto>

    @POST("api/apartments/{apartmentId}/transactions")
    suspend fun createTransaction(
        @Path("apartmentId") apartmentId: String,
        @Body request: CreateTransactionRequest
    ): Response<FinancialTransactionDto>
}
