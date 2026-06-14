package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.ui.screens.UserSession

data class MobileOperator(
    val name: String,
    val logoUrl: String
)

val contactColors = listOf(Color(0xFF8C9EFF), Color(0xFFCE93D8), Color(0xFFFFAB91), Color(0xFFA5D6A7), Color(0xFFFFCC80), Color(0xFF80CBC4), Color(0xFF90CAF9))

data class DeviceContact(val id: String, val name: String, val number: String, val color: Color)

@SuppressLint("Range")
fun fetchContacts(context: Context): List<DeviceContact> {
    val contacts = mutableListOf<DeviceContact>()
    try {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        var colorIdx = 0
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)) ?: ""
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: ""
                var number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                number = number.replace(" ", "").replace("-", "")
                val color = contactColors[colorIdx % contactColors.size]
                colorIdx++
                contacts.add(DeviceContact(id, name, number, color))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return contacts.distinctBy { it.number }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRechargeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
    }

    var allContacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var recentContacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    
    LaunchedEffect(hasContactPermission) {
        if (!hasContactPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            val fetched = fetchContacts(context)
            allContacts = fetched
            recentContacts = fetched.take(3) // Mock recent
        }
    }

    var userBalance by remember { mutableStateOf(0.0) }
    var userRechargeBalance by remember { mutableStateOf(0.0) }
    
    val currentUserUid = UserSession.getUid(context)
    DisposableEffect(currentUserUid) {
        if (currentUserUid.isBlank()) return@DisposableEffect onDispose {}
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserUid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    userBalance = when (val bal = snapshot.get("balance")) {
                        is Number -> bal.toDouble()
                        is String -> bal.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    userRechargeBalance = when (val bal = snapshot.get("rechargeBalance")) {
                        is Number -> bal.toDouble()
                        is String -> bal.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                }
            }
        onDispose { listener.remove() }
    }

    var step by remember { mutableStateOf("CONTACTS") } // "CONTACTS", "AMOUNT"
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedPhoneNumber by remember { mutableStateOf("") }
    var selectedContactName by remember { mutableStateOf("") }
    
    var showOperatorSheet by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<MobileOperator?>(null) }
    
    var amount by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val operators = listOf(
        MobileOperator("Airtel", "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781110287/v3jfzijm2wfbgfnbhjbx.jpg"),
        MobileOperator("Banglalink", "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781110286/xp7xwcwhixbx25pwxa6s.png"),
        MobileOperator("Grameenphone", "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781110286/wdbnxmmonu79vmbctpju.png"),
        MobileOperator("Robi", "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781110367/tqcnrv7i4dlmrnkrzxpf.jpg"),
        MobileOperator("Teletalk", "https://res.cloudinary.com/dhlzcea1t/image/upload/v1781110287/bh4th0ritriwk4avapjt.png"),
        MobileOperator("Skitto", "android.resource://com.example/drawable/skitto_logo_1781147687535")
    )
    val quickAmounts = listOf("20", "50", "100", "200", "500", "1000")

    val bgPink = Color(0xFFD81B60)

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                        topBar = {
                            TopAppBar(
                                title = { Text("মোবাইল রিচার্জ", color = Color.Black, fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        if (step == "AMOUNT") {
                                            step = "CONTACTS"
                                        } else {
                                            onBack()
                                        }
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showHistory = true }) {
                                        Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                            )
                        }
            ) { paddingValues ->
                if (step == "CONTACTS") {
                    ContactsSelectionView(
                        paddingValues = paddingValues,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        allContacts = allContacts,
                        recentContacts = recentContacts,
                        hasContactPermission = hasContactPermission,
                        onContactClick = { name, number ->
                            selectedContactName = name
                            selectedPhoneNumber = number
                            showOperatorSheet = true
                        },
                        onProceedUnknownNumber = {
                            selectedContactName = "Unknown"
                            selectedPhoneNumber = searchQuery
                            showOperatorSheet = true
                        }
                    )
                } else if (step == "AMOUNT") {
                    AmountEntryView(
                        paddingValues = paddingValues,
                        phoneNumber = selectedPhoneNumber,
                        contactName = selectedContactName,
                        operator = selectedProvider,
                        amount = amount,
                        userBalance = userBalance,
                        userRechargeBalance = userRechargeBalance,
                        onAmountChange = { amount = it },
                        quickAmounts = quickAmounts,
                        onChangeOperatorClick = { showOperatorSheet = true },
                        onSubmit = { submittedAmount ->
                            // Submit logic will be injected here
                        },
                        onSuccess = {
                            step = "CONTACTS"
                            searchQuery = ""
                        }
                    )
                }
            }
        }
        
        if (showOperatorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showOperatorSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = null
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("অপারেটর বেছে নিন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("এই নাম্বারের বর্তমান অপারেটর বেছে নিন", fontSize = 14.sp, color = Color.Gray)
                        }
                        TextButton(onClick = { showOperatorSheet = false }) {
                            Text("বাতিল করুন", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(operators) { operator ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        selectedProvider = operator
                                        showOperatorSheet = false
                                        step = "AMOUNT"
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFEEEEEE))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = operator.logoUrl,
                                        contentDescription = operator.name,
                                        modifier = Modifier.size(60.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // History Logic Component keeps it concise
        if (showHistory) {
            RechargeHistoryDialog(onDismiss = { showHistory = false }, context = context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSelectionView(
    paddingValues: PaddingValues,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    allContacts: List<DeviceContact>,
    recentContacts: List<DeviceContact>,
    hasContactPermission: Boolean,
    onContactClick: (String, String) -> Unit,
    onProceedUnknownNumber: () -> Unit
) {
    val filteredContacts = if (searchQuery.isBlank()) emptyList() 
        else allContacts.filter { it.name.contains(searchQuery, true) || it.number.contains(searchQuery) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFFAFAFA))
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("নাম বা নাম্বার দিন", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (searchQuery.isEmpty()) {

            item {
                Text(
                    text = "আমার নাম্বার",
                    modifier = Modifier.padding(16.dp, 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                ContactItem(name = "My Number", number = "01xxxxxxxxx", color = Color(0xFF81C784), onClick = { onContactClick("My Number", "01xxxxxxxxx") })
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (recentContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "সাম্প্রতিক",
                        modifier = Modifier.padding(16.dp, 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                items(recentContacts) { contact ->
                    ContactItem(name = contact.name, number = contact.number, color = contact.color, onClick = { onContactClick(contact.name, contact.number) })
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            if (allContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "সব কন্টাক্ট",
                        modifier = Modifier.padding(16.dp, 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                items(allContacts) { contact ->
                    ContactItem(name = contact.name, number = contact.number, color = contact.color, onClick = { onContactClick(contact.name, contact.number) })
                }
            } else if (!hasContactPermission) {
                item {
                    Text(
                        "Please grant contact permission to view your contacts.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "সার্চ করা রেজাল্টসমূহ",
                    modifier = Modifier.padding(16.dp, 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
            if (filteredContacts.isNotEmpty()) {
                items(filteredContacts) { contact ->
                    ContactItem(name = contact.name, number = contact.number, color = contact.color, onClick = { onContactClick(contact.name, contact.number) })
                }
            } else if (searchQuery.matches(Regex("^[0-9+]+$"))) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("কোন কন্টাক্ট নেই", color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onProceedUnknownNumber,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
                        ) {
                            Text("পরের ধাপে যেতে ক্লিক করুন")
                        }
                    }
                }
            } else {
                item {
                    Text("কোন কন্টাক্ট নেই", modifier = Modifier.padding(16.dp), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ContactItem(name: String, number: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "#",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(text = number, fontSize = 14.sp, color = Color.Gray)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEntryView(
    paddingValues: PaddingValues,
    phoneNumber: String,
    contactName: String,
    operator: MobileOperator?,
    amount: String,
    userBalance: Double,
    userRechargeBalance: Double,
    onAmountChange: (String) -> Unit,
    quickAmounts: List<String>,
    onChangeOperatorClick: () -> Unit,
    onSubmit: (Double) -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { pValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onChangeOperatorClick),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (operator != null) {
                            AsyncImage(
                                model = operator.logoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (contactName == "Unknown" || contactName.isEmpty()) phoneNumber else contactName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(phoneNumber, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Text("পরিবর্তন", color = Color(0xFFD81B60), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Available Recharge Balance: ৳${String.format("%.2f", userRechargeBalance)}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Amount ৳", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD81B60),
                    unfocusedBorderColor = Color.Gray,
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(quickAmounts) { amt ->
                    Card(
                        modifier = Modifier.clickable { onAmountChange(amt) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "৳$amt",
                            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val amtValue = amount.toDoubleOrNull() ?: 0.0
                    if (amtValue > 0 && operator != null) {
                        if (amtValue > userRechargeBalance) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Insufficient Recharge Balance!")
                            }
                            return@Button
                        }
                        isSubmitting = true
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val currentUserUid = UserSession.getUid(context)
                        
                        coroutineScope.launch {
                            val result = com.example.RechargeApiHelper.performRecharge(
                                phone = phoneNumber,
                                amount = amtValue,
                                operator = operator.name
                            )
                            
                            val status = if (result.isSuccess) "success" else "failed"
                            val rechargeData = hashMapOf(
                                "userId" to currentUserUid,
                                "amount" to amtValue,
                                "phoneNumber" to phoneNumber,
                                "operator" to operator.name,
                                "status" to status,
                                "transactionId" to (result.transactionId ?: ""),
                                "apiMessage" to result.message,
                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            
                            db.collection("recharge_requests")
                                .add(rechargeData)
                                .addOnCompleteListener { task ->
                                    isSubmitting = false
                                    if (task.isSuccessful) {
                                        if (result.isSuccess) {
                                            val commission = amtValue * 0.01
                                            val newRechargeBal = userRechargeBalance - amtValue
                                            val newEarningBal = userBalance + commission
                                            
                                            // Atomically update user balance document or just update it
                                            db.collection("users").document(currentUserUid)
                                                .update(
                                                    mapOf(
                                                        "rechargeBalance" to newRechargeBal,
                                                        "balance" to newEarningBal,
                                                        "earnings" to com.google.firebase.firestore.FieldValue.increment(commission)
                                                    )
                                                )
                                        }

                                        coroutineScope.launch {
                                            onAmountChange("")
                                            if (result.isSuccess) {
                                                snackbarHostState.showSnackbar("রিচার্জ সফল হয়েছে! ট্রানজেকশন আইডি: ${result.transactionId ?: ""}")
                                            } else {
                                                snackbarHostState.showSnackbar("রিচার্জ ব্যর্থ হয়েছে: ${result.message}")
                                            }
                                            kotlinx.coroutines.delay(1000)
                                            onSuccess()
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Error saving history: ${task.exception?.localizedMessage}")
                                        }
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
                enabled = !isSubmitting && amount.isNotBlank() && operator != null && (amount.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Recharge Now", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeHistoryDialog(onDismiss: () -> Unit, context: Context) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
            var isLoadingHistory by remember { mutableStateOf(true) }
            var selectedIds by remember { mutableStateOf(setOf<String>()) }
            val currentUserUid = UserSession.getUid(context)

            DisposableEffect(currentUserUid) {
                if (currentUserUid.isNotBlank()) {
                    val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("recharge_requests")
                        .whereEqualTo("userId", currentUserUid)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                historyList = snapshot.documents.map { doc ->
                                    val data = doc.data ?: emptyMap<String, Any>()
                                    data + ("id" to doc.id)
                                }.sortedByDescending { (it["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L }
                            }
                            isLoadingHistory = false
                        }
                    onDispose { listener.remove() }
                } else {
                    isLoadingHistory = false
                    onDispose {}
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { 
                        if (selectedIds.isNotEmpty()) {
                            Text("${selectedIds.size} Selected", color = Color.White)
                        } else {
                            Text("Recharge History", color = Color.White)
                        }
                    },
                    navigationIcon = {
                        if (selectedIds.isNotEmpty()) {
                            IconButton(onClick = { selectedIds = emptySet() }) { Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color.White) }
                        } else {
                            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                        }
                    },
                    actions = {
                        if (selectedIds.isNotEmpty()) {
                            IconButton(onClick = {
                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                val batch = db.batch()
                                selectedIds.forEach { id ->
                                    batch.delete(db.collection("recharge_requests").document(id))
                                }
                                batch.commit()
                                selectedIds = emptySet()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        } else if (historyList.isNotEmpty()) {
                            TextButton(onClick = { selectedIds = historyList.mapNotNull { it["id"] as? String }.toSet() }) {
                                Text("Select All", color = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD81B60))
                )
                
                if (isLoadingHistory) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD81B60))
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No recharge history found yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(historyList) { item ->
                            val operator = item["operator"] as? String ?: ""
                            val phone = item["phoneNumber"] as? String ?: ""
                            val amt = item["amount"]?.toString() ?: "0"
                            val status = item["status"] as? String ?: "pending"
                            val ts = item["createdAt"] as? com.google.firebase.Timestamp
                            val dateStr = ts?.toDate()?.let { 
                                java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(it)
                            } ?: ""

                            val itemId = item["id"] as? String ?: ""
                            val isSelected = selectedIds.contains(itemId)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (selectedIds.isNotEmpty()) {
                                            selectedIds = if (isSelected) selectedIds - itemId else selectedIds + itemId
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFFCDD2) else Color(0xFFF5F5F5)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedIds.isNotEmpty()) {
                                            androidx.compose.material3.Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    selectedIds = if (checked) selectedIds + itemId else selectedIds - itemId
                                                },
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                        }
                                        Column {
                                            Text(operator, fontWeight = FontWeight.Bold)
                                            Text(phone, style = MaterialTheme.typography.bodySmall)
                                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("৳$amt", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                                            val statusColor = when (status.lowercase()) {
                                                "pending" -> Color(0xFFFF9800)
                                                "approved", "success" -> Color(0xFF4CAF50)
                                                else -> Color.Red
                                            }
                                            Text(
                                                status.replaceFirstChar { it.uppercase() },
                                                color = statusColor,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (selectedIds.isEmpty()) {
                                            IconButton(onClick = {
                                                if (itemId.isNotBlank()) {
                                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                        .collection("recharge_requests")
                                                        .document(itemId)
                                                        .delete()
                                                }
                                            }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Gray)
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
    }
}
