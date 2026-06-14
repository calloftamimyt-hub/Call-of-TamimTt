package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bonus_history")
data class BonusHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val source: String,
    val referralUserName: String,
    val timestamp: Long,
    val status: String
)
