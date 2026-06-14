package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchCardScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }

    fun loadAd() {
        if (rewardedAd != null || isAdLoading) return
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-4288324218526190/8832383188",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isAdLoading = false
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                }
            })
    }

    LaunchedEffect(Unit) {
        loadAd()
    }

    var userBalance by remember { mutableDoubleStateOf(0.0) }
    var scratchCardAmount by remember { mutableDoubleStateOf(0.0) }
    var isScratched by remember { mutableStateOf(false) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var isSavingReward by remember { mutableStateOf(false) }

    var isEnabled by remember { mutableStateOf(true) }
    var rewardsList by remember { mutableStateOf(listOf(0.1, 0.2, 0.5, 1.0, 2.0)) }
    var breakTimeMinutes by remember { mutableIntStateOf(5) }
    var dailyLimit by remember { mutableIntStateOf(10) }

    var lastScratchTime by remember { mutableLongStateOf(0L) }
    var userScratchesCount by remember { mutableIntStateOf(0) }

    // Listen to admin settings
    LaunchedEffect(Unit) {
        db.collection("settings").document("scratch_card")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    isEnabled = snapshot.getBoolean("is_enabled") ?: true
                    breakTimeMinutes = snapshot.getLong("break_time")?.toInt() ?: 5
                    dailyLimit = snapshot.getLong("daily_limit")?.toInt() ?: 10
                    val rewardsStr = snapshot.getString("rewards") ?: "5,10,2"
                    rewardsList = rewardsStr.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                    if (rewardsList.isEmpty()) rewardsList = listOf(5.0)
                    
                    // Assign sequential reward based on how many scratches user has done
                    // If userScratchesCount exceeds the list, it loops back.
                    if (!isScratched && scratchCardAmount == 0.0) {
                        val index = userScratchesCount % rewardsList.size
                        scratchCardAmount = rewardsList[index]
                    }
                }
            }
    }

    // Listen to user balance and limits
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userBalance = snapshot.getDouble("balance") ?: 0.0
                        userScratchesCount = snapshot.getLong("daily_scratches_count")?.toInt() ?: 0
                        lastScratchTime = snapshot.getLong("last_scratch_timestamp") ?: 0L

                        // Reset count if it's a new day
                        val lastDate = snapshot.getLong("last_scratch_date") ?: 0L
                        val currentDate = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                        if (lastDate != currentDate) {
                            db.collection("users").document(userId).update(
                                "daily_scratches_count", 0,
                                "last_scratch_date", currentDate
                            )
                        }
                    }
                }
        }
    }

    val timeLeftMs = (lastScratchTime + (breakTimeMinutes * 60 * 1000)) - System.currentTimeMillis()
    val isRelaxing = timeLeftMs > 0
    val scratchesLeft = dailyLimit - userScratchesCount

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Scratch & Win", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Balance Card
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Current Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "৳${String.format("%.2f", userBalance)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Scratch the card below to reveal your bonus!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEnabled) {
                Text(
                    text = "This feature is currently disabled.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else if (isRelaxing) {
                var countdown by remember { mutableStateOf(timeLeftMs / 1000) }
                LaunchedEffect(isRelaxing) {
                    while (countdown > 0) {
                        kotlinx.coroutines.delay(1000)
                        countdown--
                    }
                }
                
                Text(
                    text = "Break Time: ${countdown / 60}m ${countdown % 60}s",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else if (scratchesLeft <= 0) {
                Text(
                    text = "You've reached your daily limit. Come back tomorrow!",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // The Scratch Card Implementation
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                // Background (The Reward)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Congratulations!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(
                        "৳${String.format("%.2f", scratchCardAmount)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50)
                    )
                    Text("Bonus Point Added", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                // Scratch Overlay
                val path = remember { mutableStateListOf<Offset>() }
                
                LaunchedEffect(isScratched) {
                    if (!isScratched) {
                        path.clear()
                    }
                }
                
                if (!isScratched && isEnabled && !isRelaxing && scratchesLeft > 0) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        path.add(startOffset)
                                    }
                                ) { change, _ ->
                                    change.consume()
                                    path.add(change.position)
                                    
                                    if (path.size > 80 && !isScratched) {
                                        isScratched = true
                                    }
                                }
                            }
                    ) {
                        // Drawing the scratchable layer (the gold cover)
                        drawRect(
                            color = Color(0xFFFFD700),
                            size = Size(size.width, size.height)
                        )
                        
                        // Decorative pattern on top
                        val dotRadius = 4.dp.toPx()
                        for (x in 0..size.width.toInt() step 60) {
                            for (y in 0..size.height.toInt() step 60) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.2f),
                                    radius = dotRadius,
                                    center = Offset(x.toFloat(), y.toFloat())
                                )
                            }
                        }

                        // Erasing parts with transparency (Scratch effect)
                        path.forEach { pathOffset ->
                            drawCircle(
                                color = Color.Transparent,
                                radius = 60f,
                                center = pathOffset,
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
            
            if (isScratched) {
                Button(
                    onClick = {
                        if (isSavingReward) return@Button
                        isSavingReward = true
                        val activity = context as? Activity
                        if (rewardedAd != null && activity != null) {
                            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    rewardedAd = null
                                    loadAd()
                                    saveReward(db, userId, scratchCardAmount) {
                                        isSavingReward = false
                                        showRewardDialog = true
                                    }
                                }
                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    rewardedAd = null
                                    loadAd()
                                    saveReward(db, userId, scratchCardAmount) {
                                        isSavingReward = false
                                        showRewardDialog = true
                                    }
                                }
                            }
                            rewardedAd?.show(activity) { _ -> }
                        } else {
                            saveReward(db, userId, scratchCardAmount) {
                                isSavingReward = false
                                showRewardDialog = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    enabled = isEnabled && !isRelaxing && scratchesLeft > 0 && !isSavingReward
                ) {
                    Text(if (isSavingReward) "Please wait..." else "Collect Bonus")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start scratching to win!", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }

    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRewardDialog = false
                isScratched = false
                val newIndex = userScratchesCount % rewardsList.size
                scratchCardAmount = rewardsList[newIndex]
            },
            confirmButton = {
                Button(onClick = { 
                    showRewardDialog = false
                    isScratched = false
                    val newIndex = userScratchesCount % rewardsList.size
                    scratchCardAmount = rewardsList[newIndex]
                }) {
                    Text("Great!")
                }
            },
            title = { Text("Reward Claimed! 🎁") },
            text = { Text("You have successfully scratched and won ৳${String.format("%.2f", scratchCardAmount)} bonus!") }
        )
    }
}

private fun saveReward(db: FirebaseFirestore, userId: String, amount: Double, onComplete: () -> Unit) {
    if (userId.isEmpty()) return
    db.runTransaction { transaction ->
        val userRef = db.collection("users").document(userId)
        val snapshot = transaction.get(userRef)
        val currentBal = snapshot.getDouble("balance") ?: 0.0
        val currentCount = snapshot.getLong("daily_scratches_count")?.toInt() ?: 0
        
        transaction.update(userRef, "balance", currentBal + amount)
        transaction.update(userRef, "daily_scratches_count", currentCount + 1)
        transaction.update(userRef, "last_scratch_timestamp", System.currentTimeMillis())
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            com.example.utils.ReferralCommissionHelper.applyCommission(userId, amount)
        }
        onComplete()
    }
}
