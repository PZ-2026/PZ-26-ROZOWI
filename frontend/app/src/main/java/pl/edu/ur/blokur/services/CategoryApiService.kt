package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AdminCategoryDto
import pl.edu.ur.blokur.dtos.CategoryCreateRequest
import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.dtos.SlaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Retrofit interface dla endpointów zarządzania kategoriami (dostęp: ZARZADCA). */
interface CategoryApiService {

    @POST("api/admin/categories")
    suspend fun createCategory(@Body request: CategoryCreateRequest): Response<AdminCategoryDto>

    @PUT("api/admin/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: CategoryCreateRequest
    ): Response<AdminCategoryDto>

    @PATCH("api/admin/categories/{id}/sla")
    suspend fun setSla(
        @Path("id") id: String,
        @Body request: SlaRequest
    ): Response<Unit>

    @PATCH("api/admin/categories/{id}/deactivate")
    suspend fun deactivateCategory(@Path("id") id: String): Response<Unit>
}
