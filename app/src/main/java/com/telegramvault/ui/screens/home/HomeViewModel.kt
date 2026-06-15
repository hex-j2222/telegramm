package com.telegramvault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.data.model.UiState
import com.telegramvault.domain.usecase.DeleteAccountUseCase
import com.telegramvault.domain.usecase.GetAccountsUseCase
import com.telegramvault.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAccounts: GetAccountsUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val repo: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TelegramAccount>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TelegramAccount>>> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event: SharedFlow<HomeEvent> = _event.asSharedFlow()

    init { loadAccounts() }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts().catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }.collect { list ->
                _uiState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            }
        }
    }

    fun onDeleteAccount(account: TelegramAccount) {
        viewModelScope.launch {
            try {
                deleteAccount(account.id)
                _event.emit(HomeEvent.AccountDeleted(account.displayName))
            } catch (e: Exception) {
                _event.emit(HomeEvent.Error(e.message ?: "Delete failed"))
            }
        }
    }

    fun onAccountClicked(account: TelegramAccount) {
        viewModelScope.launch {
            repo.updateLastUsed(account.id)
            _event.emit(HomeEvent.OpenProfile(account))
        }
    }
}

sealed class HomeEvent {
    data class AccountDeleted(val name: String) : HomeEvent()
    data class OpenProfile(val account: TelegramAccount) : HomeEvent()
    data class Error(val message: String) : HomeEvent()
}
