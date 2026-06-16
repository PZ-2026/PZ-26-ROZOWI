package pl.edu.ur.blokur.services

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Zarządza stanem sesji użytkownika — unieważnienie tokenów i sygnał wygaśnięcia sesji.
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    private val _forceRouteRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceRouteRefresh: SharedFlow<Unit> = _forceRouteRefresh.asSharedFlow()

    suspend fun invalidateSession() {
        tokenStorage.clearTokens()
        _sessionExpired.emit(Unit)
    }

    suspend fun forceRouteRefresh() {
        _forceRouteRefresh.emit(Unit)
    }
}
