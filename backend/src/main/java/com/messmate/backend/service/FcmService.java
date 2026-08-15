package com.messmate.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class FcmService {

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-adminsdk.json");
            if (serviceAccount == null) {
                System.out.println(
                        "No firebase-adminsdk.json found, FCM not initialized. Notifications will be skipped.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Application initialized successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase Messaging: " + e.getMessage());
        }
    }

    public void sendPushNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                return; // Not initialized
            }

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message to " + token + ": " + response);
        } catch (Exception e) {
            System.err.println("Firebase message delivery failed: " + e.getMessage());
        }
    }

    public void sendPushNotificationWithData(String token, String title, String body,
            java.util.Map<String, String> data) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                return; // Not initialized
            }

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent data message to " + token + ": " + response);
        } catch (Exception e) {
            System.err.println("Firebase data message delivery failed: " + e.getMessage());
        }
    }
}
