package pl.edu.ur.blokur.infrastructure.api

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.domain.repository.PropertyStructureRepository
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Moduły Hilt spinające Retrofit oraz repozytorium panelu drzewa nieruchomości.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object PropertyStructureApiModule {

    /**
     * Dostarcza serwis Retrofit używany przez panel zarządcy.
     */
    @Provides
    @Singleton
    @Named("propertyStructure")
    fun providePropertyStructureApiService(
        @Named("main") retrofit: Retrofit
    ): PropertyStructureApiService = retrofit.create(PropertyStructureApiService::class.java)
}

/**
 * Wiąże implementację Retrofit z interfejsem domenowym.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class PropertyStructureRepositoryModule {

    /**
     * Rejestruje repozytorium drzewa nieruchomości w grafie Hilt.
     */
    @Binds
    @Singleton
    abstract fun bindPropertyStructureRepository(
        impl: RetrofitPropertyStructureRepository
    ): PropertyStructureRepository
}
