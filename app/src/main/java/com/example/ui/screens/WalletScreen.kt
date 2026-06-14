package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

data class Transaction(
    val id: String,
    val type: String,
    val amount: Double,
    val date: String,
    val status: String, // Success, Pending, Failed
    val method: String,
    val notes: String,
    val timestamp: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    var balance by remember { mutableStateOf(0.0) }
    var earnings by remember { mutableStateOf(0.0) }
    var withdrawn by remember { mutableStateOf(0.0) }
    var deposited by remember { mutableStateOf(0.0) }
    var rechargeBalance by remember { mutableStateOf(0.0) }
    var isLoadingUser by remember { mutableStateOf(true) }

    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoadingTransactions by remember { mutableStateOf(true) }

    val currentUserUid = UserSession.getUid(context)

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
                        earnings = when (val value = snapshot.get("earnings")) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        withdrawn = when (val value = snapshot.get("withdrawn")) {
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

    // Listen to withdrawals and deposits combined
    DisposableEffect(currentUserUid) {
        if (currentUserUid.isBlank()) {
            isLoadingTransactions = false
            onDispose {}
        } else {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            var wList = emptyList<Transaction>()
            var dList = emptyList<Transaction>()

            fun updateMergedList() {
                transactions = (wList + dList).sortedByDescending { it.timestamp }
            }

            val wListener = db.collection("withdrawals")
                .whereEqualTo("userId", currentUserUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        wList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val amount = when (val value = doc.get("amount")) {
                                    is Number -> value.toDouble()
                                    is String -> value.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }
                                val rawStatus = doc.getString("status") ?: "pending"
                                val status = when (rawStatus.lowercase()) {
                                    "pending" -> "Pending"
                                    "approved" -> "Approved"
                                    "rejected" -> "Rejected"
                                    else -> rawStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                                }
                                val method = doc.getString("paymentMethod") ?: doc.getString("method") ?: ""
                                val accountNo = doc.getString("accountNumber") ?: doc.getString("accountNo") ?: ""
                                val ts = doc.getTimestamp("createdAt") ?: doc.getTimestamp("timestamp")
                                val dateStr = if (ts != null) {
                                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                    sdf.format(ts.toDate())
                                } else {
                                    "Just now"
                                }
                                val timeMs = ts?.toDate()?.time ?: 0L
                                Transaction(
                                    id = doc.id,
                                    type = "Withdraw",
                                    amount = amount,
                                    date = dateStr,
                                    status = status,
                                    method = method,
                                    notes = "To Account: $accountNo",
                                    timestamp = timeMs
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        updateMergedList()
                    }
                    isLoadingTransactions = false
                }

            val dListener = db.collection("deposits")
                .whereEqualTo("userId", currentUserUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        dList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val amount = when (val value = doc.get("amount")) {
                                    is Number -> value.toDouble()
                                    is String -> value.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }
                                val rawStatus = doc.getString("status") ?: "pending"
                                val status = when (rawStatus.lowercase()) {
                                    "pending" -> "Pending"
                                    "approved" -> "Approved"
                                    "rejected" -> "Rejected"
                                    else -> rawStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                                }
                                val method = doc.getString("paymentMethod") ?: doc.getString("method") ?: ""
                                val txId = doc.getString("transactionId") ?: ""
                                val ts = doc.getTimestamp("createdAt") ?: doc.getTimestamp("timestamp")
                                val dateStr = if (ts != null) {
                                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                    sdf.format(ts.toDate())
                                } else {
                                    "Just now"
                                }
                                val timeMs = ts?.toDate()?.time ?: 0L
                                Transaction(
                                    id = doc.id,
                                    type = "Deposit",
                                    amount = amount,
                                    date = dateStr,
                                    status = status,
                                    method = method,
                                    notes = "TxID: $txId",
                                    timestamp = timeMs
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        updateMergedList()
                    }
                    isLoadingTransactions = false
                }

            onDispose {
                wListener.remove()
                dListener.remove()
            }
        }
    }

    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onSubmitted = { amount, method ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Deposit request of ৳$amount via $method submitted successfully.")
                }
            }
        )
    }
    
    if (showWithdrawDialog) {
        WithdrawDialog(
            availableBalance = balance,
            onDismiss = { showWithdrawDialog = false },
            onSubmitted = { amount, method ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Withdrawal request of ৳$amount via $method submitted! Waiting for admin approval.")
                }
            }
        )
    }

    if (selectedTransaction != null) {
        TransactionDetailsDialog(transaction = selectedTransaction!!, onDismiss = { selectedTransaction = null })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.Bold) },
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
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoadingUser) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    WalletCard(balance = balance, earnings = earnings, withdrawn = withdrawn, deposited = deposited, rechargeBalance = rechargeBalance)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                QuickActions(
                    onDeposit = { showDepositDialog = true },
                    onWithdraw = { showWithdrawDialog = true },
                    onTransfer = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Transfer service is coming soon!")
                        }
                    },
                    onHistory = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("History is listed below!")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Earnings Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                EarningsOverview(lifetime = earnings)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoadingTransactions) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transaction history found.", color = Color.Gray)
                    }
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(transaction = transaction) {
                        selectedTransaction = transaction
                    }
                }
            }
        }
    }
}

