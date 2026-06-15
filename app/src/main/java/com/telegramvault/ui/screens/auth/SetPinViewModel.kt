package com.telegramvault.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramvault.data.local.prefs.AppPreferences
import com.telegramvault.security.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetPinState(
    val enteredLength: Int = 0,
    val isConfirming: Boolean = false,
    val error: String = "",
    val done: Boolean = false
)

@HiltViewModel
class SetPinViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val crypto: CryptoManager
) : ViewModel() {

    private val _state = MutableStateFlow(SetPinState())
    val state: StateFlow<SetPinState> = _state.asStateFlow()

    private val firstPin = StringBuilder()
    private val confirmPin = StringBuilder()

    fun onDigit(digit: String) {
        val current = if (_state.value.isConfirming) confirmPin else firstPin
        if (current.length >= 6) return
        current.append(digit)
        _state.value = _state.value.copy(enteredLength = current.length, error = "")
        if (current.length == 6) onPinComplete()
    }

    fun onDelete() {
        val current = if (_state.value.isConfirming) confirmPin else firstPin
        if (current.isEmpty()) return
        current.deleteCharAt(current.length - 1)
        _state.value = _state.value.copy(enteredLength = current.length, error = "")
    }

    private fun onPinComplete() {
        if (!_state.value.isConfirming) {
            _state.value = _state.value.copy(isConfirming = true, enteredLength = 0)
        } else {
            if (firstPin.toString() == confirmPin.toString()) {
                viewModelScope.launch {
                    val hash = crypto.hashPassword(firstPin.toString())
                    prefs.setPinHash(hash)
                    prefs.setPinEnabled(true)
                    prefs.setLockEnabled(true)
                    _state.value = _state.value.copy(done = true)
                }
            } else {
                confirmPin.clear()
                firstPin.clear()
                _state.value = SetPinState(error = "PINs do not match. Try again.")
            }
        }
    }
}
