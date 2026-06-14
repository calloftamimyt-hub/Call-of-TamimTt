package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AdMobManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdViewScreen(task: EarningTask, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var showHistory by remember { mutableStateOf(false) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }
    var adsWatched by remember { mutableStateOf(0) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Dynamic settings from Firestore
    var requiredAdsLimit by remember { mutableStateOf(3) }
    var rewardAmount by remember { mutableStateOf(0.15) }
    var breakDuration by remember { mutableStateOf(10) }
    var lastAdWatchTime by remember { mutableStateOf(0L) }
    var isLoadingSettings by remember { mutableStateOf(true) }
    var isSubmittingTransaction by remember { mutableStateOf(false) }

    val currentUserUid = UserSession.getUid(context)

    // Listen to Ad View settings in real-time independently of user authentication timing
    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("settings").document("ad_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AdViewScreen", "Settings listen failed", error)
                    android.widget.Toast.makeText(context, "বিজ্ঞাপন সেটিংস আপডেট ব্যর্থ বা অনুমতি নেই: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    isLoadingSettings = false
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val rawLimit = snapshot.get("required_ads")
                    requiredAdsLimit = when (rawLimit) {
                        is Number -> rawLimit.toInt()
                        is String -> rawLimit.toIntOrNull() ?: 3
                        else -> snapshot.getLong("required_ads")?.toInt() ?: 3
                    }
                    
                    val rawReward = snapshot.get("reward_amount")
                    rewardAmount = when (rawReward) {
                        is Number -> rawReward.toDouble()
                        is String -> rawReward.toDoubleOrNull() ?: 0.15
                        else -> snapshot.getDouble("reward_amount") ?: 0.15
                    }
                    
                    val rawBreak = snapshot.get("break_duration")
                    breakDuration = when (rawBreak) {
                        is Number -> rawBreak.toInt()
                        is String -> rawBreak.toIntOrNull() ?: 10
                        else -> snapshot.getLong("break_duration")?.toInt() ?: 10
                    }
                }
                isLoadingSettings = false
            }
    }

    // Listen to current user watch limits/cooldown progress
    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.w("AdViewScreen", "User listen failed: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        lastAdWatchTime = when (val value = snapshot.get("lastAdWatchTime")) {
                            is Number -> value.toLong()
                            is String -> value.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                    }
                }
        }
    }

    LaunchedEffect(Unit) {
        AdMobManager.loadRewardedAd(context)
    }

    // Live countdown calculations in seconds
    var remainingSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(lastAdWatchTime, breakDuration) {
        while (true) {
            val totalBreakMs = breakDuration.toLong() * 60L * 1000L
            val elapsedMs = System.currentTimeMillis() - lastAdWatchTime
            val diffMs = totalBreakMs - elapsedMs
            if (diffMs > 0) {
                remainingSeconds = diffMs / 1000
            } else {
                remainingSeconds = 0
            }
            delay(1000)
        }
    }

    if (showHistory) {
        AdHistoryDialog(onDismiss = { showHistory = false })
    }

    fun handleAdReward() {
        if (activity != null) {
            showingAdProgressDialog = true
            AdMobManager.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    adsWatched++
                    if (adsWatched >= requiredAdsLimit) {
                        showingAdProgressDialog = false
                        isSubmittingTransaction = true
                        
                        // User completed the set of ads; credit the reward dynamically on Firestore
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val uDocRef = db.collection("users").document(currentUserUid)
                        
                        db.runTransaction { tx ->
                            val userSnap = tx.get(uDocRef)
                            val currentBalance = when (val v = userSnap.get("balance")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val currentEarnings = when (val v = userSnap.get("earnings")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            tx.update(uDocRef, "balance", currentBalance + rewardAmount)
                            tx.update(uDocRef, "earnings", currentEarnings + rewardAmount)
                            tx.update(uDocRef, "lastAdWatchTime", System.currentTimeMillis())
                            tx.update(uDocRef, "lastAdCategoryTaskTime", System.currentTimeMillis())
                            
                            val nDoc = db.collection("notifications").document()
                            val notifyData = hashMapOf(
                                "id" to nDoc.id,
                                "userId" to currentUserUid,
                                "title" to "Ad View Reward Earned 🎁",
                                "message" to "অভিনন্দন! আপনি ${requiredAdsLimit}টি অ্যাড সম্পূর্ণ দেখে ৳${String.format("%.2f", rewardAmount)} পুরস্কার পেয়েছেন।",
                                "type" to "SUCCESS",
                                "isRead" to false,
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            tx.set(nDoc, notifyData)
                        }.addOnCompleteListener { taskResult ->
                            isSubmittingTransaction = false
                            adsWatched = 0
                            if (taskResult.isSuccessful) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("অভিনন্দন! সফলভাবে ৳${String.format("%.2f", rewardAmount)} বোনাস পেয়েছেন।")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("বিজ্ঞাপন রিওয়ার্ড যোগ করতে ব্যর্থ হয়েছে!")
                                }
                            }
                        }
                    } else {
                        showingAdProgressDialog = true
                    }
                },
                onAdDismissed = {
                    showingAdProgressDialog = false
                    if (adsWatched < requiredAdsLimit) {
                        adsWatched = 0
                    }
                }
            )
        }
    }

    if (showingAdProgressDialog) {
        Dialog(onDismissRequest = { /* Cannot dismiss ad setup */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = task.color)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ad Progress: $adsWatched / $requiredAdsLimit watched. \nLoading next ad required for reward...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { handleAdReward() }) {
                        Text("Show Next Ad")
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text(task.title, color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) }
                        },
                        actions = {
                            IconButton(onClick = { showHistory = true }) { Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                if (isLoadingSettings) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (remainingSeconds > 0) {
                    // RENDERING REST COUNTDOWN FOR AD WATCH LIMIT
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    val timeText = "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Break Time",
                                    modifier = Modifier.size(72.dp),
                                    tint = task.color
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "বিজ্ঞাপন দেখার ব্রেক টাইম চলছে!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "আপনি ${requiredAdsLimit}টি অ্যাড সফলভাবে শেষ করেছেন। দয়া করে ব্রেক টাইম শেষ হওয়া পর্যন্ত অপেক্ষা করুন এবং আবারও অ্যাড দেখুন।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = timeText,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = task.color
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "অবশিষ্ট সময়",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("ফিরে যান")
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Info, contentDescription = null, tint = task.color)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ad View Guidelines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    Text("সম্পূর্ণ বিজ্ঞাপন দেখে ঘরে বসে আয় করুন। দয়া করে প্রতিটি বিজ্ঞাপন মনোযোগ দিয়ে শেষ হওয়া পর্যন্ত দেখুন।", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Reward Amount:", fontWeight = FontWeight.SemiBold)
                                        Text("৳${String.format("%.2f", rewardAmount)}", color = task.color, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Ads to Watch:", fontWeight = FontWeight.SemiBold)
                                        Text("$adsWatched / $requiredAdsLimit Ads")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Break time between sessions:", fontWeight = FontWeight.SemiBold)
                                        Text("$breakDuration Minutes")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        
                        item {
                            if (isSubmittingTransaction) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                Button(
                                    onClick = { 
                                        handleAdReward()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Text("Watch Ad & Earn", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdHistoryDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Ad View History", color = Color.Black) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    item {
                        Text("No history yet.", modifier = Modifier.padding(16.dp), color = androidx.compose.ui.graphics.Color.Gray)
                    }
                }
            }
        }
    }
}
