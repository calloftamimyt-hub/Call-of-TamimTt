package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositDialog(onDismiss: () -> Unit, onSubmitted: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad", "Rocket", "Bank Transfer")
    var selectedDepositTarget by remember { mutableStateOf("General") }
    val depositTargets = listOf("General" to "For General Jobs/Tasks")
    
    val depositSuggestions = listOf("100", "200", "500", "1000", "2000", "5000")
    val context = androidx.compose.ui.platform.LocalContext.current

    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Suggested Amounts for Deposit
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
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
                                            com.example.NotificationHelper.showNotification(
                                                context = context,
                                                title = "Deposit Request Submitted",
                                                message = "Your deposit request has been submitted. Please wait for admin approval.",
                                                type = NotificationType.INFO
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
    var minWithdrawLimit by remember { mutableStateOf(100.0) }
    var errorText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("settings").document("withdraw_settings")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val limit = when (val value = snapshot.get("min_withdraw")) {
                        is Number -> value.toDouble()
                        is String -> value.toDoubleOrNull() ?: 100.0
                        else -> snapshot.getDouble("min_withdraw") ?: 100.0
                    }
                    minWithdrawLimit = limit
                }
            }
    }

    fun validateAmount(inputVal: String, minLimit: Double) {
        val reqAmount = inputVal.toDoubleOrNull() ?: 0.0
        errorText = if (reqAmount > availableBalance) {
            "Amount exceeds available balance"
        } else if (reqAmount < minLimit) {
            "Minimum withdrawal limit is ৳$minLimit"
        } else {
            ""
        }
    }

    LaunchedEffect(amount, minWithdrawLimit) {
        if (amount.isNotEmpty()) {
            validateAmount(amount, minWithdrawLimit)
        } else {
            errorText = ""
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
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
                        Text("Minimum withdrawal limit:", style = MaterialTheme.typography.bodyMedium)
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
                    supportingText = { if (errorText.isNotEmpty()) Text(errorText) else Text("Enter withdraw amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                                        com.example.NotificationHelper.showNotification(
                                            context = context,
                                            title = "Withdrawal Request Submitted",
                                            message = "Your withdrawal request has been submitted successfully.",
                                            type = NotificationType.INFO
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
