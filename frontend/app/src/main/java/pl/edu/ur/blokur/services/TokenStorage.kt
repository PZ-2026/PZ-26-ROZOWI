package pl.edu.ur.blokur.services

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

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("auth_tokens")

/**
 * Bezpieczny lokalny magazyn tokenów JWT oparty o DataStore.
 *
 * Przechowuje access token, refresh token oraz rolę użytkownika.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    suspend fun getAccessToken(): String? =
        context.tokenDataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()

    suspend fun getRefreshToken(): String? =
        context.tokenDataStore.data.map { it[KEY_REFRESH_TOKEN] }.firstOrNull()

    suspend fun getUserRole(): String? =
        context.tokenDataStore.data.map { it[KEY_USER_ROLE] }.firstOrNull()

    suspend fun saveTokens(accessToken: String, refreshToken: String, role: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ROLE] = role
        }
    }

    suspend fun clearTokens() {
        context.tokenDataStore.edit { it.clear() }
    }
}
