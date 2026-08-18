package com.messmate.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * FcmService — initialises Firebase Admin SDK and sends push notifications.
 *
 * Credential loading order:
 * 1. Environment variable FIREBASE_SERVICE_ACCOUNT_JSON (contents of the JSON)
 * 2. Classpath resource firebase-adminsdk.json
 * If neither is present, FCM is disabled (notifications silently skipped).
 */
@Service
public class FcmService {

    private boolean fcmReady = false;

    public boolean isReady() {
        return fcmReady;
    }

    @PostConstruct
    public void initialize() {
        try {
            InputStream credentialsStream = resolveCredentials();
            if (credentialsStream == null) {
                System.out.println("[FCM] No Firebase credentials found — notifications disabled.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            fcmReady = true;
            System.out.println("[FCM] Firebase Admin SDK initialised successfully.");

        } catch (Exception e) {
            System.err.println("[FCM] Initialisation failed: " + e.getMessage());
        }
    }

    /**
     * Try env var first, then classpath file.
     */
    private InputStream resolveCredentials() {
        // 1. Environment variable (preferred for cloud deployments like Render)
        String envJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (envJson != null && !envJson.isBlank()) {
            System.out.println("[FCM] Loading credentials from FIREBASE_SERVICE_ACCOUNT_JSON env var.");
            return new ByteArrayInputStream(envJson.getBytes(StandardCharsets.UTF_8));
        }

        // 2. Classpath resource (local dev)
        InputStream resource = getClass().getClassLoader().getResourceAsStream("firebase-adminsdk.json");
        if (resource != null) {
            System.out.println("[FCM] Loading credentials from classpath firebase-adminsdk.json.");
            return resource;
        }

        return null;
    }

    // ─── Public send methods ──────────────────────────────────────────────────

    public void sendPushNotification(String token, String title, String body) {
        if (!fcmReady || token == null || token.isBlank())
            return;

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("[FCM] Sent to " + token.substring(0, Math.min(20, token.length())) + "… → " + response);
        } catch (Exception e) {
            System.err.println("[FCM] Send failed: " + e.getMessage());
        }
    }

    public void sendPushNotificationWithData(String token, String title, String body,
            java.util.Map<String, String> data) {
        if (!fcmReady || token == null || token.isBlank())
            return;

        try {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());
            System.out.println(
                    "[FCM] Sent data msg to " + token.substring(0, Math.min(20, token.length())) + "… → " + response);
        } catch (Exception e) {
            System.err.println("[FCM] Send data msg failed: " + e.getMessage());
        }
    }
}
