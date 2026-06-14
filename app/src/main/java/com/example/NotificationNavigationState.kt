package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object NotificationNavigationState {
    var shouldOpenNotificationsPage by mutableStateOf(false)
}
