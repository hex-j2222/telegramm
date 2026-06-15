package com.telegramvault.domain.usecase

import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(private val repo: AccountRepository) {
    operator fun invoke(): Flow<List<TelegramAccount>> = repo.getAllAccounts()
}
