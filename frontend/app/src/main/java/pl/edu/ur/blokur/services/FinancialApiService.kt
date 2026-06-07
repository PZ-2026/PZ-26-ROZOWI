package pl.edu.ur.blokur.services

import okhttp3.MultipartBody
import pl.edu.ur.blokur.dtos.ApartmentBalanceItemDto
import pl.edu.ur.blokur.dtos.ApartmentTransactionsDto
import pl.edu.ur.blokur.dtos.CreateTransactionRequest
import pl.edu.ur.blokur.dtos.CsvImportResultDto
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    /** GET /api/admin/apartments/balances */
    @GET("api/admin/apartments/balances")
    suspend fun getApartmentBalances(
        @Query("propertyId") propertyId: String? = null,
        @Query("minDebt") minDebt: String? = null,
        @Query("minDaysOverdue") minDaysOverdue: Long? = null,
        @Query("sort") sort: String? = null
    ): Response<List<ApartmentBalanceItemDto>>

    /** POST /api/finance/import — masowy import transakcji z CSV */
    @Multipart
    @POST("api/finance/import")
    suspend fun importCsv(
        @Part file: MultipartBody.Part
    ): Response<CsvImportResultDto>
}

