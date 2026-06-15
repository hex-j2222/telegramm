package com.telegramvault.data.local.db

import androidx.room.*
import com.telegramvault.data.model.TelegramAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM telegram_accounts ORDER BY displayOrder ASC, lastUsed DESC")
    fun getAllAccounts(): Flow<List<TelegramAccount>>

    @Query("SELECT * FROM telegram_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): TelegramAccount?

    @Query("SELECT * FROM telegram_accounts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getAccountByPhone(phone: String): TelegramAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: TelegramAccount): Long

    @Update
    suspend fun updateAccount(account: TelegramAccount)

    @Query("DELETE FROM telegram_accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("SELECT COUNT(*) FROM telegram_accounts")
    suspend fun getAccountCount(): Int

    @Query("UPDATE telegram_accounts SET lastUsed = :time WHERE id = :id")
    suspend fun updateLastUsed(id: Long, time: Long)

    @Query("UPDATE telegram_accounts SET displayOrder = :order WHERE id = :id")
    suspend fun updateDisplayOrder(id: Long, order: Int)

    @Query("UPDATE telegram_accounts SET profilePhotoPath = :path WHERE id = :id")
    suspend fun updateProfilePhoto(id: Long, path: String)
}
