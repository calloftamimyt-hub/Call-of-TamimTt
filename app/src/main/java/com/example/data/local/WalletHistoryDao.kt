package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WalletHistoryEntity)

    @Query("SELECT * FROM wallet_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<WalletHistoryEntity>>
    
    @Query("SELECT * FROM wallet_history WHERE type = 'Deposit' ORDER BY timestamp DESC")
    fun getDepositHistory(): Flow<List<WalletHistoryEntity>>
    
    @Query("SELECT * FROM wallet_history WHERE type = 'Withdraw' ORDER BY timestamp DESC")
    fun getWithdrawHistory(): Flow<List<WalletHistoryEntity>>
    
    @Query("SELECT * FROM wallet_history WHERE type = 'Earning' ORDER BY timestamp DESC")
    fun getEarningHistory(): Flow<List<WalletHistoryEntity>>

    @Query("SELECT SUM(amount) FROM wallet_history WHERE type = 'Earning' AND timestamp >= :startTime")
    suspend fun getEarningsSince(startTime: Long): Double?
}
