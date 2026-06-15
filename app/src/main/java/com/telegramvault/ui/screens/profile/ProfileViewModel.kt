package com.telegramvault.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.data.remote.TdLibClient
import com.telegramvault.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: AccountRepository,
    private val tdLib: TdLibClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ProfileEvent>()
    val event: SharedFlow<ProfileEvent> = _event.asSharedFlow()

    private var currentAccount: TelegramAccount? = null

    fun loadAccount(account: TelegramAccount) {
        currentAccount = account
        _uiState.value = ProfileUiState.Loaded(account)
        // Resume TDLib session to show real data
        if (account.apiId > 0) {
            viewModelScope.launch {
                val sessionDir = "session_${account.phoneNumber.replace("+","").replace(" ","")}"
                tdLib.initialize(account.apiId, account.apiHash, account.phoneNumber, sessionDir)
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, bio: String) {
        val account = currentAccount ?: return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            tdLib.updateUserProfile(firstName, lastName, bio) { success ->
                viewModelScope.launch {
                    if (success) {
                        val updated = account.copy(firstName = firstName, lastName = lastName, bio = bio)
                        repo.updateAccount(updated)
                        currentAccount = updated
                        _uiState.value = ProfileUiState.Loaded(updated)
                        _event.emit(ProfileEvent.UpdateSuccess)
                    } else {
                        _uiState.value = ProfileUiState.Loaded(account)
                        _event.emit(ProfileEvent.Error("Update failed"))
                    }
                }
            }
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(val account: TelegramAccount) : ProfileUiState()
}

sealed class ProfileEvent {
    object UpdateSuccess : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}
