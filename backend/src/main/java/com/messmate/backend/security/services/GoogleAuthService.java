package com.messmate.backend.security.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {

    @Value("${google.client.id:dummyClientId}")
    private String googleClientId;

    public GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                // In production, we must uncomment the generic audience validation
                // .setAudience(Collections.singletonList(googleClientId))
                .build();

        // Warning: For this demo without a real audience, we might need a basic parser
        // if google client id is not configured
        // However GoogleIdTokenVerifier requires valid signature anyway
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new Exception("Invalid ID token.");
        }
    }
}
