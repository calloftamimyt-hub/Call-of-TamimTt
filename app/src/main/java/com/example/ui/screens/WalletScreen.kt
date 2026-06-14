package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppDatabase
import com.example.data.local.WalletHistoryEntity
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    com.example.ui.screens.WhiteStatusBarFix()

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
                .addSnapshotListener { snapshot, _ ->
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
    val cardPagerState = rememberPagerState(pageCount = { 2 })
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
            com.example.ui.screens.BeautifulHeader {
                TopAppBar(
                    title = { Text("My Wallet", fontWeight = FontWeight.Bold, color = Color.Black) },
                    actions = {
                        IconButton(onClick = { showHistoryScreen = true }) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "View History",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
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
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalPager(
                            state = cardPagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            if (page == 0) {
                                ModernBalanceCard(
                                    title = "Deposit Balance",
                                    amount = rechargeBalance + deposited,
                                    bgColor = Color(0xFF1E1E1E), // Black
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                )
                            } else {
                                ModernBalanceCard(
                                    title = "Earning Balance",
                                    amount = displayedEarningBalance,
                                    bgColor = Color(0xFF0D47A1), // Blue
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pager dots indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(2) { index ->
                                val isSelected = cardPagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 7.dp)
                                        .background(
                                            color = if (isSelected) Color(0xFF0D47A1) else Color.LightGray,
                                            shape = RoundedCornerShape(100)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (cardPagerState.currentPage == 0) "Swipe to see Earning Balance →" else "← Swipe to see Deposit Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
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
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                    ) {
                        Text("Deposit", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White)
                    ) {
                        Text("Withdraw", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { showHistoryScreen = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Filled.History, contentDescription = "History")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View History", fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Earnings Filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filterOptions = listOf("Today's Income", "Last 7 Days Income", "Last 30 Days Income", "Lifetime Income")
                    filterOptions.forEach { option ->
                        val isSelected = selectedEarningFilter == option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable { selectedEarningFilter = option },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(6.dp)
                                            .background(Color(0xFF2196F3))
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxHeight().width(6.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color(0xFF2196F3) else Color.DarkGray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ModernBalanceCard(title: String, amount: Double, modifier: Modifier = Modifier, bgColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp), // slightly rounded corners, large and modern
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "৳${String.format("%.2f", amount)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
