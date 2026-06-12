package pl.edu.ur.blokur.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.services.TokenStorage

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TokenStorageEntryPoint {
    fun tokenStorage(): TokenStorage
}
