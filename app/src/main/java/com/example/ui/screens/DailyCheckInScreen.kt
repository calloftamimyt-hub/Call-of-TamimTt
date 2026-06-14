package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import android.widget.Toast
import com.example.NotificationHelper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = UserSession.getUid(context)

    var amount by remember { mutableStateOf(2.0) }
    var isEnabled by remember { mutableStateOf(true) }
    var lastCheckInDate by remember { mutableStateOf("") }
    var isCheckingIn by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

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

    DisposableEffect(userId) {
        if (userId.isEmpty()) {
            onDispose {}
        } else {
            fun parseAmount(snapshot: com.google.firebase.firestore.DocumentSnapshot?): Double? {
                if (snapshot == null || !snapshot.exists()) return null
                val possibleKeys = listOf("amount", "checkin_reward", "reward_amount", "bonus_amount")
                for (key in possibleKeys) {
                    val value = snapshot.get(key)
                    if (value is Number) return value.toDouble()
                    if (value is String) {
                        val d = value.toDoubleOrNull()
                        if (d != null) return d
                    }
                }
                return null
            }

            val listener1 = db.collection("settings").document("daily_checkin").addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val amt = parseAmount(snapshot)
                    if (amt != null) {
                        amount = amt
                    }
                    isEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: isEnabled
                }
            }

            val listener2 = db.collection("settings").document("checkin_settings").addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val amt = parseAmount(snapshot)
                    if (amt != null) {
                        amount = amt
                    }
                    isEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: isEnabled
                }
            }

            // Fetch user profile for last check-in date
            val userListener = db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    lastCheckInDate = snapshot.getString("last_checkin_date") ?: ""
                }
            }

            onDispose {
                listener1.remove()
                listener2.remove()
                userListener.remove()
            }
        }
    }

    com.example.ui.screens.WhiteStatusBarFix()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            com.example.ui.screens.BeautifulHeader {
                TopAppBar(
                    title = { Text("Daily Check-in", fontWeight = FontWeight.Bold, color = Color.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isEnabled) {
                Text(
                    text = "Daily Check-in is currently disabled by Admin.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                return@Scaffold
            }

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Daily Rewards!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "প্রতিদিন লগইন করুন এবং জিতে নিন নিশ্চিত পুরস্কার (৳$amount)। আপনার ব্যালেন্সে পুরস্কার যোগ করতে নিচে দেওয়া বাটনে ক্লিক করুন।",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                color = Color.Gray,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            val alreadyCheckedIn = lastCheckInDate == todayDate

            fun proceedToCheckIn() {
                isCheckingIn = true
                db.runTransaction { transaction ->
                    val userRef = db.collection("users").document(userId)
                    val snapshot = transaction.get(userRef)
                    
                    val currentBalance = when (val v = snapshot.get("balance")) {
                        is Number -> v.toDouble()
                        is String -> v.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    
                    val claimAmount = if (amount <= 0.0) 2.0 else amount
                    transaction.update(userRef, "balance", currentBalance + claimAmount)
                    transaction.update(userRef, "last_checkin_date", todayDate)
                }.addOnSuccessListener {
                    val claimAmount = if (amount <= 0.0) 2.0 else amount
                    com.example.utils.ReferralCommissionHelper.applyCommission(userId, claimAmount)
                    isCheckingIn = false
                    lastCheckInDate = todayDate
                    showSuccessMessage = "অভিনন্দন! আপনি সফলভাবে ৳$claimAmount পুরস্কার পেয়েছেন।"
                    
                    // Show local system notification (works even when app is closed, locked, screen is off, etc.)
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Daily Check-In Reward Claimed! 🎉",
                        message = "৳$claimAmount successfully added to your wallet balance."
                    )
                    
                    // Show immediate toast message
                    Toast.makeText(context, "৳$claimAmount আপনার অ্যাকাউন্টে সফলভাবে যোগ করা হয়েছে!", Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    isCheckingIn = false
                    showErrorMessage = "ব্যর্থ হয়েছে: ${it.localizedMessage}"
                }
            }

            if (isCheckingIn) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (!alreadyCheckedIn) {
                            val activity = context as? Activity
                            if (rewardedAd != null && activity != null) {
                                isCheckingIn = true
                                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        rewardedAd = null
                                        loadAd()
                                        proceedToCheckIn()
                                    }
                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        rewardedAd = null
                                        loadAd()
                                        proceedToCheckIn()
                                    }
                                }
                                rewardedAd?.show(activity) { _ -> }
                            } else {
                                proceedToCheckIn()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !alreadyCheckedIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alreadyCheckedIn) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (alreadyCheckedIn) "Already Claimed Today" else "Claim Reward Now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (alreadyCheckedIn) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "You're all caught up for today!", color = Color(0xFF4CAF50))
                }
            }

            if (showSuccessMessage != null) {
                AlertDialog(
                    onDismissRequest = { showSuccessMessage = null },
                    confirmButton = {
                        TextButton(onClick = { showSuccessMessage = null }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Success!") },
                    text = { Text(showSuccessMessage!!) }
                )
            }

            if (showErrorMessage != null) {
                AlertDialog(
                    onDismissRequest = { showErrorMessage = null },
                    confirmButton = {
                        TextButton(onClick = { showErrorMessage = null }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Error") },
                    text = { Text(showErrorMessage!!) }
                )
            }
        }
    }
}
