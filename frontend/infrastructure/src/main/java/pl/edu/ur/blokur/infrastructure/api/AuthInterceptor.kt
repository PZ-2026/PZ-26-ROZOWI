package pl.edu.ur.blokur.infrastructure.api

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor dodający nagłówek `Authorization: Bearer <token>` do każdego żądania.
 *
 * Dołączany do głównego [okhttp3.OkHttpClient] (z pełnym stosem auth).
 * Endpointy `/api/auth/login` i `/api/auth/refresh` używają odrębnego klienta
 * bez tego interceptora, więc nie będą modyfikowane.
 *
 * `runBlocking` jest tutaj bezpieczny – interceptor działa na wątku sieciowym OkHttp,
 * a nie na głównym wątku Androida.
 *
 * @property tokenStorage lokalny magazyn tokenów JWT.
 */
internal class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStorage.getAccessToken() }

        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
