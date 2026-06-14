package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WebTask(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val timerSeconds: Long = 60,
    val reward: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebTasksScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var tasks by remember { mutableStateOf<List<WebTask>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var executingTask by remember { mutableStateOf<WebTask?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    com.example.ui.screens.WhiteStatusBarFix()

    LaunchedEffect(Unit) {
        db.collection("web_tasks").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                tasks = snapshot.documents.mapNotNull { it.toObject(WebTask::class.java)?.copy(id = it.id) }
            }
            isLoading = false
        }
    }

    if (executingTask != null) {
        WebTaskExecutionScreen(
            task = executingTask!!,
            onBack = { executingTask = null },
            onRewardEarned = {
                executingTask = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            com.example.ui.screens.BeautifulHeader {
                TopAppBar(
                    title = { Text("Web Tasks", color = Color.Black, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No web tasks available right now.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasks) { task ->
                    WebTaskCard(task = task, onClick = { executingTask = task })
                }
            }
        }
    }
}

@Composable
fun WebTaskCard(task: WebTask, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${task.timerSeconds}s", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Reward: ৳${task.reward}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWebTaskDialog(onDismiss: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Web Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL (http/https)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = timer,
                    onValueChange = { timer = it },
                    label = { Text("Timer (Seconds)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reward,
                    onValueChange = { reward = it },
                    label = { Text("Reward Amount (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && url.isNotEmpty() && timer.isNotEmpty() && reward.isNotEmpty()) {
                        isSaving = true
                        coroutineScope.launch {
                            try {
                                val newTask = WebTask(
                                    title = title,
                                    url = url,
                                    timerSeconds = timer.toLongOrNull() ?: 60,
                                    reward = reward.toDoubleOrNull() ?: 0.0
                                )
                                db.collection("web_tasks").add(newTask).await()
                                onDismiss()
                            } catch (e: Exception) {
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebTaskExecutionScreen(task: WebTask, onBack: () -> Unit, onRewardEarned: () -> Unit) {
    var timerRemaining by remember { mutableStateOf(task.timerSeconds) }
    var taskCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isRewarding by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUserUid = UserSession.getUid(context)

    LaunchedEffect(Unit) {
        while (timerRemaining > 0) {
            delay(1000)
            timerRemaining--
        }
        taskCompleted = true
        // Auto reward
        if (currentUserUid.isNotEmpty()) {
            isRewarding = true
            try {
                val userRef = db.collection("users").document(currentUserUid)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentBal = snapshot.getDouble("balance") ?: 0.0
                    val currentEarn = snapshot.getDouble("totalEarned") ?: 0.0
                    transaction.update(userRef, "balance", currentBal + task.reward)
                    transaction.update(userRef, "totalEarned", currentEarn + task.reward)
                }.await()
                com.example.utils.ReferralCommissionHelper.applyCommission(currentUserUid, task.reward)
                onRewardEarned()
            } catch (e: Exception) {
            } finally {
                isRewarding = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (taskCompleted) {
                        Text("Task Completed! Reward added.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        if (isRewarding) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Text("Please wait...", fontWeight = FontWeight.SemiBold)
                        Text("$timerRemaining seconds left", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(task.url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
