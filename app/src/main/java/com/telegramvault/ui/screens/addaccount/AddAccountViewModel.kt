package com.telegramvault.ui.screens.addaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramvault.data.model.AuthState
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.data.remote.TdLibClient
import com.telegramvault.domain.usecase.SaveAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val tdLib: TdLibClient,
    private val saveAccount: SaveAccountUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = tdLib.authState

    private val _event = MutableSharedFlow<AddAccountEvent>()
    val event: SharedFlow<AddAccountEvent> = _event.asSharedFlow()

    var phone: String = ""

    init {
        viewModelScope.launch {
            tdLib.authState.collect { state ->
                when (state) {
                    is AuthState.Success -> onAuthSuccess(state.account)
                    is AuthState.Error   -> _event.emit(AddAccountEvent.ShowError(state.message))
                    else -> {}
                }
            }
        }
    }

    fun startAuth(phone: String) {
        this.phone = phone
        val sessionDir = "session_${phone.replace("+", "").replace(" ", "")}"
        tdLib.initialize(phone, sessionDir)
    }

    fun submitCode(code: String) = tdLib.submitCode(code)

    fun submitPassword(password: String) = tdLib.submitPassword(password)

    fun submitRegistration(firstName: String, lastName: String) =
        tdLib.submitRegistration(firstName, lastName)

    private fun onAuthSuccess(account: TelegramAccount) {
        viewModelScope.launch {
            try {
                val id = saveAccount(account)
                _event.emit(AddAccountEvent.AccountSaved(id))
            } catch (e: Exception) {
                _event.emit(AddAccountEvent.ShowError(e.message ?: "Save failed"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Don't close — session stays active for code receiving
    }
}

sealed class AddAccountEvent {
    data class ShowError(val message: String) : AddAccountEvent()
    data class AccountSaved(val id: Long)     : AddAccountEvent()
}
