package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.MeterReadingRequestDto
import pl.edu.ur.blokur.dtos.MeterReadingResponseDto
import pl.edu.ur.blokur.dtos.MeterRequestDto
import pl.edu.ur.blokur.dtos.MeterResponseDto
import pl.edu.ur.blokur.dtos.PaginatedResponse
import retrofit2.Response
import retrofit2.http.*

interface MeterApiService {

    // ── Liczniki ─────────────────────────────────────────────────────────────

    @GET("/api/apartments/{apartmentId}/meters")
    suspend fun getMetersByApartment(
        @Path("apartmentId") apartmentId: String
    ): Response<List<MeterResponseDto>>

    @POST("/api/apartments/{apartmentId}/meters")
    suspend fun createMeter(
        @Path("apartmentId") apartmentId: String,
        @Body request: MeterRequestDto
    ): Response<MeterResponseDto>

    @PATCH("/api/meters/{id}/deactivate")
    suspend fun deactivateMeter(
        @Path("id") id: String
    ): Response<MeterResponseDto>

    // ── Odczyty ─────────────────────────────────────────────────────────────

    @GET("/api/apartments/{apartmentId}/meter-readings")
    suspend fun getMeterReadingsByApartment(
        @Path("apartmentId") apartmentId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<PaginatedResponse<MeterReadingResponseDto>>

    @POST("/api/apartments/{apartmentId}/meter-readings")
    suspend fun createMeterReading(
        @Path("apartmentId") apartmentId: String,
        @Body request: MeterReadingRequestDto
    ): Response<MeterReadingResponseDto>

    @DELETE("/api/meter-readings/{id}")
    suspend fun deleteMeterReading(
        @Path("id") id: String
    ): Response<Unit>

    /**
     * GET /api/meter-readings/{id} — pobiera szczegóły konkretnego odczytu.
     * Dostęp: ZARZADCA (wszystkie), KONSERWATOR i MIESZKANIEC (w obrębie swojego lokalu).
     */
    @GET("/api/meter-readings/{id}")
    suspend fun getMeterReadingById(
        @Path("id") id: String
    ): Response<MeterReadingResponseDto>

    /**
     * PUT /api/meter-readings/{id} — edytuje istniejący odczyt (tylko własny lokal lub ZARZADCA).
     * Przykładowe zastosowanie: korekta błędnie wpisanej wartości przed migracjá danych.
     */
    @PUT("/api/meter-readings/{id}")
    suspend fun updateMeterReading(
        @Path("id") id: String,
        @Body request: MeterReadingRequestDto
    ): Response<MeterReadingResponseDto>
}
