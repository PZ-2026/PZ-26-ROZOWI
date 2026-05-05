package pl.edu.ur.blokur.services

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.edu.ur.blokur.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module dostarczający klientów HTTP i serwisów Retrofit.
 *
 * Dwa osobne klienty OkHttp:
 * - "bare" — tylko logging (login, refresh)
 * - "main" — z AuthInterceptor + TokenAuthenticator (pozostałe żądania)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val BASE_URL get() = BuildConfig.BACKEND_URL

    @Provides
    @Singleton
    @Named("bare")
    fun provideBareOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("bare") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

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

    @Provides
    @Singleton
    @Named("main")
    fun provideMainRetrofit(@Named("main") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePropertyApiService(@Named("main") retrofit: Retrofit): PropertyApiService =
        retrofit.create(PropertyApiService::class.java)

    @Provides
    @Singleton
    fun provideTicketApiService(@Named("main") retrofit: Retrofit): TicketApiService =
        retrofit.create(TicketApiService::class.java)

    @Provides
    @Singleton
    fun provideCategoryApiService(@Named("main") retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    @Provides
    @Singleton
    fun provideAdminUserApiService(@Named("main") retrofit: Retrofit): AdminUserApiService =
        retrofit.create(AdminUserApiService::class.java)

    @Provides
    @Singleton
    fun provideFinancialApiService(@Named("main") retrofit: Retrofit): FinancialApiService =
        retrofit.create(FinancialApiService::class.java)

    @Provides
    @Singleton
    fun provideMeterApiService(@Named("main") retrofit: Retrofit): MeterApiService =
        retrofit.create(MeterApiService::class.java)
    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
}