@Composable
fun WalletCard(balance: Double, earnings: Double, withdrawn: Double, deposited: Double, rechargeBalance: Double = 0.0) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF000000), Color(0xFF1E1E1E))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Deposit Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "৳${String.format("%.2f", deposited)}",
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WalletStatItem("Recharge Balance", "৳${String.format("%.2f", rechargeBalance)}")
                    WalletStatItem("Earning Balance", "৳${String.format("%.2f", balance)}")
                    WalletStatItem("Withdrawal", "৳${String.format("%.2f", withdrawn)}")
                }
            }
        }
    }
}

@Composable
fun WalletStatItem(label: String, amount: String) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp
        )
        Text(
            text = amount,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun QuickActions(
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onTransfer: () -> Unit,
    onHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton("Deposit", Icons.Outlined.ArrowDownward, Color(0xFF4CAF50), onDeposit, imageUrl = "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781147015/dfb4c6odqbgymblqshz9.png")
        ActionButton("Withdraw", Icons.Outlined.ArrowUpward, Color(0xFFF44336), onWithdraw, imageUrl = "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781147015/bqkrursvklb52otbmorl.jpg")
        ActionButton("Transfer", Icons.Outlined.SwapHoriz, Color(0xFF2196F3), onTransfer)
        ActionButton("History", Icons.Filled.History, Color(0xFFFF9800), onHistory)
    }
}

@Composable
fun ActionButton(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, imageUrl: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EarningsOverview(lifetime: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EarningCard("Today", "৳0.00", Modifier.weight(1f))
            EarningCard("Weekly", "৳0.00", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EarningCard("Monthly", "৳0.00", Modifier.weight(1f))
            EarningCard("Lifetime", "৳${String.format("%.2f", lifetime)}", Modifier.weight(1f))
        }
    }
}

