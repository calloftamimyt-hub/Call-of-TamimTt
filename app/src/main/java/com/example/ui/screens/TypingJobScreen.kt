package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.AdMobManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingJobScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = androidx.compose.ui.platform.LocalView.current

    com.example.ui.screens.WhiteStatusBarFix()
    
    var generatedText by remember { mutableStateOf(generateRandomString(8)) }
    var userInput by remember { mutableStateOf("") }
    var completedCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }

    // Dynamic settings loaded from Firestore
    var rewardAmount by remember { mutableStateOf(2.50) }
    var breakDuration by remember { mutableStateOf(25) }
    var lastTypingTime by remember { mutableStateOf(0L) }
    var isLoadingSettings by remember { mutableStateOf(true) }
    var isSubmittingTransaction by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val currentUserUid = UserSession.getUid(context)

    // Listen to typing configurations in real-time independently of user authentication timing
    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("settings").document("typing_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TypingJobScreen", "Settings listen failed", error)
                    android.widget.Toast.makeText(context, "টাইপিং সেটিংস আপডেট ব্যর্থ বা অনুমতি নেই: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    isLoadingSettings = false
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val rawReward = snapshot.get("reward_amount")
                    rewardAmount = when (rawReward) {
                        is Number -> rawReward.toDouble()
                        is String -> rawReward.toDoubleOrNull() ?: 2.50
                        else -> snapshot.getDouble("reward_amount") ?: 2.50
                    }
                    
                    val rawBreak = snapshot.get("break_duration")
                    breakDuration = when (rawBreak) {
                        is Number -> rawBreak.toInt()
                        is String -> rawBreak.toIntOrNull() ?: 25
                        else -> snapshot.getLong("break_duration")?.toInt() ?: 25
                    }
                }
                isLoadingSettings = false
            }
    }

    // Listen to current user typing progress/time limits
    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.w("TypingJobScreen", "User listen failed: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        lastTypingTime = when (val value = snapshot.get("lastTypingTime")) {
                            is Number -> value.toLong()
                            is String -> value.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                    }
                }
        }
    }

    // Preload ad when screen starts
    LaunchedEffect(Unit) {
        AdMobManager.loadRewardedAd(context)
    }

    // Live countdown computation
    var remainingSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(lastTypingTime, breakDuration) {
        while (true) {
            val totalBreakMs = breakDuration.toLong() * 60L * 1000L
            val elapsedMs = System.currentTimeMillis() - lastTypingTime
            val diffMs = totalBreakMs - elapsedMs
            if (diffMs > 0) {
                remainingSeconds = diffMs / 1000
            } else {
                remainingSeconds = 0
            }
            delay(1000)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            com.example.ui.screens.BeautifulHeader {
                TopAppBar(
                    title = { Text("Typing Job", fontWeight = FontWeight.Bold, color = Color.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black
                    )
                )
            }
        }
    ) { paddingValues ->
        if (isLoadingSettings) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (remainingSeconds > 0) {
            // RENDERING PREMIUM BREAK ACTIVE SCREEN WITH SECONDS ACCURACY countdown
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "টাইপিং জবের ব্রেক টাইম চলছে!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "আপনি ৫টি কাজ সফলভাবে সম্পন্ন করেছেন। দয়া করে ব্রেক টাইম শেষ হওয়া পর্যন্ত অপেক্ষা করুন এবং আবারও টাইপ করুন।",
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
                            color = MaterialTheme.colorScheme.primary
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stats Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Progress", style = MaterialTheme.typography.labelMedium)
                            Text("$completedCount / 5", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reward Pool", style = MaterialTheme.typography.labelMedium)
                            Text("৳${String.format("%.2f", rewardAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Type the exact text below", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(16.dp))

                // Text to copy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = generatedText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = userInput,
                    onValueChange = { 
                        userInput = it
                        errorMessage = "" 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Type here...") },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmittingTransaction
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isSubmittingTransaction) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (userInput == generatedText) {
                                completedCount++
                                userInput = ""
                                if (completedCount >= 5) {
                                    isSubmittingTransaction = true
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
                                        tx.update(uDocRef, "lastTypingTime", System.currentTimeMillis())
                                        tx.update(uDocRef, "lastAdCategoryTaskTime", System.currentTimeMillis())
                                        
                                        val nDoc = db.collection("notifications").document()
                                        val notifyData = hashMapOf(
                                            "id" to nDoc.id,
                                            "userId" to currentUserUid,
                                            "title" to "Typing Reward Earned ⌨️",
                                            "message" to "অভিনন্দন! আপনি ৫টি রাইটিং সফলভাবে টাইপ করে ৳${String.format("%.2f", rewardAmount)} বোনাস পেয়েছেন।",
                                            "type" to "SUCCESS",
                                            "isRead" to false,
                                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )
                                        tx.set(nDoc, notifyData)
                                    }.addOnCompleteListener { taskResult ->
                                        if (taskResult.isSuccessful) {
                                            com.example.utils.ReferralCommissionHelper.applyCommission(currentUserUid, rewardAmount)
                                        }
                                        isSubmittingTransaction = false
                                        completedCount = 0
                                        generatedText = generateRandomString(8)
                                    }
                                } else {
                                    generatedText = generateRandomString(8)
                                }
                            } else {
                                errorMessage = "Text does not match. Try again."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = userInput.isNotBlank() && !isSubmittingTransaction
                    ) {
                        Text("Submit Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                // Admin Note Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Warning, contentDescription = "Info", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "এডমিন নোট: প্রতিটি ৫টি টাইপিং টাস্ক সফলভাবে সম্পন্ন করার মাধ্যমে আপনি ৳${String.format("%.2f", rewardAmount)} বোনাস ব্যালেন্স অর্জন করবেন এবং পরবর্তী পেতে $breakDuration মিনিট অপেক্ষা করতে হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun generateRandomString(length: Int): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
