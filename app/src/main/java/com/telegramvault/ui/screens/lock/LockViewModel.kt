package com.telegramvault.ui.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramvault.data.local.prefs.AppPreferences
import com.telegramvault.security.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockState(
    val enteredLength: Int = 0,
    val error: String = "",
    val unlocked: Boolean = false
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val crypto: CryptoManager
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    private val enteredPin = StringBuilder()
    private var storedPinHash = ""
    private var storedPatternHash = ""

    init {
        viewModelScope.launch {
            prefs.pinHash.first().let { storedPinHash = it }
            prefs.patternHash.first().let { storedPatternHash = it }
        }
    }

    fun onDigit(digit: String) {
        if (enteredPin.length >= 6) return
        enteredPin.append(digit)
        _state.value = _state.value.copy(enteredLength = enteredPin.length, error = "")
        if (enteredPin.length == 6) verifyPin()
    }

    fun onDelete() {
        if (enteredPin.isEmpty()) return
        enteredPin.deleteCharAt(enteredPin.length - 1)
        _state.value = _state.value.copy(enteredLength = enteredPin.length, error = "")
    }

    private fun verifyPin() {
        val inputHash = crypto.hashPassword(enteredPin.toString())
        enteredPin.clear()
        if (inputHash == storedPinHash) {
            _state.value = _state.value.copy(unlocked = true, enteredLength = 0)
        } else {
            _state.value = _state.value.copy(
                enteredLength = 0,
                error = "Incorrect PIN. Try again."
            )
        }
    }

    fun onBiometricSuccess() {
        _state.value = _state.value.copy(unlocked = true)
    }

    fun verifyPattern(pattern: String) {
        val hash = crypto.hashPassword(pattern)
        if (hash == storedPatternHash) {
            _state.value = _state.value.copy(unlocked = true)
        } else {
            _state.value = _state.value.copy(error = "Wrong pattern.")
        }
    }
}
