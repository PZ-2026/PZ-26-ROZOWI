package pl.edu.ur.blokur.infrastructure.api

import pl.edu.ur.blokur.infrastructure.api.dto.ApartmentRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.ApartmentResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.BuildingRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.BuildingResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.BuildingTreeDto
import pl.edu.ur.blokur.infrastructure.api.dto.PropertyRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.PropertyResponseDto
import pl.edu.ur.blokur.infrastructure.api.dto.StaircaseRequestDto
import pl.edu.ur.blokur.infrastructure.api.dto.StaircaseResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface PropertyStructureApiService {

    @GET("api/properties")
    suspend fun getProperties(): List<PropertyResponseDto>

    @GET("api/buildings/tree")
    suspend fun getBuildingTree(): List<BuildingTreeDto>

    @POST("api/properties")
    suspend fun createProperty(@Body request: PropertyRequestDto): PropertyResponseDto

    @PUT("api/properties/{id}")
    suspend fun updateProperty(
        @Path("id") propertyId: String,
        @Body request: PropertyRequestDto
    ): PropertyResponseDto

    @POST("api/buildings")
    suspend fun createBuilding(@Body request: BuildingRequestDto): BuildingResponseDto

    @PUT("api/buildings/{id}")
    suspend fun updateBuilding(
        @Path("id") buildingId: String,
        @Body request: BuildingRequestDto
    ): BuildingResponseDto

    @DELETE("api/buildings/{id}")
    suspend fun deleteBuilding(@Path("id") buildingId: String)

    @POST("api/buildings/{id}/staircases")
    suspend fun createStaircase(
        @Path("id") buildingId: String,
        @Body request: StaircaseRequestDto
    ): StaircaseResponseDto

    @PUT("api/buildings/{id}/staircases/{stId}")
    suspend fun updateStaircase(
        @Path("id") buildingId: String,
        @Path("stId") staircaseId: String,
        @Body request: StaircaseRequestDto
    ): StaircaseResponseDto

    @DELETE("api/buildings/{id}/staircases/{stId}")
    suspend fun deleteStaircase(
        @Path("id") buildingId: String,
        @Path("stId") staircaseId: String
    )

    @POST("api/staircases/{id}/apartments")
    suspend fun createApartment(
        @Path("id") staircaseId: String,
        @Body request: ApartmentRequestDto
    ): ApartmentResponseDto

    @PUT("api/staircases/{id}/apartments/{aptId}")
    suspend fun updateApartment(
        @Path("id") staircaseId: String,
        @Path("aptId") apartmentId: String,
        @Body request: ApartmentRequestDto
    ): ApartmentResponseDto

    @DELETE("api/staircases/{id}/apartments/{aptId}")
    suspend fun deleteApartment(
        @Path("id") staircaseId: String,
        @Path("aptId") apartmentId: String
    )
}
