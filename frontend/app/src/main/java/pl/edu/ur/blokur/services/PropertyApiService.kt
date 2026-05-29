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

    @GET("/api/properties/{id}")
    suspend fun getPropertyById(@Path("id") id: String): Response<PropertyResponseDto>

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

    /** DELETE /api/buildings/{id} — usuwa budynek (tylko gdy brak powiązanych lokali). ZARZADCA. */
    @DELETE("/api/buildings/{id}")
    suspend fun deleteBuilding(@Path("id") id: String): Response<Unit>

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

    /**
     * DELETE /api/buildings/{buildingId}/staircases/{staircaseId} —
     * Usuwa klatkę schodową z budynku. ZARZADCA.
     */
    @DELETE("/api/buildings/{buildingId}/staircases/{staircaseId}")
    suspend fun deleteStaircase(
        @Path("buildingId") buildingId: String,
        @Path("staircaseId") staircaseId: String
    ): Response<Unit>

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

    /**
     * DELETE /api/staircases/{staircaseId}/apartments/{apartmentId} —
     * Usuwa lokal. Historyczne zgłoszenia powiązane z lokalem pozostają nienaruszone. ZARZADCA.
     */
    @DELETE("/api/staircases/{staircaseId}/apartments/{apartmentId}")
    suspend fun deleteApartment(
        @Path("staircaseId") staircaseId: String,
        @Path("apartmentId") apartmentId: String
    ): Response<Unit>
}
