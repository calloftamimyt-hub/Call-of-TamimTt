package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_history")
data class WalletHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Deposit", "Withdraw", "Earning"
    val amount: Double,
    val title: String, // E.g., "bKash Deposit", "Ad Earning", "Quiz Earning"
    val status: String, // "Success", "Pending", "Failed"
    val timestamp: Long,
    val notes: String = ""
)
