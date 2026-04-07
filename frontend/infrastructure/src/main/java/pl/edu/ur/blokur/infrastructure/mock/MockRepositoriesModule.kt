package pl.edu.ur.blokur.infrastructure.mock

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.domain.repository.FinancesRepository
import pl.edu.ur.blokur.domain.repository.TicketRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MockRepositoriesModule {

    @Binds
    @Singleton
    abstract fun bindTicketRepository(impl: MockTicketRepository): TicketRepository

    @Binds
    @Singleton
    abstract fun bindFinancesRepository(impl: MockFinancesRepository): FinancesRepository
}
