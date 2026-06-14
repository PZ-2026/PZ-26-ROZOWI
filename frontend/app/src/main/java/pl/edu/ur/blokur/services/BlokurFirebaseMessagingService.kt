package pl.edu.ur.blokur.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.MainActivity
import javax.inject.Inject

/**
 * Serwis FCM — wymagany przez manifest przy integracji Firebase Messaging.
 * Obsługuje odbieranie wiadomości push również gdy aplikacja jest na pierwszym planie.
 *
 * Gdy FCM wygeneruje nowy token (rotacja, reinstalacja), [onNewToken] automatycznie
 * rejestruje go w backendzie — dzięki czemu push notifications działają ciągle.
 */
@AndroidEntryPoint
class BlokurFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "BlokurFCM"
        private const val CHANNEL_ID = "blokur_notifications"
    }

    @Inject
    lateinit var deviceService: DeviceService

    @Inject
    lateinit var tokenStorage: TokenStorage

    /** Scope do operacji asynchronicznych w ramach cyklu życia serwisu. */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Wywoływane przez Firebase gdy token FCM zostaje wygenerowany lub odświeżony
     * (np. przy reinstalacji lub po długim czasie nieaktywności).
     *
     * Rejestrujemy nowy token w backendzie i zapisujemy go lokalnie w DataStore,
     * dzięki czemu push notifications trafiają na aktualne urządzenie.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Nowy token FCM: ${token.take(8)}…")
        serviceScope.launch {
            try {
                val ok = deviceService.registerDevice(token)
                if (ok) {
                    tokenStorage.saveFcmToken(token)
                    Log.d(TAG, "Token FCM zarejestrowany w backendzie po rotacji")
                } else {
                    Log.w(TAG, "Rejestracja tokenu FCM po rotacji nieudana (brak sesji?)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Błąd rejestracji tokenu FCM po rotacji: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Odebrano push: ${message.notification?.title}")
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Blokur"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Powiadomienia Blokur",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
