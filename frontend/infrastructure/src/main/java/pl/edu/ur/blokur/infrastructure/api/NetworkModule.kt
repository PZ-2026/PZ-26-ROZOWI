package pl.edu.ur.blokur.infrastructure.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module dostarczający klientów HTTP i serwisów Retrofit.
 *
 * Architektura sieciowa:
 * ```
 * "bare" OkHttpClient  ──►  "auth" AuthApiService  (login, refresh – bez interceptorów auth)
 *                                     ▲
 *                                     │ używa
 *                              TokenAuthenticator
 *                                     │
 * "main" OkHttpClient  ──► AuthInterceptor + TokenAuthenticator  (pozostałe żądania)
 * ```
 *
 * Dwa osobne klienty OkHttp eliminują kołową zależność:
 * `OkHttpClient → TokenAuthenticator → AuthApiService → OkHttpClient`.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    /**
     * Adres bazowy backendu – wstrzykiwany z `local.properties` przez BuildConfig.
     * Każdy developer ustawia `backend.url` lokalnie (plik jest gitignorowany).
     */
    private val BASE_URL get() = pl.edu.ur.blokur.infrastructure.BuildConfig.BACKEND_URL

    // ─── Bare OkHttpClient (tylko logging) ───────────────────────────────────

    @Provides
    @Singleton
    @Named("bare")
    fun provideBareOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    // ─── Auth Retrofit + AuthApiService (używa bare klienta) ─────────────────

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("bare") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Serwis używany wyłącznie do logowania i odświeżania tokenów.
     * Nie posiada interceptora dodającego Bearer token.
     */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    // ─── Główny OkHttpClient (z interceptorami auth) ──────────────────────────

    /**
     * Główny klient OkHttp z [AuthInterceptor] (dodaje Bearer token) i
     * [TokenAuthenticator] (odświeża token przy HTTP 401).
     */
    @Provides
    @Singleton
    @Named("main")
    fun provideMainOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    // ─── Główny Retrofit (używa main klienta) ─────────────────────────────────

    /**
     * Główna instancja Retrofit do wszystkich endpointów poza auth.
     * Wstrzykiwana przez `@Named("main")` w pozostałych repozytoriach.
     */
    @Provides
    @Singleton
    @Named("main")
    fun provideMainRetrofit(@Named("main") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
}
