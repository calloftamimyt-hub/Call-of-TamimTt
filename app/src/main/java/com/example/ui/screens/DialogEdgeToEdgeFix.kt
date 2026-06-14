package com.example.ui.screens

import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

@Composable
fun FullScreenDialogModifier(
    statusBarColor: Int = android.graphics.Color.WHITE,
    isLightStatusIcons: Boolean = true
) {
    val dialogView = LocalView.current
    SideEffect {
        val window = (dialogView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.let {
            // Set decor fits system windows to false so we have full system bar control
            WindowCompat.setDecorFitsSystemWindows(it, false)
            
            // Clear dim behind to prevent black transparent overlays on system bars
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            
            // Enable custom status and navigation bar backgrounds
            it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            it.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            it.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            
            // Ensure the window matches physical dimensions fully
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            
            // Force status/navigation bar background
            it.statusBarColor = statusBarColor
            it.navigationBarColor = android.graphics.Color.WHITE
            
            // Configure status and navigation bar components to show in light/dark visibility correctly
            val insetsController = WindowCompat.getInsetsController(it, dialogView)
            insetsController.isAppearanceLightStatusBars = isLightStatusIcons 
            insetsController.isAppearanceLightNavigationBars = true 
            
            // Ensure window background is solid white to cover the screen
            it.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE))
        }
    }
}

@Composable
fun BeautifulHeader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Color.White)
    ) {
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            // Draw ambient light-blue glowing spheres for an aesthetically pleasing effect on white
            drawCircle(
                color = Color(0x1F42A5F5), // Soft glowing light blue
                radius = size.width * 0.45f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.25f)
            )
            drawCircle(
                color = Color(0x1590CAF9), // Subtle sky blue ambient fill
                radius = size.width * 0.35f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.75f)
            )
        }
        content()
    }
}

@Composable
fun WhiteStatusBarFix() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current
    SideEffect {
        val activity = context as? android.app.Activity
        activity?.window?.let { window ->
            window.statusBarColor = android.graphics.Color.WHITE
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
        }
    }
}



