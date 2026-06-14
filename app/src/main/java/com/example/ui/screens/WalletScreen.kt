package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.WalletHistoryEntity
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showHistoryScreen by remember { mutableStateOf(false) }

    var balance by remember { mutableStateOf(0.0) }
    var deposited by remember { mutableStateOf(0.0) }
    var rechargeBalance by remember { mutableStateOf(0.0) }
    var isLoadingUser by remember { mutableStateOf(true) }

    val currentUserUid = UserSession.getUid(context)
    val walletHistoryDao = remember { AppDatabase.getDatabase(context).walletHistoryDao() }

    // Listen to user stats
    DisposableEffect(currentUserUid) {
        if (currentUserUid.isBlank()) {
            isLoadingUser = false
            onDispose {}
        } else {
            val listenerReg = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null && snapshot.exists()) {
                        balance = when (val value = snapshot.get("balance")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        deposited = when (val value = snapshot.get("deposited")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        rechargeBalance = when (val value = snapshot.get("rechargeBalance")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                    }
                    isLoadingUser = false
                }
            onDispose {
                listenerReg.remove()
            }
        }
    }

    // Earnings Filter State
    var selectedEarningFilter by remember { mutableStateOf("Lifetime Income") }
    var displayedEarningBalance by remember { mutableStateOf(0.0) }

    LaunchedEffect(selectedEarningFilter, balance) {
        if (selectedEarningFilter == "Lifetime Income") {
            displayedEarningBalance = balance
        } else {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            when (selectedEarningFilter) {
                "Today's Income" -> {
                    // Stays at start of today
                }
                "Last 7 Days Income" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                }
                "Last 30 Days Income" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                }
            }
            
            val localSum = walletHistoryDao.getEarningsSince(calendar.timeInMillis) ?: 0.0
            displayedEarningBalance = localSum
        }
    }

    if (showHistoryScreen) {
        WalletHistoryScreen(onBack = { showHistoryScreen = false })
        return
    }

    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onSubmitted = { amount, method ->
                coroutineScope.launch {
                    walletHistoryDao.insert(
                        WalletHistoryEntity(
                            type = "Deposit",
                            amount = amount,
                            title = "Deposit request via $method",
                            status = "Pending",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    snackbarHostState.showSnackbar("Deposit request of ৳$amount via $method submitted.")
                }
            }
        )
    }
    
    if (showWithdrawDialog) {
        WithdrawDialog(
            availableBalance = balance, // passing balance as available
            onDismiss = { showWithdrawDialog = false },
            onSubmitted = { amount, method ->
                coroutineScope.launch {
                    walletHistoryDao.insert(
                        WalletHistoryEntity(
                            type = "Withdraw",
                            amount = amount,
                            title = "Withdrawal request via $method",
                            status = "Pending",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    snackbarHostState.showSnackbar("Withdrawal request of ৳$amount via $method submitted!")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showHistoryScreen = true }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoadingUser) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModernBalanceCard(
                            title = "Deposit Balance",
                            amount = rechargeBalance + deposited,
                            modifier = Modifier.weight(1f),
                            bgGradient = Brush.linearGradient(listOf(Color(0xFF2b5876), Color(0xFF4e4376)))
                        )
                        ModernBalanceCard(
                            title = "Earning Balance",
                            amount = displayedEarningBalance,
                            modifier = Modifier.weight(1f),
                            bgGradient = Brush.linearGradient(listOf(Color(0xFF0ba360), Color(0xFF3cba92)))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showDepositDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Deposit", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Withdraw", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Earnings Filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val filterOptions = listOf("Today's Income", "Last 7 Days Income", "Last 30 Days Income", "Lifetime Income")
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions.size) { index ->
                        val option = filterOptions[index]
                        FilterChip(
                            selected = selectedEarningFilter == option,
                            onClick = { selectedEarningFilter = option },
                            label = { Text(option) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ModernBalanceCard(title: String, amount: Double, modifier: Modifier = Modifier, bgGradient: Brush) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "৳${String.format("%.2f", amount)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
