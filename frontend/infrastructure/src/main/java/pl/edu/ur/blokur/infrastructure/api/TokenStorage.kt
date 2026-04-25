package pl.edu.ur.blokur.infrastructure.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Singleton DataStore przechowujący tokeny – jeden na kontekst aplikacji. */
private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("auth_tokens")

/**
 * Bezpieczny lokalny magazyn tokenów JWT oparty o [DataStore].
 *
 * Przechowuje access token, refresh token oraz rolę użytkownika.
 * Wszystkie operacje są `suspend` – bezpieczne do wywołania z korutyn.
 * Dostęp z OkHttp interceptorów odbywa się przez `runBlocking`
 * na tle wątku sieciowego (patrz [AuthInterceptor], [TokenAuthenticator]).
 *
 * @property context kontekst aplikacji wstrzykiwany przez Hilt.
 */
@Singleton
internal class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    /**
     * Zwraca access token lub `null`, gdy sesja nie istnieje.
     */
    suspend fun getAccessToken(): String? =
        context.tokenDataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()

    /**
     * Zwraca refresh token lub `null`, gdy sesja nie istnieje.
     */
    suspend fun getRefreshToken(): String? =
        context.tokenDataStore.data.map { it[KEY_REFRESH_TOKEN] }.firstOrNull()

    /**
     * Zwraca rolę użytkownika jako ciąg znaków lub `null`, gdy sesja nie istnieje.
     */
    suspend fun getUserRole(): String? =
        context.tokenDataStore.data.map { it[KEY_USER_ROLE] }.firstOrNull()

    /**
     * Atomowo zapisuje nowe tokeny i rolę (po udanym logowaniu lub odświeżeniu).
     *
     * @param accessToken  nowy krótkotrwały JWT.
     * @param refreshToken nowy długotrwały token odświeżający.
     * @param role         rola użytkownika (np. `"MIESZKANIEC"`).
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, role: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ROLE] = role
        }
    }

    /**
     * Usuwa wszystkie zapisane tokeny (wylogowanie).
     */
    suspend fun clearTokens() {
        context.tokenDataStore.edit { it.clear() }
    }
}
