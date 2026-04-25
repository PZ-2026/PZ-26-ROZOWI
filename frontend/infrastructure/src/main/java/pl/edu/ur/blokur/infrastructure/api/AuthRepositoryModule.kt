package pl.edu.ur.blokur.infrastructure.api

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.ur.blokur.domain.repository.AuthRepository
import javax.inject.Singleton

/**
 * Hilt module wiążący interfejs [AuthRepository] z implementacją [RetrofitAuthRepository].
 *
 * Dzięki temu warstwa prezentacji i domenowa widzą wyłącznie interfejs –
 * podmiana implementacji (np. na mock do testów) wymaga tylko zmiany tego modułu.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthRepositoryModule {

    /**
     * Wiąże [RetrofitAuthRepository] z [AuthRepository] jako singleton w grafie DI.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RetrofitAuthRepository): AuthRepository
}
