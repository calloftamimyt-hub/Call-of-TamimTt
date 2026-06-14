package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BonusHistoryDao {
    @Query("SELECT * FROM bonus_history ORDER BY timestamp DESC")
    fun getAllBonusHistory(): Flow<List<BonusHistoryEntity>>

    @Insert
    suspend fun insertBonusHistory(bonusHistory: BonusHistoryEntity)

    @Query("DELETE FROM bonus_history")
    suspend fun clearAll()
}
