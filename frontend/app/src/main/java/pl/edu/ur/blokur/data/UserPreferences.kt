package pl.edu.ur.blokur.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val USER_ROLE = stringPreferencesKey("user_role")
    }
    
    val authToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[JWT_TOKEN]
        }
        
    val userRole: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[USER_ROLE]
        }
        
    suspend fun saveAuthData(token: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
            preferences[USER_ROLE] = role
        }
    }
    
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
            preferences.remove(USER_ROLE)
        }
    }
}
