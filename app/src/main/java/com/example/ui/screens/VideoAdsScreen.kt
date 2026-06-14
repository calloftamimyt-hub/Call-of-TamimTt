package com.example.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class VideoAd(
    val id: Int = 0,
    val label: String = "",
    val reward_amount: Double = 0.0,
    val break_duration: Int = 0, // seconds
    val ad_unit_id: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoAdsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val currentUserUid = UserSession.getUid(context)
    
    com.example.ui.screens.WhiteStatusBarFix()

    // Dynamic settings from Firestore
    var isEnabled by remember { mutableStateOf(true) }
    var dailyLimit by remember { mutableStateOf(10) }
    var videoAdsList by remember { mutableStateOf(listOf<VideoAd>()) }

    // User's daily count and last ad reset date
    var userDailyCount by remember { mutableStateOf(0) }
    var lastResetDate by remember { mutableStateOf("") }
    
    // Cooldown management
    val prefs = context.getSharedPreferences("VideoAdCooldowns_$currentUserUid", Context.MODE_PRIVATE)
    var currentTime by remember { mutableStateOf(System.currentTimeMillis() / 1000) }
    
    // Refresh current time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis() / 1000
            delay(1000)
        }
    }

    // Sync settings from Firestore
    LaunchedEffect(Unit) {
        db.collection("settings").document("video_ad_settings")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    isEnabled = snapshot.getBoolean("is_enabled") ?: true
                    dailyLimit = (snapshot.getLong("daily_limit") ?: 10).toInt()
                    
                    val adsArray = snapshot.get("ads") as? List<Map<String, Any>>
                    val mappedAds = ArrayList<VideoAd>()
                    adsArray?.forEach { map ->
                        mappedAds.add(VideoAd(
                            id = (map["id"] as? Long ?: 0).toInt(),
                            label = map["label"] as? String ?: "Video Ad",
                            reward_amount = (map["reward_amount"] as? Number)?.toDouble() ?: 0.0,
                            break_duration = (map["break_duration"] as? Long ?: 60).toInt(),
                            ad_unit_id = map["ad_unit_id"] as? String ?: "ca-app-pub-3940256099942544/5224354917" // Test ID
                        ))
                    }
                    videoAdsList = mappedAds
                }
            }
            
        if (currentUserUid.isNotEmpty()) {
            db.collection("users").document(currentUserUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userDailyCount = (snapshot.getLong("daily_ad_count") ?: 0).toInt()
                        lastResetDate = snapshot.getString("last_ad_reset_date") ?: ""
                        
                        // Check if we need to reset daily count
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        if (lastResetDate != today) {
                            db.collection("users").document(currentUserUid)
                                .update("daily_ad_count", 0, "last_ad_reset_date", today)
                        }
                    }
                }
        }
    }

    var adsLoading by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            snackbarMessage = ""
        }
    }

    fun creditUserWallet(rewardAmount: Double) {
        scope.launch {
            try {
                if (currentUserUid.isNotEmpty()) {
                    val userRef = db.collection("users").document(currentUserUid)
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(userRef)
                        val bal = snapshot.getDouble("balance") ?: 0.0
                        val totalEarned = snapshot.getDouble("totalEarned") ?: 0.0
                        val count = snapshot.getLong("daily_ad_count") ?: 0
                        
                        transaction.update(userRef, "balance", bal + rewardAmount)
                        transaction.update(userRef, "totalEarned", totalEarned + rewardAmount)
                        transaction.update(userRef, "daily_ad_count", count + 1)
                    }.await()
                    com.example.utils.ReferralCommissionHelper.applyCommission(currentUserUid, rewardAmount)
                    snackbarMessage = "Congratulations! You earned ৳$rewardAmount"
                }
            } catch (e: Exception) {
                snackbarMessage = "Error updating wallet"
            }
        }
    }

    fun playRewardedAd(videoAd: VideoAd) {
        if (activity == null) return
        
        // 1. Check Daily Limit
        if (userDailyCount >= dailyLimit) {
            snackbarMessage = "You have reached your daily ad limit of $dailyLimit!"
            return
        }

        // 2. Check Global Cooldown (1 minute between ANY ad)
        val lastAnyWatchTime = prefs.getLong("last_any_ad_watch_time", 0L)
        val globalSecondsPassed = currentTime - lastAnyWatchTime
        if (globalSecondsPassed < 60) {
            val globalRemain = 60 - globalSecondsPassed
            snackbarMessage = "Please wait $globalRemain seconds for the next ad!"
            return
        }

        // 3. Check Per-Ad Cooldown
        val lastWatchTime = prefs.getLong("last_watch_time_ad_${videoAd.id}", 0L)
        val secondsPassed = currentTime - lastWatchTime
        if (secondsPassed < videoAd.break_duration) {
            val remain = videoAd.break_duration - secondsPassed
            snackbarMessage = "Please wait $remain seconds for this specific ad!"
            return
        }

        adsLoading = true
        snackbarMessage = "Loading Ad..."
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, videoAd.ad_unit_id, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                adsLoading = false
                ad.show(activity) { _ ->
                    // Update Cooldowns
                    prefs.edit()
                        .putLong("last_watch_time_ad_${videoAd.id}", currentTime)
                        .putLong("last_any_ad_watch_time", currentTime)
                        .apply()
                    // Credit Wallet
                    creditUserWallet(videoAd.reward_amount)
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                adsLoading = false
                snackbarMessage = "Ad failed to load. Please try again."
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Ads", color = Color.Black, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (!isEnabled) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Video Ads are currently disabled by Admin.", fontWeight = FontWeight.Bold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Daily Goal", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { if (dailyLimit > 0) userDailyCount.toFloat() / dailyLimit else 0f },
                                modifier = Modifier.weight(1f).height(8.dp),
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFFE8F5E9),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("$userDailyCount/$dailyLimit", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Video Ad Cards
                val lastAnyWatchTime = prefs.getLong("last_any_ad_watch_time", 0L)
                val globalSecondsPassed = currentTime - lastAnyWatchTime
                val isGlobalAvailable = globalSecondsPassed >= 60
                val globalRemain = if (isGlobalAvailable) 0 else (60 - globalSecondsPassed).toInt()

                videoAdsList.forEach { videoAd ->
                    val lastWatchTime = prefs.getLong("last_watch_time_ad_${videoAd.id}", 0L)
                    val secondsPassed = currentTime - lastWatchTime
                    val isPerAdAvailable = secondsPassed >= videoAd.break_duration
                    
                    val isOverallAvailable = isGlobalAvailable && isPerAdAvailable
                    val effectiveRemain = if (isOverallAvailable) 0 else maxOf(globalRemain, if (isPerAdAvailable) 0 else (videoAd.break_duration - secondsPassed).toInt())

                    VideoAdCard(
                        videoAd = videoAd,
                        isAvailable = isOverallAvailable,
                        remainingSeconds = effectiveRemain,
                        isLoading = adsLoading,
                        onClick = { playRewardedAd(videoAd) }
                    )
                }
                
                if (videoAdsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoAdCard(
    videoAd: VideoAd,
    isAvailable: Boolean,
    remainingSeconds: Int,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = "Video",
                modifier = Modifier.size(48.dp),
                tint = if (isAvailable) Color(0xFFE91E63) else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = videoAd.label,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reward: ৳${videoAd.reward_amount}",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                if (!isAvailable) {
                    Text(
                        text = "Wait: ${remainingSeconds}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
            Button(
                onClick = onClick,
                enabled = isAvailable && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAvailable) Color(0xFF1E1E1E) else Color.LightGray,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("Start", fontWeight = FontWeight.Bold)
            }
        }
    }
}


fun formatTimeLeft(millis: Long): String {
    if (millis <= 0) return "0s"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}
