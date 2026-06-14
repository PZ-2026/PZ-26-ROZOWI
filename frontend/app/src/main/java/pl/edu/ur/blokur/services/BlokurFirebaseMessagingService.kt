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
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.MainActivity

/**
 * Serwis FCM — wymagany przez manifest przy integracji Firebase Messaging.
 * Obsługuje odbieranie wiadomości push również gdy aplikacja jest na pierwszym planie.
 */
class BlokurFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "BlokurFCM"
        private const val CHANNEL_ID = "blokur_notifications"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nowy token FCM: ${token.take(8)}…")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"]
        if (type == "ZMIANA_ROLI") {
            Log.i(TAG, "Odebrano cichy PUSH ZMIANA_ROLI. Wymuszam odświeżenie sesji...")
            handleRoleChangedPush()
            return
        }

        Log.d(TAG, "Odebrano push: ${message.notification?.title}")
        showNotification(message)
    }

    private fun handleRoleChangedPush() {
        val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
            applicationContext,
            pl.edu.ur.blokur.di.AuthEntryPoint::class.java
        )
        val authService = entryPoint.authService()
        val tokenStorage = entryPoint.tokenStorage()

        val sessionManager = dagger.hilt.android.EntryPointAccessors.fromApplication(
            applicationContext,
            pl.edu.ur.blokur.di.SessionEntryPoint::class.java
        ).sessionManager()

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val oldRole = tokenStorage.getUserRole()
                if (oldRole == null) {
                    Log.i(TAG, "Zignorowano powiadomienie ZMIANA_ROLI, użytkownik nie jest zalogowany.")
                    return@launch
                }

                val newRole = authService.forceTokenRefresh()
                if (newRole != null) {
                    if (oldRole == newRole) {
                        Log.i(TAG, "Rola po odświeżeniu jest taka sama ($newRole). Ignoruję powiadomienie PUSH.")
                        return@launch
                    }

                    Log.i(TAG, "Zmieniono rolę z sukcesem z $oldRole na $newRole, wysyłam sygnał forceRouteRefresh.")
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            applicationContext,
                            "System: Automatycznie zaktualizowano rolę z $oldRole na $newRole",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    
                    sessionManager.forceRouteRefresh()
                } else {
                    Log.w(TAG, "Nie udało się odświeżyć tokena przy zmianie roli, wylogowuję.")
                    sessionManager.invalidateSession()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Błąd podczas odświeżania sesji po zmianie roli", e)
            }
        }
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
