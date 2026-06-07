package pl.edu.ur.blokur.services

import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pobiera token FCM z Firebase Messaging.
 * Gdy Google Play Services są niedostępne — zwraca null (bez crasha).
 */
@Singleton
class FirebaseFcmTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : FcmTokenProvider {

    companion object {
        private const val TAG = "FirebaseFcmTokenProvider"
    }

    override suspend fun getToken(): String? {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(context)
        if (result != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services niedostępne ($result) — pominięto token FCM")
            return null
        }
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.w(TAG, "Nie udało się pobrać tokenu FCM: ${e.message}")
            null
        }
    }
}
