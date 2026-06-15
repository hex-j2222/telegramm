package com.telegramvault.domain.repository

import com.telegramvault.data.local.db.AccountDao
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val dao: AccountDao,
    private val crypto: CryptoManager
) {
    fun getAllAccounts(): Flow<List<TelegramAccount>> = dao.getAllAccounts()

    suspend fun getAccount(id: Long): TelegramAccount? = dao.getAccountById(id)

    suspend fun saveAccount(account: TelegramAccount): Long {
        val secured = account.copy(
            encryptedSession = if (account.encryptedSession.isNotEmpty())
                crypto.encrypt(account.encryptedSession) else ""
        )
        return dao.insertAccount(secured)
    }

    suspend fun updateAccount(account: TelegramAccount) {
        dao.updateAccount(account)
    }

    suspend fun deleteAccount(id: Long) = dao.deleteAccount(id)

    suspend fun getAccountCount(): Int = dao.getAccountCount()

    suspend fun updateLastUsed(id: Long) = dao.updateLastUsed(id, System.currentTimeMillis())

    suspend fun updateProfilePhoto(id: Long, path: String) = dao.updateProfilePhoto(id, path)

    fun getDecryptedSession(account: TelegramAccount): String =
        if (account.encryptedSession.isNotEmpty()) crypto.decrypt(account.encryptedSession) else ""
}
