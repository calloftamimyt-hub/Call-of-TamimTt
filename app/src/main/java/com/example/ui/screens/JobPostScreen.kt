package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.TaskAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var workerCount by remember { mutableStateOf("") }
    var payPerWorker by remember { mutableStateOf("") }
    var isTermsAccepted by remember { mutableStateOf(false) }
    
    var showHistory by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var depositedBalance by remember { mutableStateOf(0.0) }
    var isLoadingUser by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    val currentUserUid = UserSession.getUid(context)

    // Work / Available requests states
    var activeTabIndex by remember { mutableStateOf(0) }
    var otherJobsList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isOtherJobsLoading by remember { mutableStateOf(true) }
    var selectedJobForWork by remember { mutableStateOf<Map<String, Any>?>(null) }

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
                        depositedBalance = when (val value = snapshot.get("deposited")) {
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

    LaunchedEffect(activeTabIndex, currentUserUid) {
        if (activeTabIndex == 1 && currentUserUid.isNotBlank()) {
            isOtherJobsLoading = true
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("jobs")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val oId = data["userId"] as? String ?: ""
                            if (oId == currentUserUid) return@mapNotNull null
                            data + ("id" to doc.id)
                        }
                        otherJobsList = list
                    }
                    isOtherJobsLoading = false
                }
        }
    }

    val totalCost = (workerCount.toIntOrNull() ?: 0) * (payPerWorker.toDoubleOrNull() ?: 0.0)

    if (showHistory) {
        JobPostHistoryDialog(onDismiss = { showHistory = false })
    }

    if (selectedJobForWork != null) {
        JobWorkSubmissionDialog(
            job = selectedJobForWork!!,
            onDismiss = { selectedJobForWork = null },
            onSubmitted = { msg ->
                selectedJobForWork = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier(
            statusBarColor = android.graphics.Color.WHITE,
            isLightStatusIcons = true
        )
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    com.example.ui.screens.BeautifulHeader {
                        TopAppBar(
                            title = { Text(if (activeTabIndex == 0) "Post a Job" else "Job Offers / Tasks", color = Color.Black, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                                }
                            },
                            actions = {
                                IconButton(onClick = { showHistory = true }) {
                                    Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabRow(
                        selectedTabIndex = activeTabIndex,
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = activeTabIndex == 0,
                            onClick = { activeTabIndex = 0 },
                            text = { Text("Post a Job", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                        )
                        Tab(
                            selected = activeTabIndex == 1,
                            onClick = { activeTabIndex = 1 },
                            text = { Text("Requests / Work", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                        )
                    }

                    if (activeTabIndex == 0) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Job Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        OutlinedTextField(
                                            value = title,
                                            onValueChange = { title = it },
                                            label = { Text("Job Title") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        OutlinedTextField(
                                            value = description,
                                            onValueChange = { description = it },
                                            label = { Text("Job Description") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 3,
                                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        OutlinedTextField(
                                            value = link,
                                            onValueChange = { link = it },
                                            label = { Text("Job Link") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = workerCount,
                                                onValueChange = { workerCount = it },
                                                label = { Text("Workers Needed") },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                            )
                                            OutlinedTextField(
                                                value = payPerWorker,
                                                onValueChange = { payPerWorker = it },
                                                label = { Text("Pay per Worker (৳)") },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Your Deposit Balance:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                            Text(
                                                text = "৳${String.format("%.2f", depositedBalance)}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (depositedBalance >= totalCost) Color(0xFF4CAF50) else Color(0xFFF44336),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Total Cost:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                            Text(
                                                text = "৳${String.format("%.2f", totalCost)}", 
                                                fontWeight = FontWeight.Bold, 
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isTermsAccepted, onCheckedChange = { isTermsAccepted = it })
                                    Text("I accept the terms and conditions for posting a job.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                if (isSubmitting) {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val reqWorkers = workerCount.toIntOrNull() ?: 0
                                            val reqPay = payPerWorker.toDoubleOrNull() ?: 0.0
                                            val calculatedCost = reqWorkers * reqPay
                                            if (calculatedCost <= 0.0) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("অনুগ্রহ করে সঠিক কর্মী সংখ্যা ও কর্মী প্রতি পেমেন্ট লিখুন।")
                                                }
                                                return@Button
                                            }
                                            if (depositedBalance < calculatedCost) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("আপনার ডিপোজিট ব্যালেন্সে পর্যাপ্ত টাকা নেই। অনুগ্রহ করে প্রথমে ফান্ড ডিপোজিট করুন।")
                                                }
                                                return@Button
                                            }
                                            
                                            isSubmitting = true
                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            val userDocRef = db.collection("users").document(currentUserUid)

                                            db.runTransaction { transaction ->
                                                val userSnapshot = transaction.get(userDocRef)
                                                val currentDeposit = when (val value = userSnapshot.get("deposited")) {
                                                    is Number -> value.toDouble()
                                                    is String -> value.toDoubleOrNull() ?: 0.0
                                                    else -> 0.0
                                                }
                                                if (currentDeposit < calculatedCost) {
                                                    throw Exception("Insufficient deposit balance")
                                                }
                                                
                                                // Deduct cost
                                                val newDeposit = currentDeposit - calculatedCost
                                                transaction.update(userDocRef, "deposited", newDeposit)

                                                // Insert in jobs
                                                val jobDoc = db.collection("jobs").document()
                                                val jobId = jobDoc.id
                                                val jobData = hashMapOf(
                                                    "id" to jobId,
                                                    "userId" to currentUserUid,
                                                    "title" to title,
                                                    "description" to description,
                                                    "link" to link,
                                                    "workerCount" to reqWorkers,
                                                    "payPerWorker" to reqPay,
                                                    "totalCost" to calculatedCost,
                                                    "status" to "Pending",
                                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                                )
                                                transaction.set(jobDoc, jobData)

                                                // Insert in job_posts for admin app layout backup
                                                val jobPostDoc = db.collection("job_posts").document(jobId)
                                                transaction.set(jobPostDoc, jobData)

                                                // Insert notification
                                                val notificationDoc = db.collection("notifications").document()
                                                val notificationData = hashMapOf(
                                                    "id" to notificationDoc.id,
                                                    "userId" to currentUserUid,
                                                    "title" to "Job Post Submitted",
                                                    "message" to "আপনার '$title' জবটি সাকসেসফুলি আপলোড হয়েছে। অনুগ্রহ করে এডমিন অ্যাপ্রভালের জন্য অপেক্ষা করুন।",
                                                    "type" to "INFO",
                                                    "isRead" to false,
                                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                                )
                                                transaction.set(notificationDoc, notificationData)
                                            }.addOnCompleteListener { taskResult ->
                                                isSubmitting = false
                                                if (taskResult.isSuccessful) {
                                                    title = ""
                                                    description = ""
                                                    link = ""
                                                    workerCount = ""
                                                    payPerWorker = ""
                                                    isTermsAccepted = false
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("আপনার জব পোস্ট সফলভাবে সাবমিট হয়েছে এবং পেমেন্ট কেটে নেওয়া হয়েছে।")
                                                    }
                                                } else {
                                                    val errMsg = taskResult.exception?.localizedMessage ?: "ত্রুটি ঘটেছে! অনুগ্রহ করে আবার চেষ্টা করুন।"
                                                    coroutineScope.launch {
                                                        if (errMsg.contains("Insufficient deposit balance")) {
                                                            snackbarHostState.showSnackbar("আপনার ডিপোজিট ব্যালেন্সে পর্যাপ্ত টাকা নেই।")
                                                        } else {
                                                            snackbarHostState.showSnackbar("ত্রুটি: $errMsg")
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        enabled = isTermsAccepted && title.isNotBlank() && description.isNotBlank() && workerCount.isNotBlank() && payPerWorker.isNotBlank()
                                    ) {
                                        Text("Submit Job Post", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    } else {
                        // Requests / Work Tab inside JobPostScreen
                        if (isOtherJobsLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (otherJobsList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No job offers available at the moment.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(otherJobsList) { jobMap ->
                                    val jTitle = jobMap["title"] as? String ?: "Job Title"
                                    val jDesc = jobMap["description"] as? String ?: ""
                                    val pay = when (val value = jobMap["payPerWorker"]) {
                                        is Number -> value.toDouble()
                                        is String -> value.toDoubleOrNull() ?: 0.0
                                        else -> 0.0
                                    }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(jTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black, modifier = Modifier.weight(1f))
                                                Text("৳${String.format("%.2f", pay)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            }
                                            if (jDesc.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(jDesc, maxLines = 2, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { selectedJobForWork = jobMap },
                                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("View details & Submit Proof", style = MaterialTheme.typography.labelMedium)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobWorkSubmissionDialog(job: Map<String, Any>, onDismiss: () -> Unit, onSubmitted: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var message by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val jTitle = job["title"] as? String ?: "Job Offer"
    val jDesc = job["description"] as? String ?: ""
    val jLink = job["link"] as? String ?: ""
    val pay = when (val value = job["payPerWorker"]) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
    val jobId = job["id"] as? String ?: ""
    val jobOwnerId = job["userId"] as? String ?: ""

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Submit Work Proof", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(jTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reward: ৳${String.format("%.2f", pay)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text("Instructions:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(jDesc, style = MaterialTheme.typography.bodyMedium)
                            
                            if (jLink.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri(jLink)
                                        } catch (e: Exception) {}
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open Job Link")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Submission Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Type proof description / completion word / details...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isSubmitting
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !isSubmitting) {
                            launcher.launch("image/*")
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedUri != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = if (selectedUri != null) Icons.Filled.CheckCircle else Icons.Filled.AddPhotoAlternate, 
                                contentDescription = null, 
                                tint = if (selectedUri != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedUri == null) "Upload Screenshot Proof" else "Screenshot Selected!", 
                                color = if (selectedUri != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancel") }
                        Button(
                            onClick = {
                                isSubmitting = true
                                val currentUserUid = UserSession.getUid(context)
                                val userEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "N/A"
                                
                                val caption = """
                                    📝 JOB POST WORK SUBMISSION
                                    💼 Job Title: $jTitle
                                    💰 Pay per Worker: ৳${String.format("%.2f", pay)}
                                    👤 Worker: $userEmail
                                    🆔 Worker ID: $currentUserUid
                                    💬 Message: $message
                                """.trimIndent()

                                if (selectedUri != null) {
                                    TelegramUploadHelper.uploadScreenshot(
                                        context = context,
                                        imageUri = selectedUri!!,
                                        caption = caption,
                                        callback = object : TelegramUploadHelper.UploadCallback {
                                            override fun onSuccess(fileUrl: String, telegramMessageId: Long) {
                                                val data = hashMapOf(
                                                    "userId" to currentUserUid,
                                                    "userEmail" to userEmail,
                                                    "jobId" to jobId,
                                                    "jobTitle" to jTitle,
                                                    "reward" to pay,
                                                    "message" to message,
                                                    "screenshotUrl" to fileUrl,
                                                    "telegramMessageId" to telegramMessageId,
                                                    "postOwnerId" to jobOwnerId,
                                                    "status" to "Pending",
                                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                                )
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    .collection("job_post_submissions")
                                                    .add(data)
                                                    .addOnCompleteListener { task ->
                                                        isSubmitting = false
                                                        if (task.isSuccessful) {
                                                            onSubmitted("Proof successfully sent to Telegram!")
                                                        } else {
                                                            onSubmitted("Saved in Firestore failed: ${task.exception?.localizedMessage}")
                                                        }
                                                    }
                                            }

                                            override fun onFailure(errorMessage: String) {
                                                isSubmitting = false
                                                onSubmitted("Telegram upload failed: $errorMessage")
                                            }
                                        }
                                    )
                                } else {
                                    val data = hashMapOf(
                                        "userId" to currentUserUid,
                                        "userEmail" to userEmail,
                                        "jobId" to jobId,
                                        "jobTitle" to jTitle,
                                        "reward" to pay,
                                        "message" to message,
                                        "screenshotUrl" to "",
                                        "postOwnerId" to jobOwnerId,
                                        "status" to "Pending",
                                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                    )
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("job_post_submissions")
                                        .add(data)
                                        .addOnCompleteListener { task ->
                                            isSubmitting = false
                                            if (task.isSuccessful) {
                                                onSubmitted("Proof submitted successfully!")
                                            } else {
                                                onSubmitted("Failed: ${task.exception?.localizedMessage}")
                                            }
                                        }
                                }
                            },
                            enabled = !isSubmitting && (message.isNotBlank() || selectedUri != null),
                            modifier = Modifier.fillMaxWidth().height(50.dp).weight(1f).padding(start = 16.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Submit Proof")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostHistoryDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserUid = UserSession.getUid(context)

    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("jobs")
                .whereEqualTo("userId", currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val map = doc.data ?: return@mapNotNull null
                            map + ("id" to doc.id)
                        }.sortedWith { a, b ->
                            val tsA = a["timestamp"] as? com.google.firebase.Timestamp
                            val tsB = b["timestamp"] as? com.google.firebase.Timestamp
                            val timeA = tsA?.toDate()?.time ?: 0L
                            val timeB = tsB?.toDate()?.time ?: 0L
                            timeB.compareTo(timeA)
                        }
                        historyList = list
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Job Posts History", color = Color.Black) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { paddingValues ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("No history yet.", color = androidx.compose.ui.graphics.Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyList) { job ->
                            val jTitle = job["title"] as? String ?: "Job Offer"
                            val jDesc = job["description"] as? String ?: ""
                            val pay = when (val value = job["payPerWorker"]) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val workers = when (val value = job["workerCount"]) {
                                is Number -> value.toInt()
                                is String -> value.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val calculatedCost = when (val value = job["totalCost"]) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val status = job["status"] as? String ?: "Pending"

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(jTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Workers: $workers | Pay: ৳${String.format("%.2f", pay)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("৳${String.format("%.2f", calculatedCost)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val statusColor = when (status) {
                                                "Approved", "Success" -> Color(0xFF4CAF50)
                                                "Pending" -> Color(0xFFFF9800)
                                                "Rejected", "Failed" -> Color(0xFFF44336)
                                                else -> Color.Gray
                                            }
                                            Text(status, color = statusColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    if (jDesc.isNotBlank()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        Text(jDesc, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 3)
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
