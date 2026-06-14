package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ListItem(
                headlineContent = { Text("Profile Information", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Update your name and contact details") },
                leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.clickable { /* Navigate */ }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Notifications", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Manage alert preferences") },
                leadingContent = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                modifier = Modifier.clickable { /* Navigate */ }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Payment Methods", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Manage withdrawal accounts") },
                leadingContent = { Icon(Icons.Filled.Payment, contentDescription = null) },
                modifier = Modifier.clickable { /* Navigate */ }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Security", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Change password and PIN") },
                leadingContent = { Icon(Icons.Filled.Lock, contentDescription = null) },
                modifier = Modifier.clickable { /* Navigate */ }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { /* Log out */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log Out")
            }
        }
    }
}
