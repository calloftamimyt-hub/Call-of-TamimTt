package com.example.ui.screens

import android.app.Activity
import android.content.Context
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoAdsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val currentUserUid = UserSession.getUid(context)

    // Settings
    var rewardAd1 by remember { mutableStateOf(2.0) }
    var rewardAd2 by remember { mutableStateOf(3.0) }
    var rewardAd3 by remember { mutableStateOf(1.0) }
    var adsLoading by remember { mutableStateOf(false) }

    // Last watched timestamps
    val prefs = context.getSharedPreferences("video_ads_prefs_$currentUserUid", Context.MODE_PRIVATE)
    var lastWatched1 by remember { mutableStateOf(prefs.getLong("last_watched_1", 0L)) }
    var lastWatched2 by remember { mutableStateOf(prefs.getLong("last_watched_2", 0L)) }
    var lastWatched3 by remember { mutableStateOf(prefs.getLong("last_watched_3", 0L)) }

    // Time calculations
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        db.collection("settings").document("video_ads_settings")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    rewardAd1 = snapshot.getDouble("reward_ad1") ?: 2.0
                    rewardAd2 = snapshot.getDouble("reward_ad2") ?: 3.0
                    rewardAd3 = snapshot.getDouble("reward_ad3") ?: 1.0
                }
            }
    }

    // Rules
    val ad1Cooldown = 30 * 60 * 1000L // 30 mins
    val ad2Cooldown = 30 * 60 * 1000L
    val ad3Cooldown = 30 * 60 * 1000L
    val ad2BreakFromAd1 = 31 * 1000L
    val ad3BreakFromAd2 = 90 * 1000L // 1 min 30 sec

    // Availability checks
    val ad1NextTime = lastWatched1 + ad1Cooldown
    val ad1Available = currentTime >= ad1NextTime

    val ad2NextTimeBasedOnAd2 = lastWatched2 + ad2Cooldown
    val ad2NextTimeBasedOnAd1 = lastWatched1 + ad2BreakFromAd1
    val ad2NextTime = maxOf(ad2NextTimeBasedOnAd2, ad2NextTimeBasedOnAd1)
    val ad2Available = currentTime >= ad2NextTime

    val ad3NextTimeBasedOnAd3 = lastWatched3 + ad3Cooldown
    val ad3NextTimeBasedOnAd2 = lastWatched2 + ad3BreakFromAd2
    val ad3NextTime = maxOf(ad3NextTimeBasedOnAd3, ad3NextTimeBasedOnAd2)
    val ad3Available = currentTime >= ad3NextTime

    // Dialog state
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            snackbarMessage = ""
        }
    }

    fun handleReward(rewardAmount: Double, adNumber: Int) {
        val now = System.currentTimeMillis()
        when (adNumber) {
            1 -> { lastWatched1 = now; prefs.edit().putLong("last_watched_1", now).apply() }
            2 -> { lastWatched2 = now; prefs.edit().putLong("last_watched_2", now).apply() }
            3 -> { lastWatched3 = now; prefs.edit().putLong("last_watched_3", now).apply() }
        }

        scope.launch {
            try {
                if (currentUserUid.isNotEmpty()) {
                    val userRef = db.collection("users").document(currentUserUid)
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(userRef)
                        val bal = snapshot.getDouble("balance") ?: 0.0
                        val totalEarned = snapshot.getDouble("totalEarned") ?: 0.0
                        transaction.update(userRef, "balance", bal + rewardAmount)
                        transaction.update(userRef, "totalEarned", totalEarned + rewardAmount)
                        transaction.update(userRef, "lastAdCategoryTaskTime", System.currentTimeMillis())
                    }.await()
                    snackbarMessage = "Congratulations! You earned ৳$rewardAmount"
                }
            } catch (e: Exception) {
                snackbarMessage = "Error claiming reward"
            }
        }
    }

    fun loadAndShowRewardedAd() {
        if (activity == null) return
        adsLoading = true
        snackbarMessage = "Loading Ad..."
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, "ca-app-pub-4288324218526190/8832383188", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                adsLoading = false
                ad.show(activity) { _ ->
                    handleReward(rewardAd1, 1)
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                adsLoading = false
                snackbarMessage = "Ad failed to load. Try again later."
            }
        })
    }

    fun loadAndShowRewardedInterstitialAd() {
        if (activity == null) return
        adsLoading = true
        snackbarMessage = "Loading Ad..."
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, "ca-app-pub-4288324218526190/9504595744", adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                adsLoading = false
                ad.show(activity) { _ ->
                    handleReward(rewardAd2, 2)
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                adsLoading = false
                snackbarMessage = "Ad failed to load. Try again later."
            }
        })
    }

    fun loadAndShowInterstitialAd() {
        if (activity == null) return
        adsLoading = true
        snackbarMessage = "Loading Ad..."
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, "ca-app-pub-4288324218526190/1290229653", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                adsLoading = false
                ad.show(activity)
                // App-level reward point since AdMob doesn't handle callback points for Interstitial
                handleReward(rewardAd3, 3) 
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                adsLoading = false
                snackbarMessage = "Ad failed to load. Try again later."
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Ads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-4288324218526190/9060448049"
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VideoAdCard(
                title = "Rewarded Ad",
                bonus = rewardAd1,
                isAvailable = ad1Available,
                nextTimeStr = formatTimeLeft(ad1NextTime - currentTime),
                isLoading = adsLoading,
                onClick = { loadAndShowRewardedAd() }
            )
            
            VideoAdCard(
                title = "Rewarded Interstitial Ad",
                bonus = rewardAd2,
                isAvailable = ad2Available,
                nextTimeStr = formatTimeLeft(ad2NextTime - currentTime),
                isLoading = adsLoading,
                onClick = { loadAndShowRewardedInterstitialAd() }
            )
            
            VideoAdCard(
                title = "Interstitial Ad",
                bonus = rewardAd3,
                isAvailable = ad3Available,
                nextTimeStr = formatTimeLeft(ad3NextTime - currentTime),
                isLoading = adsLoading,
                onClick = { loadAndShowInterstitialAd() }
            )
        }
    }
}

@Composable
fun VideoAdCard(
    title: String,
    bonus: Double,
    isAvailable: Boolean,
    nextTimeStr: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Bonus: ৳$bonus", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                if (!isAvailable) {
                    Text("Available in: $nextTimeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Button(
                onClick = onClick,
                enabled = isAvailable && !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start")
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
