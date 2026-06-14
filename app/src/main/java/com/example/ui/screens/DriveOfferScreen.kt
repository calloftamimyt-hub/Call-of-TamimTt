package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

data class DriveOffer(
    val id: String = "",
    val operator: String = "", // GP, Robi, Airtel, Banglalink
    val title: String = "",
    val description: String = "",
    val regularPrice: Double = 0.0,
    val offerPrice: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveOfferScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showHistory by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<DriveOffer?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var offers by remember { mutableStateOf<List<DriveOffer>>(emptyList()) }
    var isLoadingOffers by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listenerRegistration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("drive_offers")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val operator = doc.getString("operator") ?: ""
                            val title = doc.getString("title") ?: ""
                            val description = doc.getString("description") ?: ""
                            
                            val regularPrice = when (val value = doc.get("regularPrice")) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> doc.getDouble("regularPrice") ?: 0.0
                            }
                            
                            val offerPrice = when (val value = doc.get("offerPrice")) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> doc.getDouble("offerPrice") ?: 0.0
                            }
                            
                            DriveOffer(id, operator, title, description, regularPrice, offerPrice)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    offers = list
                }
                isLoadingOffers = false
            }

        onDispose {
            listenerRegistration.remove()
        }
    }
    
    if (showHistory) {
        DriveHistoryDialog(onDismiss = { showHistory = false })
    }

    if (selectedOffer != null) {
        OrderDriveOfferDialog(
            offer = selectedOffer!!, 
            onDismiss = { selectedOffer = null },
            onSubmit = { phone ->
                coroutineScope.launch {
                    val offerToOrder = selectedOffer!!
                    selectedOffer = null
                    
                    val currentUserUid = UserSession.getUid(context)
                    val orderData = hashMapOf(
                        "userId" to currentUserUid,
                        "offerId" to offerToOrder.id,
                        "operator" to offerToOrder.operator,
                        "title" to offerToOrder.title,
                        "offerPrice" to offerToOrder.offerPrice,
                        "phone" to phone,
                        "status" to "Pending",
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("drive_orders")
                        .add(orderData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Order placed for $phone! Pending admin approval.")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to place order: ${task.exception?.localizedMessage}")
                                }
                            }
                        }
                }
            }
        )
    }

    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Drive Offers", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) }
                        },
                        actions = {
                            IconButton(onClick = { showHistory = true }) { Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                    item {
                        Text("Available Offers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (isLoadingOffers) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (offers.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No drive offers available.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(offers) { offer ->
                            DriveOfferCard(offer = offer, onClick = { selectedOffer = offer })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriveOfferCard(offer: DriveOffer, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.operator, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text(offer.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(offer.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("৳${offer.offerPrice}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                Text("৳${offer.regularPrice}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textDecoration = TextDecoration.LineThrough)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDriveOfferDialog(offer: DriveOffer, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Order Drive Offer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(offer.title, fontWeight = FontWeight.Bold)
                Text("Operator: ${offer.operator}", style = MaterialTheme.typography.bodyMedium)
                Text("Price: ৳${offer.offerPrice}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Enter Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(phoneNumber) },
                        enabled = phoneNumber.isNotBlank() && phoneNumber.length >= 10
                    ) {
                        Text("Confirm Order")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveHistoryDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserUid = UserSession.getUid(context)

    DisposableEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            val listenerRegistration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("drive_orders")
                .whereEqualTo("userId", currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val list = snapshot.documents.map { doc ->
                            val map = doc.data ?: emptyMap<String, Any>()
                            map + ("id" to doc.id)
                        }.sortedByDescending { (it["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L }
                        
                        historyList = list
                    }
                    isLoading = false
                }
            
            onDispose {
                listenerRegistration.remove()
            }
        } else {
            isLoading = false
            onDispose {}
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Offer History", color = Color.Black) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { padding ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No history yet.", color = androidx.compose.ui.graphics.Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                        items(historyList) { order ->
                            val title = order["title"] as? String ?: "Offer"
                            val operator = order["operator"] as? String ?: ""
                            val price = order["offerPrice"] as? Any
                            val phone = order["phone"] as? String ?: ""
                            val status = order["status"] as? String ?: "Pending"
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(operator, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Target: $phone", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("৳$price", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val statusColor = when (status.lowercase()) {
                                            "success", "approved", "completed" -> Color(0xFF4CAF50)
                                            "failed", "rejected" -> MaterialTheme.colorScheme.error
                                            else -> Color(0xFFFF9800)
                                        }
                                        Text(status, color = statusColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
