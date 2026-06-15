package com.telegramvault.domain.usecase

import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.domain.repository.AccountRepository
import javax.inject.Inject

class SaveAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(account: TelegramAccount): Long = repo.saveAccount(account)
}
