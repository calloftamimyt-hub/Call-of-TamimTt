package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
        WithdrawScreen(
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
            },
            onViewHistory = {
                showWithdrawDialog = false
                showHistoryScreen = true
            }
        )
        return
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
                                    isEarning = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                )
                            } else {
                                ModernBalanceCard(
                                    title = "Earning Balance",
                                    amount = displayedEarningBalance,
                                    isEarning = true,
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
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        // Buttons moved to the bottom of the card section
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { showDepositDialog = true },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                            ) {
                                Text("Deposit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { showWithdrawDialog = true },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1), contentColor = Color.White)
                            ) {
                                Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // (Previous Row of buttons removed here)

            item {
                Text(
                    text = "Earnings Filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val filterOptions = listOf("Today's Income", "Last 7 Days Income", "Last 30 Days Income", "Lifetime Income")
                    filterOptions.forEach { option ->
                        val isSelected = selectedEarningFilter == option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = if (isSelected) 10.dp else 1.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    spotColor = if (isSelected) Color(0xFF0D47A1) else Color.Black,
                                    ambientColor = if (isSelected) Color(0xFF0D47A1) else Color.Black
                                )
                                .clickable { selectedEarningFilter = option },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF0D47A1)) else BorderStroke(0.5.dp, Color.LightGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Wallet Icon on the left
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = if (isSelected) Color(0xFF0D47A1).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF0D47A1) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Text(
                                    text = option,
                                    color = if (isSelected) Color(0xFF0D47A1) else Color.DarkGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF0D47A1),
                                        modifier = Modifier.size(20.dp)
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
fun ModernBalanceCard(title: String, amount: Double, modifier: Modifier = Modifier, isEarning: Boolean) {
    val gradientBrush = if (isEarning) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFF021B79), Color(0xFF0575E6))
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        )
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            // Shiny metallic microchip simulator
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(38.dp, 28.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF9D423), Color(0xFFFF4E50))
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            )

            // Stylized credit/wallet card branding
            Text(
                text = if (isEarning) "EARNINGS PREFERRED" else "DEPOSIT PREMIER",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // Core card text layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "৳${String.format("%.2f", amount)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom row mimicking debit/credit cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "••••  ••••  ••••  ${if (isEarning) "9825" else "5012"}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 12.sp
                )
                
                // Overlapping circles resembling credit card network issuer logos
                Box(modifier = Modifier.width(36.dp).height(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFFFF5F00).copy(alpha = 0.85f), shape = RoundedCornerShape(100))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(20.dp)
                            .background(Color(0xFFF79E1B).copy(alpha = 0.85f), shape = RoundedCornerShape(100))
                    )
                }
            }
        }
    }
}
