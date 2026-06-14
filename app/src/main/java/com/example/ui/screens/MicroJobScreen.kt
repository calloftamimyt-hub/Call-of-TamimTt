package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

data class MicroJob(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val reward: Double = 0.0,
    val link: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroJobScreen(onBack: () -> Unit) {
    var showHistory by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<MicroJob?>(null) }
    var availableJobs by remember { mutableStateOf<List<MicroJob>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val listenerRegistration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("micro_jobs")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = doc.getString("title") ?: ""
                            val description = doc.getString("description") ?: ""
                            val link = doc.getString("link") ?: ""
                            val reward = when (val value = doc.get("reward")) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            MicroJob(id, title, description, reward, link)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    availableJobs = list
                }
                isLoading = false
            }

        onDispose {
            listenerRegistration.remove()
        }
    }
    
    if (showHistory) {
        MicroJobHistoryDialog(onDismiss = { showHistory = false })
    }

    if (selectedJob != null) {
        MicroJobDetailsDialog(
            job = selectedJob!!, 
            onDismiss = { selectedJob = null },
            onTaskSubmitted = { msg ->
                selectedJob = null
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
                            title = { Text("Micro Jobs", color = Color.Black, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) }
                            },
                            actions = {
                                IconButton(onClick = { showHistory = true }) { Icon(Icons.Filled.History, contentDescription = "History", tint = Color.Black) }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                    Text("Available Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (availableJobs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("No micro jobs available.", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(availableJobs) { job ->
                                MicroJobCard(job = job, onClick = { selectedJob = job })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MicroJobCard(job: MicroJob, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                job.title, 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.titleSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("৳${String.format("%.2f", job.reward)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(36.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp)) {
                Text("View Details", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroJobDetailsDialog(job: MicroJob, onDismiss: () -> Unit, onTaskSubmitted: (String) -> Unit) {
    var showSubmitProof by remember { mutableStateOf(false) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    
    if (showSubmitProof) {
        SubmitProofDialog(
            job = job,
            onDismiss = { showSubmitProof = false },
            onSubmit = { msg ->
                showSubmitProof = false
                onTaskSubmitted(msg)
            }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Job Details", color = Color.Black) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(job.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reward: ৳${String.format("%.2f", job.reward)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text("Description", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(job.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    if (job.link.isNotBlank()) {
                                        uriHandler.openUri(job.link)
                                    }
                                } catch (e: Exception) {
                                    // handle gracefully
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Open Link")
                        }
                        Button(
                            onClick = { showSubmitProof = true },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Submit Proof")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitProofDialog(job: MicroJob, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var message by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Submit Proof", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Write a message...") },
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
                            text = if (selectedUri == null) "Add Screenshot Proof" else "Screenshot Selected!", 
                            color = if (selectedUri != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    if (isSubmitting) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                         Spacer(modifier = Modifier.width(16.dp))
                    }
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isSubmitting = true
                            val currentUserUid = UserSession.getUid(context)
                            val userEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "N/A"
                            
                            val caption = """
                                📝 MICRO JOB PROOF SUBMISSION
                                💼 Job Title: ${job.title}
                                💰 Reward: ৳${String.format("%.2f", job.reward)}
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
                                                "jobId" to job.id,
                                                "jobTitle" to job.title,
                                                "reward" to job.reward,
                                                "message" to message,
                                                "screenshotUrl" to fileUrl,
                                                "telegramMessageId" to telegramMessageId,
                                                "status" to "Pending",
                                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )
                                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("micro_job_submissions")
                                                .add(data)
                                                .addOnCompleteListener { task ->
                                                    isSubmitting = false
                                                    if (task.isSuccessful) {
                                                        onSubmit("Proof submitted successfully with screenshot!")
                                                    } else {
                                                        onSubmit("Failed to submit proof: ${task.exception?.localizedMessage}")
                                                    }
                                                }
                                        }
                                        
                                        override fun onFailure(errorMessage: String) {
                                            isSubmitting = false
                                            onSubmit("Telegram upload failed: $errorMessage")
                                        }
                                    }
                                )
                            } else {
                                val data = hashMapOf(
                                    "userId" to currentUserUid,
                                    "userEmail" to userEmail,
                                    "jobId" to job.id,
                                    "jobTitle" to job.title,
                                    "reward" to job.reward,
                                    "message" to message,
                                    "screenshotUrl" to "",
                                    "status" to "Pending",
                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("micro_job_submissions")
                                    .add(data)
                                    .addOnCompleteListener { task ->
                                        isSubmitting = false
                                        if (task.isSuccessful) {
                                            onSubmit("Proof submitted successfully!")
                                        } else {
                                            onSubmit("Failed to submit proof: ${task.exception?.localizedMessage}")
                                        }
                                    }
                            }
                        },
                        enabled = !isSubmitting && (message.isNotBlank() || selectedUri != null)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroJobHistoryDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserUid = UserSession.getUid(context)

    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("micro_job_submissions")
                .whereEqualTo("userId", currentUserUid)
                .get()
                .addOnSuccessListener { result ->
                    val list = result.documents.map { doc ->
                        val map = doc.data ?: emptyMap<String, Any>()
                        map + ("id" to doc.id)
                    }
                    historyList = list
                    isLoading = false
                }
                .addOnFailureListener {
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
                        title = { Text("Micro Job History", color = Color.Black) },
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
                        items(historyList) { submission ->
                            val title = submission["jobTitle"] as? String ?: "Micro Job"
                            val reward = when (val value = submission["reward"]) {
                                is Number -> value.toDouble()
                                is String -> value.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            val status = submission["status"] as? String ?: "Pending"
                            val message = submission["message"] as? String ?: ""
                            val screenshots = when (val value = submission["screenshotsCount"]) {
                                is Number -> value.toInt()
                                is String -> value.toIntOrNull() ?: 0
                                else -> 0
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Screenshots added: $screenshots", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("৳${String.format("%.2f", reward)}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val statusColor = when (status.lowercase()) {
                                                "success", "approved", "completed" -> Color(0xFF4CAF50)
                                                "failed", "rejected" -> MaterialTheme.colorScheme.error
                                                else -> Color(0xFFFF9800)
                                            }
                                            Text(status, color = statusColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    if (message.isNotBlank()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        Text("Message: $message", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
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
