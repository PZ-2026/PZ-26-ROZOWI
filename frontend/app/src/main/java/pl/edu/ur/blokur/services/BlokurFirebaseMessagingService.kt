package pl.edu.ur.blokur.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Serwis FCM — wymagany przez manifest przy integracji Firebase Messaging.
 */
class BlokurFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "BlokurFCM"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nowy token FCM: ${token.take(8)}…")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Odebrano push: ${message.notification?.title}")
    }
}
