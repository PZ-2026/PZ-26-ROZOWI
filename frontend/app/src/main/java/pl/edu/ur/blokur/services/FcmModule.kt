package pl.edu.ur.blokur.services

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module rejestrujący FcmTokenProvider.
 *
 * Obecnie używa NoOpFcmTokenProvider (zwraca null — brak Firebase w projekcie).
 * Gdy Firebase Messaging zostanie dodane do build.gradle:
 *   1. Dodaj zależność: implementation("com.google.firebase:firebase-messaging-ktx")
 *   2. Dodaj zależność: implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")
 *   3. Zastąp NoOpFcmTokenProvider implementacją używającą FirebaseMessaging.getInstance().token.await()
 */
@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    @Provides
    @Singleton
    fun provideFcmTokenProvider(): FcmTokenProvider = NoOpFcmTokenProvider()
}
