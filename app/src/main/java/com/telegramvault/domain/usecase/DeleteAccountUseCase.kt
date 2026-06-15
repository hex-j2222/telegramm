package com.telegramvault.domain.usecase

import com.telegramvault.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteAccount(id)
}
