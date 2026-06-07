package pl.edu.ur.blokur.services

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module rejestrujący [FcmTokenProvider].
 * Używa [FirebaseFcmTokenProvider] — przy braku GMS zwraca null.
 */
@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    @Provides
    @Singleton
    fun provideFcmTokenProvider(
        firebaseProvider: FirebaseFcmTokenProvider
    ): FcmTokenProvider = firebaseProvider
}
