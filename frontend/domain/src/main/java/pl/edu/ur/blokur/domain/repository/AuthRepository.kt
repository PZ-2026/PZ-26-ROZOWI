package pl.edu.ur.blokur.domain.repository

import pl.edu.ur.blokur.domain.model.UserRole

/**
 * Kontrakt dostępu do zasobów autoryzacyjnych.
 *
 * Warstwa infrastruktury dostarcza implementację opartą o REST API (Retrofit + DataStore).
 * Domena nie wie nic o szczegółach sieciowych ani sposobie przechowywania tokenów.
 */
interface AuthRepository {

    /**
     * Uwierzytelnia użytkownika i zapisuje tokeny sesji (access + refresh).
     *
     * @param email    adres e-mail użytkownika (pole `username` w API).
     * @param password hasło użytkownika.
     * @return [UserRole] zdekodowana z odpowiedzi serwera.
     * @throws pl.edu.ur.blokur.domain.AuthException.InvalidCredentials gdy dane są błędne (HTTP 401).
     * @throws pl.edu.ur.blokur.domain.AuthException.AccountLocked gdy konto jest zablokowane (HTTP 423).
     * @throws pl.edu.ur.blokur.domain.AuthException.ApiError przy innym błędzie HTTP.
     */
    suspend fun login(email: String, password: String): UserRole

    /**
     * Wylogowuje użytkownika – usuwa wszystkie tokeny z lokalnego magazynu.
     */
    suspend fun logout()

    /**
     * Zwraca rolę aktualnie zalogowanego użytkownika lub `null`, gdy sesja nie istnieje.
     */
    suspend fun getCurrentUserRole(): UserRole?
}
