package pl.edu.ur.blokur.infrastructure.logging

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.domain.services.LoggingService

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DependencyInjector {

    @Binds
    abstract fun bindLoggingService(
        impl: LoggingServiceImpl
    ): LoggingService
}