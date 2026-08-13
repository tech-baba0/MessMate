package com.messmate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.messmate.android.network.ApiClient
import com.messmate.android.ui.navigation.MessMateNavGraph
import com.messmate.android.ui.theme.MessMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ApiClient.initialize(this)
        
        setContent {
            MessMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val startDest = if (ApiClient.tokenManager.getToken() != null) {
                        try {
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful && task.result != null) {
                                    androidx.lifecycle.lifecycleScope.launchWhenStarted {
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
