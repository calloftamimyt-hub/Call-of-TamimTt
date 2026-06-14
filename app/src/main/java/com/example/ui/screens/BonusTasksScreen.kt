package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.BonusHistoryEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonusTasksScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // State
    var bonusBalance by remember { mutableStateOf(0.0) }
    var totalReferrals by remember { mutableStateOf(0) }
    var activeReferrals by remember { mutableStateOf(0) }

    var todayBonus by remember { mutableStateOf(0.0) }
    var thisWeekBonus by remember { mutableStateOf(0.0) }
    var thisMonthBonus by remember { mutableStateOf(0.0) }

    var historyList by remember { mutableStateOf(emptyList<BonusHistoryEntity>()) }

    val database = remember { AppDatabase.getDatabase(context) }

    var rules by remember { mutableStateOf("Loading rules...") }

    // Fetch user stats
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            val userRef = db.collection("users").document(uid)
            userRef.addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    bonusBalance = snapshot.getDouble("bonusBalance") ?: 0.0
                    totalReferrals = snapshot.getLong("totalReferral")?.toInt() ?: snapshot.getLong("referrals")?.toInt() ?: 0
                    activeReferrals = snapshot.getLong("activeReferrals")?.toInt() ?: 0
                }
            }
        }
        
        db.collection("settings").document("bonus_rules")
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val r = snap.getString("rulesText")
                    if (r != null) {
                        rules = r
                    } else {
                        // Build string experimentally from fields
                        val p = snap.getDouble("referralIncomeTrigger") ?: 100.0
                        val b = snap.getDouble("bonusAmount") ?: 1.0
                        rules = "If your referral earns ৳$p, you get ৳$b."
                    }
                } else {
                    rules = "Refer friends to get bonus rewards based on their activity!"
                }
            }
    }

    // Fetch history & calculate today/week/month stats
    LaunchedEffect(Unit) {
        database.bonusHistoryDao().getAllBonusHistory().collectLatest { list ->
            historyList = list

            val now = Calendar.getInstance()
            
            // Calculate Today
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            // Calculate This Week
            val weekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            // Calculate This Month
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            var tBonus = 0.0
            var wBonus = 0.0
            var mBonus = 0.0

            list.forEach { entity ->
                if (entity.timestamp >= todayStart) tBonus += entity.amount
                if (entity.timestamp >= weekStart) wBonus += entity.amount
                if (entity.timestamp >= monthStart) mBonus += entity.amount
            }

            todayBonus = tBonus
            thisWeekBonus = wBonus
            thisMonthBonus = mBonus
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bonus Dashboard", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Bonus Balance", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "৳${String.format("%.2f", bonusBalance)}",
                            color = Color.White,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Time based stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BonusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Today",
                        amount = todayBonus,
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32)
                    )
                    BonusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "This Week",
                        amount = thisWeekBonus,
                        containerColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFEF6C00)
                    )
                    BonusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "This Month",
                        amount = thisMonthBonus,
                        containerColor = Color(0xFFE3F2FD),
                        contentColor = Color(0xFF1565C0)
                    )
                }
            }

            // Referral Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReferralStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Referrals",
                        count = totalReferrals,
                        icon = Icons.Filled.People,
                        tint = Color(0xFF1976D2)
                    )
                    ReferralStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Referrals",
                        count = activeReferrals,
                        icon = Icons.Filled.CheckCircle,
                        tint = Color(0xFF388E3C)
                    )
                }
            }

            // Rules
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = "Rules", tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(rules, color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // History Section Text
            item {
                Text(
                    text = "Bonus History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // History List
            if (historyList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No bonus history yet.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(historyList) { history ->
                    HistoryItemCard(history)
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun BonusStatCard(modifier: Modifier = Modifier, title: String, amount: Double, containerColor: Color, contentColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = contentColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("৳${String.format("%.2f", amount)}", color = contentColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReferralStatCard(modifier: Modifier = Modifier, title: String, count: Int, icon: ImageVector, tint: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Text(count.toString(), color = Color.Black, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryItemCard(history: BonusHistoryEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${history.source} - ${history.referralUserName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
                Text(
                    text = sdf.format(Date(history.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+৳${String.format("%.2f", history.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = history.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (history.status == "Success") Color(0xFF2E7D32) else Color(0xFFF57C00)
                )
            }
        }
    }
}
