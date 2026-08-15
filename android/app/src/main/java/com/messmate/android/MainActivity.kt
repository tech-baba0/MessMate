package com.messmate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.messmate.android.network.ApiClient
import com.messmate.android.ui.navigation.MessMateNavGraph
import com.messmate.android.ui.theme.MessMateTheme
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ApiClient.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        setContent {
            MessMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val startDest = if (ApiClient.tokenManager.getToken() != null) {
                        try {
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful && task.result != null) {
                                    lifecycleScope.launch {
                                        try {
                                            ApiClient.apiService.updateFcmToken(
                                                com.messmate.android.data.auth.FcmTokenRequest(task.result!!)
                                            )
                                        } catch (e: Exception) {}
                                    }
                                }
                            }
                        } catch (e: Exception) {}
                        com.messmate.android.ui.navigation.Screen.Dashboard.route
                    } else {
                        com.messmate.android.ui.navigation.Screen.Login.route
                    }
                    MessMateNavGraph(startDestination = startDest)
                }
            }
        }
    }
}
