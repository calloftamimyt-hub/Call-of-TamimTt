package com.example.ui.screens

import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FullScreenDialogModifier() {
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
            
            // Force status/navigation bar background to White
            it.statusBarColor = android.graphics.Color.WHITE
            it.navigationBarColor = android.graphics.Color.WHITE
            
            // Configure status and navigation bar components to show in light/dark visibility correctly
            val insetsController = WindowCompat.getInsetsController(it, dialogView)
            insetsController.isAppearanceLightStatusBars = true 
            insetsController.isAppearanceLightNavigationBars = true 
            
            // Ensure window background is solid white to cover the screen
            it.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE))
        }
    }
}



