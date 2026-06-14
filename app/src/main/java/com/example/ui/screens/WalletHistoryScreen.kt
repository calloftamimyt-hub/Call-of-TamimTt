package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppDatabase
import com.example.data.local.WalletHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletHistoryScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val walletHistoryDao = remember { AppDatabase.getDatabase(context).walletHistoryDao() }
    
    val allHistory by walletHistoryDao.getAllHistory().collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Deposit", "Withdraw", "Earning")

    val displayedHistory = when(selectedFilter) {
        "All" -> allHistory
        "Deposit" -> allHistory.filter { it.type == "Deposit" }
        "Withdraw" -> allHistory.filter { it.type == "Withdraw" }
        "Earning" -> allHistory.filter { it.type == "Earning" }
        else -> allHistory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Scroll Row
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            if (displayedHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history found locally.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedHistory) { history ->
                        LocalTransactionItem(history)
                    }
                }
            }
        }
    }
}

@Composable
fun LocalTransactionItem(transaction: WalletHistoryEntity) {
    val statusColor = when (transaction.status) {
        "Success", "Approved" -> Color(0xFF4CAF50)
        "Pending" -> Color(0xFFFF9800)
        "Failed", "Rejected" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val icon = when (transaction.type) {
        "Deposit" -> Icons.Outlined.ArrowDownward
        "Withdraw" -> Icons.Outlined.ArrowUpward
        else -> Icons.Filled.AttachMoney
    }

    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateStr = sdf.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Text(text = transaction.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (transaction.type == "Withdraw") "-" else "+"
                val amountColor = if (transaction.type == "Withdraw") Color.Red else Color(0xFF4CAF50)
                
                Text(
                    text = "$prefix৳${String.format("%.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = transaction.status, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}
