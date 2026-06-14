package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.UserSession
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferAndEarnScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val db = FirebaseFirestore.getInstance()
    
    val userId = UserSession.getUid(context)
    var referralCode by remember { mutableStateOf("") }
    var rewardAmount by remember { mutableStateOf(10.0) }
    var isEnabled by remember { mutableStateOf(true) }

    DisposableEffect(userId) {
        if (userId.isEmpty()) {
            onDispose {}
        } else {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                referralCode = doc.getString("referCode") ?: doc.getString("myReferralCode") ?: userId.take(8).uppercase()
            }
            
            fun parseReferralAmount(snapshot: com.google.firebase.firestore.DocumentSnapshot?): Double? {
                if (snapshot == null || !snapshot.exists()) return null
                val possibleKeys = listOf("bonus_amount", "refer_reward", "reward_amount")
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
            
            val listener1 = db.collection("settings").document("referral").addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val amt = parseReferralAmount(snapshot)
                    if (amt != null) {
                        rewardAmount = amt
                    }
                    isEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: isEnabled
                }
            }
            
            val listener2 = db.collection("settings").document("refer_settings").addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val amt = parseReferralAmount(snapshot)
                    if (amt != null) {
                        rewardAmount = amt
                    }
                    isEnabled = snapshot.getBoolean("is_enabled") ?: snapshot.getBoolean("enabled") ?: isEnabled
                }
            }
            
            onDispose {
                listener1.remove()
                listener2.remove()
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Refer & Earn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (!isEnabled) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "Refer & Earn is currently disabled by Admin.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFE1F5FE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "বন্ধুদের ইনভাইট করুন এবং জিতে নিন!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "আপনার রেফারেল কোড ব্যবহার করে আপনার বন্ধুদের এই অ্যাপে জয়েন করান এবং প্রতিটি সফল রেফারেলে পান ৳$rewardAmount বোনাস।",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Referral Code Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR REFERRAL CODE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = referralCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(referralCode))
                        }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Hey! Join this amazing app and earn money. Use my referral code: $referralCode \nDownload now: https://play.google.com/store/apps/details?id=${context.packageName}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "SHARE WITH FRIENDS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // How it works section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "কিভাবে কাজ করে?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                HowItWorksItem(number = "1", text = "আপনার রেফারেল কোড বন্ধুদের সাথে শেয়ার করুন।")
                HowItWorksItem(number = "2", text = "আপনার বন্ধুরা যখন একাউন্ট তৈরি করবে তখন আপনার কোড ব্যবহার করতে বলুন।")
                HowItWorksItem(number = "3", text = "সফলভাবে একাউন্ট তৈরি করলে আপনার ব্যালেন্সে ৳$rewardAmount যোগ হবে।")
            }
        }
    }
}
}

@Composable
fun HowItWorksItem(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF0288D1), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}
