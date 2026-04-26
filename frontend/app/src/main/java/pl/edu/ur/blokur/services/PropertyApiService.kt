package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.*
import retrofit2.Response
import retrofit2.http.*

/** Retrofit interface dla endpointów nieruchomości, budynków, klatek i lokali. */
interface PropertyApiService {

    // ─── Tree ────────────────────────────────────────────────────────
    @GET("/api/buildings/tree")
    suspend fun getBuildingTree(): Response<List<BuildingTreeNodeDto>>

    // ─── Properties ──────────────────────────────────────────────────
    @GET("/api/properties")
    suspend fun getProperties(): Response<List<PropertyResponseDto>>

    @POST("/api/properties")
    suspend fun createProperty(@Body request: PropertyRequestDto): Response<PropertyResponseDto>

    @PUT("/api/properties/{id}")
    suspend fun updateProperty(
        @Path("id") id: String,
        @Body request: PropertyRequestDto
    ): Response<PropertyResponseDto>

    // ─── Buildings ───────────────────────────────────────────────────
    @POST("/api/buildings")
    suspend fun createBuilding(@Body request: BuildingRequestDto): Response<BuildingResponseDto>

    @PUT("/api/buildings/{id}")
    suspend fun updateBuilding(
        @Path("id") id: String,
        @Body request: BuildingRequestDto
    ): Response<BuildingResponseDto>

    // ─── Staircases ──────────────────────────────────────────────────
    @POST("/api/buildings/{buildingId}/staircases")
    suspend fun createStaircase(
        @Path("buildingId") buildingId: String,
        @Body request: StaircaseRequestDto
    ): Response<StaircaseResponseDto>

    @PUT("/api/buildings/{buildingId}/staircases/{staircaseId}")
    suspend fun updateStaircase(
        @Path("buildingId") buildingId: String,
        @Path("staircaseId") staircaseId: String,
        @Body request: StaircaseRequestDto
    ): Response<StaircaseResponseDto>

    // ─── Apartments ──────────────────────────────────────────────────
    @POST("/api/staircases/{staircaseId}/apartments")
    suspend fun createApartment(
        @Path("staircaseId") staircaseId: String,
        @Body request: ApartmentRequestDto
    ): Response<ApartmentResponseDto>

    @PUT("/api/staircases/{staircaseId}/apartments/{apartmentId}")
    suspend fun updateApartment(
        @Path("staircaseId") staircaseId: String,
        @Path("apartmentId") apartmentId: String,
        @Body request: ApartmentRequestDto
    ): Response<ApartmentResponseDto>
}
