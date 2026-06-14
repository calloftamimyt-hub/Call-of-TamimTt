package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositDialog(onDismiss: () -> Unit, onSubmitted: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad")
    var selectedDepositTarget by remember { mutableStateOf("General") }
    val depositTargets = listOf("General" to "For General Jobs/Tasks")
    
    val depositSuggestions = listOf("100", "200", "500", "1000", "2000", "3000", "5000", "10000")
    val context = androidx.compose.ui.platform.LocalContext.current

    var isSubmitting by remember { mutableStateOf(false) }

    val darkBlue = Color(0xFF0D47A1)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                // Customized Header (Premium layout with dark blue text and back button)
                TopAppBar(
                    title = { 
                        Text(
                            text = "Deposit Funds", 
                            color = darkBlue, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = darkBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Select Payment Method", 
                        fontWeight = FontWeight.Bold, 
                        color = darkBlue,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // 1. Payment method cards (bKash & Nagad) - tight layout
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("bKash", "Nagad").forEach { method ->
                            val isSelected = selectedMethod == method
                            val logoUrl = if (method == "bKash") {
                                "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781435647/zjsxlhs1456rhzoxwgpm.png"
                            } else {
                                "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781435648/vaqawkmxc67mcbinxnzb.jpg"
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { if (!isSubmitting) selectedMethod = method },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) darkBlue else Color(0xFFE0E0E0)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .background(if (isSelected) Color(0x0C0D47A1) else Color.Transparent)
                                        .padding(4.dp)
                                ) {
                                    AsyncImage(
                                        model = logoUrl,
                                        contentDescription = "$method Logo",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.Center)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                    
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .background(darkBlue, shape = androidx.compose.foundation.shape.CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Manual Payment instruction Card
                    val numberToShow = when (selectedMethod) {
                        "bKash" -> "01909902319"
                        "Nagad" -> "01623673650"
                        else -> ""
                    }

                    if (numberToShow.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, darkBlue.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "ম্যানুয়াল পেমেন্ট নির্দেশাবলী (${selectedMethod}):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = darkBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "নিচের নাম্বারে টাকা পাঠিয়ে Transaction ID টি সাবমিট করুন।",
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${selectedMethod} Number (Personal)",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = numberToShow,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = darkBlue,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Copied Number", numberToShow)
                                            clipboardManager.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "${selectedMethod} নাম্বার কপি করা হয়েছে! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                                    ) {
                                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Copy", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Enter transaction ID
                    Text(
                        text = "Transaction ID",
                        fontWeight = FontWeight.Bold,
                        color = darkBlue,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        placeholder = { Text("যেমন: 8N7X9K2L", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 0.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = darkBlue,
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedLabelColor = darkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Enter Amount
                    Text(
                        text = "Amount (৳)",
                        fontWeight = FontWeight.Bold,
                        color = darkBlue,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("কত টাকা ডিপোজিট করতে চান", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 0.dp),
                        enabled = !isSubmitting,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = darkBlue,
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedLabelColor = darkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 5. Dynamic amount chips with 4 items per line, aligned perfectly
                    Text(
                        text = "Suggested Amount",
                        fontWeight = FontWeight.Bold,
                        color = darkBlue,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    val chunkedSuggestions = depositSuggestions.chunked(4)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        chunkedSuggestions.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowItems.forEach { suggestion ->
                                    val isSelected = amount == suggestion
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSelected) darkBlue else Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = darkBlue,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                if (!isSubmitting) {
                                                    amount = suggestion
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "৳$suggestion",
                                            color = if (isSelected) Color.White else darkBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                if (rowItems.size < 4) {
                                    repeat(4 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Submit Button (with requested slightly rounded corners, custom thin height)
                    if (isSubmitting) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = darkBlue)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = darkBlue,
                                disabledContainerColor = darkBlue.copy(alpha = 0.5f)
                            ),
                            enabled = amount.isNotBlank() && transactionId.isNotBlank()
                        ) {
                            Text("Submit Deposit Request", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(availableBalance: Double, onDismiss: () -> Unit, onSubmitted: (Double, String) -> Unit, onViewHistory: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var amount by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("bKash") }
    val methods = listOf("bKash", "Nagad")
    
    val withdrawSuggestions = listOf("100", "200", "500", "1000", "2000", "3000", "5000", "10000")
    var minWithdrawLimit by remember { mutableStateOf(100.0) }
    var errorText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val darkBlue = Color(0xFF0D47A1)

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

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            // Customized Header (Premium layout with dark blue text and cross button)
            TopAppBar(
                title = { 
                    Text(
                        text = "Withdraw Funds", 
                        color = darkBlue, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = darkBlue)
                    }
                },
                actions = {
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Filled.History, contentDescription = "History", tint = darkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Select Withdrawal Method", 
                    fontWeight = FontWeight.Bold, 
                    color = darkBlue,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 1. Payment method cards at the very top (bKash & Nagad)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf("bKash", "Nagad").forEach { method ->
                        val isSelected = selectedMethod == method
                        val logoUrl = if (method == "bKash") {
                            "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781435647/zjsxlhs1456rhzoxwgpm.png"
                        } else {
                            "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781435648/vaqawkmxc67mcbinxnzb.jpg"
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { if (!isSubmitting) selectedMethod = method },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) darkBlue else Color(0xFFE0E0E0)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .background(if (isSelected) Color(0x0C0D47A1) else Color.Transparent)
                                    .padding(6.dp)
                            ) {
                                AsyncImage(
                                    model = logoUrl,
                                    contentDescription = "$method Logo",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                                
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .background(darkBlue, shape = androidx.compose.foundation.shape.CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Available Balance info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, darkBlue.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Available Balance", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("৳${String.format("%.2f", availableBalance)}", fontWeight = FontWeight.Bold, color = darkBlue, fontSize = 15.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, darkBlue.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Min Withdrawal", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("৳${String.format("%.2f", minWithdrawLimit)}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
                        }
                    }
                }

                // Account Number input
                Text(
                    text = "Account Number (${selectedMethod})",
                    fontWeight = FontWeight.Bold,
                    color = darkBlue,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it },
                    placeholder = { Text("আপনার ${selectedMethod} পার্সোনাল নাম্বার দিন", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    enabled = !isSubmitting,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = darkBlue,
                        unfocusedBorderColor = Color(0xFFB0BEC5),
                        focusedLabelColor = darkBlue,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                // Amount input
                Text(
                    text = "Amount (৳)",
                    fontWeight = FontWeight.Bold,
                    color = darkBlue,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        validateAmount(it, minWithdrawLimit)
                    },
                    placeholder = { Text("কত টাকা উত্তোলন করতে চান", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    enabled = !isSubmitting,
                    singleLine = true,
                    isError = errorText.isNotEmpty(),
                    supportingText = { if (errorText.isNotEmpty()) Text(errorText) else null },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = darkBlue,
                        unfocusedBorderColor = Color(0xFFB0BEC5),
                        errorBorderColor = Color.Red,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                // Suggested Amount Chips custom layout (Grid, 4 items per line, aligned perfectly)
                Text(
                    text = "Suggested Amount",
                    fontWeight = FontWeight.Bold,
                    color = darkBlue,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val chunkedSuggestions = withdrawSuggestions.chunked(4)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chunkedSuggestions.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { suggestion ->
                                val isSelected = amount == suggestion
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) darkBlue else Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = darkBlue,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (!isSubmitting) {
                                                amount = suggestion
                                                validateAmount(suggestion, minWithdrawLimit)
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "৳$suggestion",
                                        color = if (isSelected) Color.White else darkBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (rowItems.size < 4) {
                                repeat(4 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit button (with requested thinner height)
                if (isSubmitting) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = darkBlue)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkBlue,
                            disabledContainerColor = darkBlue.copy(alpha = 0.5f)
                        ),
                        enabled = amount.isNotBlank() && errorText.isEmpty() && accountNo.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) >= minWithdrawLimit
                    ) {
                        Text("Confirm Withdraw", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
