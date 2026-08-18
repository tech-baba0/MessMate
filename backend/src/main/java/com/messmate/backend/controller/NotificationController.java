package com.messmate.backend.controller;

import com.messmate.backend.entity.User;
import com.messmate.backend.repository.UserRepository;
import com.messmate.backend.security.services.UserDetailsImpl;
import com.messmate.backend.service.FcmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private FcmService fcmService;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/notifications/status
     * Returns whether FCM is initialised on the backend.
     * Use this to verify whether FIREBASE_SERVICE_ACCOUNT_JSON env var is working.
     */
    @GetMapping("/status")
    public ResponseEntity<?> fcmStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("fcmReady", fcmService.isReady());
        result.put("message", fcmService.isReady()
                ? "✅ Firebase Admin SDK is initialised. Notifications will be delivered."
                : "❌ Firebase not initialised. Set FIREBASE_SERVICE_ACCOUNT_JSON env var on Render.");
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/notifications/test
     * Sends a test push notification to the currently-logged-in user's device.
     * Call this from Postman (with JWT bearer) or from the Android debug menu.
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification() {
        Map<String, Object> result = new HashMap<>();

        if (!fcmService.isReady()) {
            result.put("success", false);
            result.put("message",
                    "❌ FCM not initialised on server. Add FIREBASE_SERVICE_ACCOUNT_JSON to Render env vars.");
            return ResponseEntity.ok(result);
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            result.put("success", false);
            result.put("message", "Not authenticated.");
            return ResponseEntity.badRequest().body(result);
        }

        String userId = ((UserDetailsImpl) principal).getId();
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found.");
            return ResponseEntity.badRequest().body(result);
        }

        User user = userOpt.get();
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.isBlank()) {
            result.put("success", false);
            result.put("message", "No FCM token saved for this user. Open the app and log in to register the token.");
            result.put("userId", userId);
            return ResponseEntity.ok(result);
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "TEST");

        fcmService.sendPushNotificationWithData(
                fcmToken,
                "🔔 MessMate Test",
                "Push notifications are working! ✅",
                data);

        result.put("success", true);
        result.put("message", "Test notification sent to " + user.getName() + " (" + user.getEmail() + ")");
        result.put("tokenPrefix", fcmToken.substring(0, Math.min(20, fcmToken.length())) + "…");
        return ResponseEntity.ok(result);
    }
}
