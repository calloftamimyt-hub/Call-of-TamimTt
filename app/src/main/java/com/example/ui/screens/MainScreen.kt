package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.AdMobManager
import kotlinx.coroutines.launch

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        route = "home"
    ),
    BottomNavItem(
        title = "Tools",
        selectedIcon = Icons.Filled.Handyman,
        unselectedIcon = Icons.Outlined.Handyman,
        route = "tools"
    ),
    BottomNavItem(
        title = "Shop",
        selectedIcon = Icons.Filled.Store,
        unselectedIcon = Icons.Outlined.Store,
        route = "shop"
    ),
    BottomNavItem(
        title = "Wallet",
        selectedIcon = Icons.Filled.Wallet,
        unselectedIcon = Icons.Outlined.Wallet,
        route = "wallet"
    ),
    BottomNavItem(
        title = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        route = "profile"
    )
)

@Composable
fun MainScreen(onLogout: () -> Unit) {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(84.dp)
                    .navigationBarsPadding(),
                tonalElevation = 0.dp,
                containerColor = Color.White
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray
                        ),
                        icon = {
                            Box(modifier = Modifier.padding(top = 16.dp)) {
                                Icon(
                                    imageVector = if (index == selectedItemIndex) {
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding).fillMaxSize()) {
            when (selectedItemIndex) {
                0 -> HomeScreen(onLogout = onLogout)
                1 -> ToolsScreen()
                2 -> ShopScreen()
                3 -> WalletScreen()
                4 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars), 
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "$title Screen coming soon.", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ProfileListItem(
    icon: ImageVector,
    title: String,
    iconBgColor: Color,
    iconTint: Color = Color.White,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = Color(0xFFF5F5F5).copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    
    var adsWatched by remember { mutableStateOf(0) }
    var showingAdProgressDialog by remember { mutableStateOf(false) }
    var lastCheckInTime by remember { mutableStateOf(0L) }
    var lastCheckInDate by remember { mutableStateOf("") }
    var dailyRewardAmount by remember { androidx.compose.runtime.mutableDoubleStateOf(2.0) }
    var referRewardAmount by remember { androidx.compose.runtime.mutableDoubleStateOf(10.0) }
    var isReferEnabled by remember { mutableStateOf(true) }
    var myReferralCode by remember { mutableStateOf("") }
    val requiredAdsForReward = 3
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
    // Dynamic settings & user metrics loaded from Firestore
    val currentUserUid = UserSession.getUid(context)
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    val processedDepositIds = remember { androidx.compose.runtime.mutableStateListOf<String>() }
    val processedWithdrawalIds = remember { androidx.compose.runtime.mutableStateListOf<String>() }

    // Listen to real Firestore User Record & Settings
    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            // 1. Listen to Profile Screen User document
            db.collection("users").document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        firstName = snapshot.getString("firstName") ?: ""
                        lastName = snapshot.getString("lastName") ?: ""
                        mobile = snapshot.getString("mobile") ?: ""
                        lastCheckInDate = snapshot.getString("last_checkin_date") ?: ""
                        balance = when (val value = snapshot.get("balance")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        
                        // Generates backward-compatible referral code if missing
                        val savedRef = snapshot.getString("myReferralCode") ?: ""
                        if (savedRef.isBlank() || savedRef == "null") {
                            val generatedCode = (1..8).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
                            myReferralCode = generatedCode
                            db.collection("users").document(currentUserUid).update("myReferralCode", generatedCode)
                        } else {
                            myReferralCode = savedRef
                        }
                    }
                    isLoadingProfile = false
                }

            // 2. Listen to dynamic checkin settings
            db.collection("settings").document("checkin_settings")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        val reward = when (val value = snapshot.get("checkin_reward")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 2.0
                            else -> when (val v2 = snapshot.get("reward_amount")) {
                                is Number -> v2.toDouble()
                                is String -> v2.toDoubleOrNull() ?: 2.0
                                else -> when (val v3 = snapshot.get("amount")) {
                                    is Number -> v3.toDouble()
                                    is String -> v3.toDoubleOrNull() ?: 2.0
                                    else -> 2.0
                                }
                            }
                        }
                        dailyRewardAmount = reward
                    }
                }

            db.collection("settings").document("daily_checkin")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        val reward = when (val value = snapshot.get("amount")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 2.0
                            else -> when (val v2 = snapshot.get("reward_amount")) {
                                is Number -> v2.toDouble()
                                is String -> v2.toDoubleOrNull() ?: 2.0
                                else -> when (val v3 = snapshot.get("checkin_reward")) {
                                    is Number -> v3.toDouble()
                                    is String -> v3.toDoubleOrNull() ?: 2.0
                                    else -> 2.0
                                }
                            }
                        }
                        dailyRewardAmount = reward
                    }
                }

            // 3. Listen to dynamic referral settings
            db.collection("settings").document("refer_settings")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        val reward = when (val value = snapshot.get("refer_reward")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 10.0
                            else -> when (val v2 = snapshot.get("reward_amount")) {
                                is Number -> v2.toDouble()
                                is String -> v2.toDoubleOrNull() ?: 10.0
                                else -> when (val v3 = snapshot.get("bonus_amount")) {
                                    is Number -> v3.toDouble()
                                    is String -> v3.toDoubleOrNull() ?: 10.0
                                    else -> 10.0
                                }
                            }
                        }
                        referRewardAmount = reward
                        isReferEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: isReferEnabled
                    }
                }

            db.collection("settings").document("referral")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        val reward = when (val value = snapshot.get("bonus_amount")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 10.0
                            else -> when (val v2 = snapshot.get("reward_amount")) {
                                is Number -> v2.toDouble()
                                is String -> v2.toDoubleOrNull() ?: 10.0
                                else -> when (val v3 = snapshot.get("refer_reward")) {
                                    is Number -> v3.toDouble()
                                    is String -> v3.toDoubleOrNull() ?: 10.0
                                    else -> 10.0
                                }
                            }
                        }
                        referRewardAmount = reward
                        isReferEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: isReferEnabled
                    }
                }

            // 4. Automatic Balance Adjustment for approved deposits and rejected/approved withdrawals
            // This register handles automatic adjustments of the wallet balance and generates in-app notifications.
            
            // Handle Approved Deposits
            db.collection("deposits")
                .whereEqualTo("userId", currentUserUid)
                .whereEqualTo("status", "approved")
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.documents?.forEach { doc ->
                        val docId = doc.id
                        if (doc.getBoolean("processed_for_balance") != true && !processedDepositIds.contains(docId)) {
                            val amount = when (val v = doc.get("amount")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val target = doc.getString("depositTarget") ?: "General"
                            if (amount > 0) {
                                processedDepositIds.add(docId)
                                db.runTransaction { tx ->
                                    val userRef = db.collection("users").document(currentUserUid)
                                    val userSnap = tx.get(userRef)
                                    
                                    val fieldToUpdate = when (target) {
                                        "Recharge" -> "rechargeBalance"
                                        "General" -> "deposited"
                                        else -> "balance"
                                    }
                                    
                                    val currentVal = when (val value = userSnap.get(fieldToUpdate)) {
                                        is Number -> value.toDouble()
                                        is String -> value.toDoubleOrNull() ?: 0.0
                                        else -> 0.0
                                    }
                                    
                                    tx.update(userRef, fieldToUpdate, currentVal + amount)
                                    tx.update(doc.reference, "processed_for_balance", true)
                                    tx.update(doc.reference, "claimed_at", com.google.firebase.firestore.FieldValue.serverTimestamp())
                                }.addOnSuccessListener {
                                    com.example.NotificationHelper.showNotification(
                                        context = context,
                                        title = "ডিপোজিট অ্যাপ্রুভ হয়েছে! ✅",
                                        message = "আপনার ৳$amount ডিপোজিট রিকোয়েস্ট অ্যাপ্রুভ করা হয়েছে এবং ব্যালেন্স আপনার অ্যাকাউন্টে যোগ করা হয়েছে।",
                                        type = com.example.ui.screens.NotificationType.SUCCESS
                                    )
                                }.addOnFailureListener { err ->
                                    processedDepositIds.remove(docId)
                                    android.util.Log.e("MainScreen", "Transaction failed to process approved deposit: ", err)
                                }
                            }
                        }
                    }
                }

            // Handle Rejected Deposits
            db.collection("deposits")
                .whereEqualTo("userId", currentUserUid)
                .whereEqualTo("status", "rejected")
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.documents?.forEach { doc ->
                        val docId = doc.id
                        if (doc.getBoolean("processed_for_reject") != true) {
                            val amount = when (val v = doc.get("amount")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            db.runTransaction { tx ->
                                tx.update(doc.reference, "processed_for_reject", true)
                            }.addOnSuccessListener {
                                com.example.NotificationHelper.showNotification(
                                    context = context,
                                    title = "ডিপোজিট বাতিল হয়েছে! ❌",
                                    message = "আপনার ৳$amount ডিপোজিট রিকোয়েস্টটি বাতিল করা হয়েছে। ট্রানজেকশন আইডি ভালো করে চেক করে আবার রিকোয়েস্ট করুন।",
                                    type = com.example.ui.screens.NotificationType.ERROR
                                )
                            }.addOnFailureListener { err ->
                                android.util.Log.e("MainScreen", "Transaction failed to process rejected deposit: ", err)
                                doc.reference.update("processed_for_reject", true)
                            }
                        }
                    }
                }

            // Handle Rejected Withdrawals (Return funds and notify)
            db.collection("withdrawals")
                .whereEqualTo("userId", currentUserUid)
                .whereEqualTo("status", "rejected")
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.documents?.forEach { doc ->
                        val docId = doc.id
                        if (doc.getBoolean("amount_deducted") == true && doc.getBoolean("processed_for_balance") != true && !processedWithdrawalIds.contains(docId)) {
                            val amount = when (val v = doc.get("amount")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            if (amount > 0) {
                                processedWithdrawalIds.add(docId)
                                db.runTransaction { tx ->
                                    val userRef = db.collection("users").document(currentUserUid)
                                    val userSnap = tx.get(userRef)
                                    val currentBal = when (val bal = userSnap.get("balance")) {
                                        is Number -> bal.toDouble()
                                        is String -> bal.toDoubleOrNull() ?: 0.0
                                        else -> 0.0
                                    }
                                    tx.update(userRef, "balance", currentBal + amount)
                                    tx.update(doc.reference, "processed_for_balance", true)
                                    tx.update(doc.reference, "claimed_at", com.google.firebase.firestore.FieldValue.serverTimestamp())
                                }.addOnSuccessListener {
                                    com.example.NotificationHelper.showNotification(
                                        context = context,
                                        title = "উইথড্র রিকোয়েস্ট বাতিল হয়েছে! ❌",
                                        message = "আপনার ৳$amount উইথড্র রিকোয়েস্টটি বাতিল করা হয়েছে এবং টাকা আপনার ব্যালেন্সে ফেরত দেওয়া হয়েছে।",
                                        type = com.example.ui.screens.NotificationType.ERROR
                                    )
                                }.addOnFailureListener { err ->
                                    processedWithdrawalIds.remove(docId)
                                    android.util.Log.e("MainScreen", "Transaction failed to process rejected withdrawal: ", err)
                                }
                            }
                        }
                    }
                }

            // Handle Approved Withdrawals (Generate approval notification for user)
            db.collection("withdrawals")
                .whereEqualTo("userId", currentUserUid)
                .whereEqualTo("status", "approved")
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.documents?.forEach { doc ->
                        if (doc.getBoolean("processed_for_approval_notification") != true) {
                            val amount = when (val v = doc.get("amount")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            db.runTransaction { tx ->
                                tx.update(doc.reference, "processed_for_approval_notification", true)
                            }.addOnSuccessListener {
                                // Send local push notification
                                com.example.NotificationHelper.showNotification(
                                    context = context,
                                    title = "উইথড্র সফল হয়েছে! 🎁",
                                    message = "আপনার ৳$amount উইথড্র রিকোয়েস্টটি সফলভাবে সম্পন্ন হয়েছে।",
                                    type = com.example.ui.screens.NotificationType.SUCCESS
                                )
                            }.addOnFailureListener {
                                doc.reference.update("processed_for_approval_notification", true).addOnSuccessListener {
                                    // Send local push notification
                                    com.example.NotificationHelper.showNotification(
                                        context = context,
                                        title = "উইথড্র সফল হয়েছে! 🎁",
                                        message = "আপনার ৳$amount উইথড্র রিকোয়েস্টটি সফলভাবে সম্পন্ন হয়েছে।",
                                        type = com.example.ui.screens.NotificationType.SUCCESS
                                    )
                                }
                            }
                        }
                    }
                }
        } else {
            isLoadingProfile = false
        }
    }

    var showEditProfile by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AdMobManager.loadRewardedAd(context)
    }

    fun handleAdReward() {
        if (activity != null) {
            showingAdProgressDialog = true
            AdMobManager.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    adsWatched++
                    if (adsWatched >= requiredAdsForReward) {
                        showingAdProgressDialog = false
                        adsWatched = 0
                        lastCheckInTime = System.currentTimeMillis()
                        
                        // Dynamically update user balance in Firestore
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val uDocRef = db.collection("users").document(currentUserUid)
                        db.runTransaction { tx ->
                            val userSnap = tx.get(uDocRef)
                            val currentBalance = when (val v = userSnap.get("balance")) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            tx.update(uDocRef, "balance", currentBalance + dailyRewardAmount)
                        }.addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                snackbarMessage = "ডেইলি রিওয়ার্ড সফলভাবে দাবি করেছেন! ৳$dailyRewardAmount আপনার ওয়ালেটে যোগ হয়েছে।"
                            } else {
                                snackbarMessage = "রিওয়ার্ড যোগ করতে ব্যর্থ হয়েছে!"
                            }
                        }
                    } else {
                        showingAdProgressDialog = true
                    }
                },
                onAdDismissed = {
                    showingAdProgressDialog = false
                    if (adsWatched < requiredAdsForReward) {
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
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ad Progress: $adsWatched / $requiredAdsForReward watched. \nLoading next ad required for reward...",
                        textAlign = TextAlign.Center,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { handleAdReward() }) {
                        Text("Show Next Ad")
                    }
                }
            }
        }
    }

    // Showing Snackbar message
    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
    }

    // Modern Confirm Delete Account Dialog Block
    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("একাউন্ট ডিলেট করুন", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.Red) },
            text = { Text("আপনি কি নিশ্চিত যে আপনার অ্যাকাউন্টটি স্থায়ীভাবে ডিলিট করতে চান? ডিলিট করার সাথে সাথেই আপনার সব ব্যালেন্স, রেফারাল এবং একাউন্ট তথ্য সম্পূর্ণভাবে ফায়ারস্টোর সার্ভার থেকে মুছে ফেলা হবে।", lineHeight = 20.sp) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        // 1. Hard Delete User document from Firestore Database
                        db.collection("users").document(currentUserUid).delete()
                            .addOnCompleteListener { dbTask ->
                                // 2. Trigger Firebase Auth runtime deletion
                                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                user?.delete()?.addOnCompleteListener {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                } ?: run {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            }
                    }
                ) {
                    Text("হ্যাঁ, ডিলেট করুন", color = Color.Red, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল করুন")
                }
            }
        )
    }

    // FULL SCREEN PROFILE REWRITE DIALOG
    if (showEditProfile) {
        Dialog(
            onDismissRequest = { showEditProfile = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            com.example.ui.screens.FullScreenDialogModifier()
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color.White
            ) {
                var editFirstName by remember { mutableStateOf(firstName) }
                var editLastName by remember { mutableStateOf(lastName) }
                var editMobile by remember { mutableStateOf(mobile) }
                var isSaving by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        androidx.compose.material3.TopAppBar(
                            title = { Text("এডিট প্রোফাইল", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            navigationIcon = {
                                androidx.compose.material3.IconButton(onClick = { showEditProfile = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { padValues ->
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padValues)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = editFirstName,
                            onValueChange = { editFirstName = it },
                            label = { Text("First Name (প্রথম নাম)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = editLastName,
                            onValueChange = { editLastName = it },
                            label = { Text("Last Name (শেষ নাম)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                        androidx.compose.material3.OutlinedTextField(
                            value = editMobile,
                            onValueChange = { editMobile = it },
                            label = { Text("Mobile Number (মোবাইল নম্বর)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            )
                        )

                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(40.dp))

                        if (isSaving) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = {
                                    isSaving = true
                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    db.collection("users").document(currentUserUid)
                                        .update(
                                            "firstName", editFirstName,
                                            "lastName", editLastName,
                                            "mobile", editMobile
                                        ).addOnCompleteListener { updateTask ->
                                            isSaving = false
                                            if (updateTask.isSuccessful) {
                                                firstName = editFirstName
                                                lastName = editLastName
                                                mobile = editMobile
                                                snackbarMessage = "প্রোফাইল সফলভাবে আপডেট করা হয়েছে! ✅"
                                                showEditProfile = false
                                            } else {
                                                snackbarMessage = "আপডেট ব্যর্থ হয়েছে: ❌ ${updateTask.exception?.localizedMessage}"
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = editFirstName.isNotBlank() && editLastName.isNotBlank() && editMobile.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = "Save"
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Profile", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    var showSettings by remember { mutableStateOf(false) }
    var showDailyCheckInFullScreen by remember { mutableStateOf(false) }
    var showReferEarnFullScreen by remember { mutableStateOf(false) }

    if (showDailyCheckInFullScreen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDailyCheckInFullScreen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            com.example.ui.screens.FullScreenDialogModifier()
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color.White
            ) {
                DailyCheckInScreen(onBack = { showDailyCheckInFullScreen = false })
            }
        }
    }

    if (showReferEarnFullScreen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showReferEarnFullScreen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            com.example.ui.screens.FullScreenDialogModifier()
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color.White
            ) {
                ReferAndEarnScreen(onBack = { showReferEarnFullScreen = false })
            }
        }
    }

    if (showSettings) {
        com.example.ui.screens.SettingsScreen(onBack = { showSettings = false })
    }


    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Curved top banner matching the screenshot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            color = Color(0xFF2196F3),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                ) {
                    // Left Decorator Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-20).dp, y = (-10).dp)
                            .background(Color.White.copy(alpha = 0.12f), androidx.compose.foundation.shape.CircleShape)
                    )
                    // Right Decorator Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-5).dp)
                            .background(Color.White.copy(alpha = 0.12f), androidx.compose.foundation.shape.CircleShape)
                    )
                }

                // Center overlapping Avatar Frame
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(64.dp),
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (isLoadingProfile) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "${firstName} ${lastName}".trim().ifEmpty { "User" },
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = mobile.ifEmpty { "Mobile number not found" },
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Balance capsule badge
                androidx.compose.material3.Surface(
                    color = Color(0xFF2196F3).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Wallet,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Current Balance: ৳${String.format("%.2f", balance)}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val currentTime = System.currentTimeMillis()
                val msIn24Hours = 24 * 60 * 60 * 1000
                val canCheckIn = (currentTime - lastCheckInTime) > msIn24Hours

                // 1. Edit Profile
                ProfileListItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    iconBgColor = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF1E88E5),
                    onClick = { showEditProfile = true }
                )

                // 2. Settings
                ProfileListItem(
                    icon = Icons.Default.Settings,
                    title = "System Settings",
                    iconBgColor = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF43A047),
                    onClick = { showSettings = true }
                )
                
                // 3. Daily Check-in
                val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                val isTodayCheckedIn = lastCheckInDate == todayDate
                ProfileListItem(
                    icon = Icons.Filled.CardGiftcard,
                    title = "Daily Check-in",
                    iconBgColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFFB8C00),
                    trailingText = if (isTodayCheckedIn) "Completed" else "৳$dailyRewardAmount",
                    onClick = { showDailyCheckInFullScreen = true }
                )
                
                // 4. Refer & Earn
                ProfileListItem(
                    icon = Icons.Filled.Share,
                    title = "Refer & Earn",
                    iconBgColor = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF8E24AA),
                    trailingText = if (isReferEnabled) "৳$referRewardAmount" else "Disabled",
                    onClick = { showReferEarnFullScreen = true }
                )

                // 5. Logout
                ProfileListItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Log Out",
                    iconBgColor = Color(0xFFECEFF1),
                    iconTint = Color(0xFF546E7A),
                    onClick = {
                        UserSession.clearSession(context)
                        onLogout()
                    }
                )

                // 6. Delete Account
                ProfileListItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    iconBgColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFE53935),
                    onClick = { showDeleteConfirm = true }
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
    
    // SnackbarHost
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        if (snackbarMessage != null) {
            SnackbarHost(hostState = remember { SnackbarHostState() }.apply { 
                LaunchedEffect(snackbarMessage) { showSnackbar(snackbarMessage!!) }
            })
        }
    }
}

