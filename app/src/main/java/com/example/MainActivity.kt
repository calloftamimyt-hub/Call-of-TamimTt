package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleNotificationIntent(intent)
    MobileAds.initialize(this) {}
    enableEdgeToEdge(
        statusBarStyle = androidx.activity.SystemBarStyle.light(
            android.graphics.Color.WHITE, android.graphics.Color.WHITE
        ),
        navigationBarStyle = androidx.activity.SystemBarStyle.light(
            android.graphics.Color.WHITE, android.graphics.Color.WHITE
        )
    )
    setContent {
      MyApplicationTheme {
        val context = androidx.compose.ui.platform.LocalContext.current
        var isLoggedIn by remember { mutableStateOf(com.example.ui.screens.UserSession.isLoggedIn(context)) }

        // Dynamic Request for POST_NOTIFICATIONS Permission on App Launch (Android 13+)
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Permission result handled gracefully
        }

        LaunchedEffect(Unit) {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.POST_NOTIFICATIONS"
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                }
            }
        }
        
        if (isLoggedIn) {
          MainScreen(onLogout = { isLoggedIn = false })
        } else {
          AuthScreen(onLoginSuccess = { isLoggedIn = true })
        }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleNotificationIntent(intent)
  }

  private fun handleNotificationIntent(intent: android.content.Intent?) {
    if (intent == null) return
    if (intent.getStringExtra("action") == "open_notifications") {
        com.example.NotificationNavigationState.shouldOpenNotificationsPage = true
    }
  }
}
