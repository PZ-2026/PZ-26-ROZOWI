package pl.edu.ur.blokur.infrastructure.logging

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.domain.services.LoggingService

@Module
@InstallIn(SingletonComponent::class)
abstract class DependencyInjector {

    @Binds
    abstract fun bindLoggingService(
        impl: LoggingServiceImpl
    ): LoggingService
}
//@Module
//@InstallIn(SingletonComponent::class)
//object DependencyInjector { // Zmień na 'object', jeśli używasz tylko @Provides
//
//    @Provides
//    fun provideLoggingService(): LoggingService {
//        // Tutaj ręcznie tworzysz instancję implementacji
//        return LoggingServiceImpl()
//    }
//}