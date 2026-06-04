package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.UserProfileDto
import retrofit2.Response
import retrofit2.http.GET

/** Retrofit interface dla endpointów zalogowanego użytkownika. */
interface UserApiService {
    @GET("/api/users/me")
    suspend fun getMe(): Response<UserProfileDto>
}
