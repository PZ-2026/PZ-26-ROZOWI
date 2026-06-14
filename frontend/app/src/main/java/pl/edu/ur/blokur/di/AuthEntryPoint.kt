package pl.edu.ur.blokur.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.services.AuthService

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthEntryPoint {
    fun authService(): AuthService
}
