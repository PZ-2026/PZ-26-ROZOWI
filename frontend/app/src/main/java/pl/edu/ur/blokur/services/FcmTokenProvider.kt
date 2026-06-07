package pl.edu.ur.blokur.services

/**
 * Abstrakcja dostarczająca token FCM dla rejestracji powiadomień push.
 *
 * Domyślna implementacja [NoOpFcmTokenProvider] zwraca null (Firebase nie skonfigurowane).
 * Gdy Firebase zostanie dodany do projektu, należy wstrzyknąć [FirebaseFcmTokenProvider].
 */
interface FcmTokenProvider {
    /** Zwraca aktualny token FCM lub null jeśli niedostępny. */
    suspend fun getToken(): String?
}

/**
 * Implementacja zastępcza — używana gdy Firebase Messaging nie jest skonfigurowane.
 * Zwraca zawsze null; rejestracja push notifications jest pomijana.
 */
class NoOpFcmTokenProvider : FcmTokenProvider {
    override suspend fun getToken(): String? = null
}
