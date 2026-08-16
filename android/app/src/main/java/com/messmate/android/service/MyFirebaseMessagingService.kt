package com.messmate.android.service

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
import com.messmate.android.MainActivity
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")

        scope.launch {
            try {
                if (!ApiClient.isInitialized) {
                    ApiClient.initialize(applicationContext)
                }

                if (ApiClient.tokenManager.getToken() != null) {
                    ApiClient.apiService.updateFcmToken(
                        com.messmate.android.data.auth.FcmTokenRequest(token)
                    )
                    Log.d("FCM", "Token updated successfully on backend")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Failed to update token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from} with data: ${message.data}")

        // Instant Live Update Trigger
        val type = message.data["type"]
        if (type != null) {
            Log.d("FCM", "Emitting Live Update Event: $type")
            FcmEventBus.emitEvent(type)
        }

        // Handle Visual Notification
        message.notification?.let {
            showForegroundNotification(it.title ?: "MessMate", it.body ?: "")
        } ?: run {
            val title = message.data["title"]
            val body = message.data["body"]
            if (title != null && body != null) {
                showForegroundNotification(title, body)
            }
        }
    }

    private fun showForegroundNotification(title: String, body: String) {
        val channelId = "messmate_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "MessMate Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates for meals and expenses"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