@Composable
fun EarningCard(title: String, amount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    val statusColor = when (transaction.status) {
        "Success", "Approved" -> Color(0xFF4CAF50)
        "Pending" -> Color(0xFFFF9800)
        "Failed", "Rejected" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val icon = when (transaction.type) {
        "Deposit" -> Icons.Outlined.ArrowDownward
        "Withdraw" -> Icons.Outlined.ArrowUpward
        "Earning" -> Icons.Filled.MonetizationOn
        else -> Icons.Filled.AttachMoney
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(statusColor.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = statusColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (transaction.type == "Withdraw") "-৳${String.format("%.2f", transaction.amount)}" else "+৳${String.format("%.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == "Withdraw") Color.Red else Color(0xFF4CAF50),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = transaction.status, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositDialog(onDismiss: () -> Unit, onSubmitted: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad", "Rocket", "Bank Transfer")
    var selectedDepositTarget by remember { mutableStateOf("Recharge") }
    val depositTargets = listOf("Recharge" to "For Mobile Recharge", "General" to "For General Jobs/Tasks")
    
    val depositSuggestions = listOf("100", "200", "500", "1000", "2000", "5000")
    val context = androidx.compose.ui.platform.LocalContext.current

    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                TopAppBar(
                    title = { Text("Deposit Funds") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, enabled = !isSubmitting) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Deposit Purpose", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    depositTargets.forEach { (targetId, description) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (!isSubmitting) selectedDepositTarget = targetId }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedDepositTarget == targetId,
                                onClick = { if (!isSubmitting) selectedDepositTarget = targetId }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(targetId, fontWeight = FontWeight.Bold)
                                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
                
                // bKash & Nagad copyable numbers section
                val numberToShow = when (selectedMethod) {
                    "bKash" -> "01909902319"
                    "Nagad" -> "01623673650"
                    else -> ""
                }
                
                if (numberToShow.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ম্যানুয়াল পেমেন্ট ইনস্ট্রাকশন (${selectedMethod}):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নিচের নাম্বারে টাকা পাঠিয়ে Transaction ID টি সাবমিট করুন।",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedMethod} Number (Personal)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = numberToShow,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Copied Number", numberToShow)
                                        clipboardManager.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "${selectedMethod} নাম্বার কপি করা হয়েছে! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Suggested Amounts for Deposit
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    depositSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { if (!isSubmitting) amount = suggestion },
                            label = { Text("৳$suggestion") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Select Payment Method", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { if (!isSubmitting) selectedMethod = method },
                            label = { Text(method) },
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = !isSubmitting
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    label = { Text("Transaction ID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                if (isSubmitting) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Button(
                        onClick = {
                            val reqAmount = amount.toDoubleOrNull() ?: 0.0
                            if (reqAmount > 0 && transactionId.isNotBlank()) {
                                isSubmitting = true
                                val currentUserUid = UserSession.getUid(context)
                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                val depositDoc = db.collection("deposits").document()
                                val depositId = depositDoc.id
                                val depositData = hashMapOf(
                                    "id" to depositId,
                                    "userId" to currentUserUid,
                                    "amount" to reqAmount,
                                    "method" to selectedMethod,
                                    "paymentMethod" to selectedMethod,
                                    "depositTarget" to selectedDepositTarget,
                                    "transactionId" to transactionId,
                                    "status" to "pending",
                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                db.collection("deposits").document(depositId).set(depositData)
                                    .addOnCompleteListener { task ->
                                        isSubmitting = false
                                        if (task.isSuccessful) {
                                            // Send local push notification (free, background & lockscreen supported)
                                            com.example.NotificationHelper.showNotification(
                                                context = context,
                                                title = "Deposit Request Submitted",
                                                message = "Your deposit request has been submitted. Please wait for admin approval.",
                                                type = com.example.ui.screens.NotificationType.INFO
                                            )
                                            onSubmitted(reqAmount, selectedMethod)
                                            onDismiss()
                                        }
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = amount.isNotBlank() && transactionId.isNotBlank()
                    ) {
                        Text("Submit Deposit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawDialog(availableBalance: Double, onDismiss: () -> Unit, onSubmitted: (Double, String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var amount by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad", "Rocket", "Bank Transfer")
    
    val withdrawSuggestions = listOf("100", "200", "500", "1000", "2000", "5000")
    var minWithdrawLimit by remember { androidx.compose.runtime.mutableDoubleStateOf(100.0) }
    var errorText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Read min withdrawal amount from settings/withdraw_settings in real-time
    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("settings").document("withdraw_settings")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val limit = when (val value = snapshot.get("min_withdraw")) {
                        is Number -> value.toDouble()
                        is String -> value.toDoubleOrNull() ?: 100.0
                        else -> {
                            val v2 = snapshot.get("minimum_withdraw")
                            when (v2) {
                                is Number -> v2.toDouble()
                                is String -> v2.toDoubleOrNull() ?: 100.0
                                else -> snapshot.getDouble("min_withdraw") ?: 100.0
                            }
                        }
                    }
                    minWithdrawLimit = limit
                }
            }
    }

    // Function to validate amount
    fun validateAmount(inputVal: String, minLimit: Double) {
        val reqAmount = inputVal.toDoubleOrNull() ?: 0.0
        errorText = if (reqAmount > availableBalance) {
            "Amount exceeds available balance (ব্যালেন্সের চেয়ে বেশি টাকা তুলতে পারবেন না)"
        } else if (reqAmount < minLimit) {
            "Minimum withdrawal limit is ৳$minLimit (সর্বনিম্ন উইথড্র লিমিট ৳$minLimit)"
        } else {
            ""
        }
    }

    // Automatically recalculate error when minWithdrawLimit changes or amount changes
    LaunchedEffect(amount, minWithdrawLimit) {
        if (amount.isNotEmpty()) {
            validateAmount(amount, minWithdrawLimit)
        } else {
            errorText = ""
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                TopAppBar(
                    title = { Text("Withdraw Funds") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, enabled = !isSubmitting) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Balance", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("৳${String.format("%.2f", availableBalance)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Minimum withdrawal limit (সর্বনিম্ন লিমিট):", style = MaterialTheme.typography.bodyMedium)
                        Text("৳${String.format("%.2f", minWithdrawLimit)}", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        validateAmount(it, minWithdrawLimit)
                    },
                    label = { Text("Amount (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    isError = errorText.isNotEmpty(),
                    supportingText = { if (errorText.isNotEmpty()) Text(errorText) else Text("টাকা উত্তোলনের পরিমাণ লিখুন") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Suggested Amounts for Withdraw
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    withdrawSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { 
                                if (!isSubmitting) {
                                    amount = suggestion
                                    validateAmount(suggestion, minWithdrawLimit)
                                }
                            },
                            label = { Text("৳$suggestion") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Withdraw Method", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { if (!isSubmitting) selectedMethod = method },
                            label = { Text(method) },
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = !isSubmitting
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    label = { Text("Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                if (isSubmitting) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Button(
                        onClick = {
                            val reqAmount = amount.toDoubleOrNull() ?: 0.0
                            if (reqAmount >= minWithdrawLimit && reqAmount <= availableBalance && accountNo.isNotBlank()) {
                                isSubmitting = true
                                val currentUserUid = UserSession.getUid(context)
                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                val withdrawDoc = db.collection("withdrawals").document()
                                val withdrawId = withdrawDoc.id
                                val withdrawData = hashMapOf(
                                    "id" to withdrawId,
                                    "userId" to currentUserUid,
                                    "amount" to reqAmount,
                                    "method" to selectedMethod,
                                    "paymentMethod" to selectedMethod,
                                    "accountNo" to accountNo,
                                    "accountNumber" to accountNo,
                                    "status" to "pending",
                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                db.runTransaction { transaction ->
                                    val userRef = db.collection("users").document(currentUserUid)
                                    val userSnap = transaction.get(userRef)
                                    val currentBalance = when (val bal = userSnap.get("balance")) {
                                        is Number -> bal.toDouble()
                                        is String -> bal.toDoubleOrNull() ?: 0.0
                                        else -> 0.0
                                    }

                                    if (currentBalance >= reqAmount) {
                                        transaction.update(userRef, "balance", currentBalance - reqAmount)
                                        val newWithdrawData = withdrawData.toMutableMap()
                                        newWithdrawData["amount_deducted"] = true
                                        transaction.set(withdrawDoc, newWithdrawData)
                                    } else {
                                        throw Exception("Insufficient balance")
                                    }
                                }.addOnCompleteListener { task ->
                                    isSubmitting = false
                                    if (task.isSuccessful) {
                                        // Send local push notification (free, background & lockscreen supported)
                                        com.example.NotificationHelper.showNotification(
                                            context = context,
                                            title = "Withdrawal Request Submitted",
                                            message = "Your withdrawal request has been submitted successfully. Please wait for admin approval.",
                                            type = com.example.ui.screens.NotificationType.INFO
                                        )
                                        onSubmitted(reqAmount, selectedMethod)
                                        onDismiss()
                                    } else {
                                        errorText = task.exception?.localizedMessage ?: "Transaction failed"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = amount.isNotBlank() && errorText.isEmpty() && accountNo.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) >= minWithdrawLimit
                    ) {
                        Text("Confirm Withdraw", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsDialog(transaction: Transaction, onDismiss: () -> Unit) {
    val statusColor = when (transaction.status) {
        "Success", "Approved" -> Color(0xFF4CAF50)
        "Pending" -> Color(0xFFFF9800)
        "Failed", "Rejected" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Transaction Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailRow("Transaction ID", transaction.id)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Type", transaction.type)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Amount", "৳${String.format("%.2f", transaction.amount)}")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Date", transaction.date)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Method", transaction.method)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(transaction.status, fontWeight = FontWeight.Bold, color = statusColor)
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Notes", transaction.notes)
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
