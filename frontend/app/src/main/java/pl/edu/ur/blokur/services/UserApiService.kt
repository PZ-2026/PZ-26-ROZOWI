package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.UserProfileDto
import retrofit2.Response
import retrofit2.http.GET

/** Retrofit interface dla operacji na profilu zalogowanego użytkownika. */
interface UserApiService {

    /** Pobiera profil zalogowanego użytkownika. */
    @GET("api/users/me")
    suspend fun getMe(): Response<UserProfileDto>
}
