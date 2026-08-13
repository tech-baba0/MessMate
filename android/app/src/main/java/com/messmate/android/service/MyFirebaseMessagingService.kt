package com.messmate.android.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
        // TODO: Send token to backend if user is logged in
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // Typically show notification here using NotificationManager
            // Because MessMate is Compose, we could also use a broadcast receiver to show in-app snackbars.
        }
    }
}
