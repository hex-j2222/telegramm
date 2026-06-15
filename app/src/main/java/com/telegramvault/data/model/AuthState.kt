package com.telegramvault.data.model

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class WaitingCode(val phoneCodeHash: String, val timeout: Int, val phone: String) : AuthState()
    data class WaitingPassword(val hint: String) : AuthState()
    data class WaitingRegistration(val termsOfService: String) : AuthState()
    data class Success(val account: TelegramAccount) : AuthState()
    data class Error(val message: String, val code: Int = 0) : AuthState()
}
