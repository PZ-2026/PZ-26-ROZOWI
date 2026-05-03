package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.AdminUserDto
import pl.edu.ur.blokur.dtos.CreateAdminUserRequest
import pl.edu.ur.blokur.dtos.UpdateAdminUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit interface dla endpointów zarządzania użytkownikami (dostęp: ZARZADCA). */
interface AdminUserApiService {

    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<AdminUserDto>>

    @POST("api/admin/users")
    suspend fun createUser(@Body request: CreateAdminUserRequest): Response<AdminUserDto>

    @PATCH("api/admin/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateAdminUserRequest
    ): Response<AdminUserDto>

    @PATCH("api/admin/users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: String): Response<Unit>
}
