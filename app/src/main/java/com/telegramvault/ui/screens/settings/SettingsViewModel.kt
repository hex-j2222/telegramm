package com.telegramvault.ui.screens.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.telegramvault.data.local.prefs.AppPreferences
import com.telegramvault.TelegramVaultApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val biometricEnabled: Boolean = false,
    val patternEnabled:   Boolean = false,
    val pinEnabled:       Boolean = false,
    val notificationsEnabled: Boolean = true,
    val googleSignedIn:   Boolean = false,
    val driveBackup:      Boolean = false,
    val language:         String  = "system",
    val theme:            String  = "dark"
)

sealed class SettingsEvent {
    object RestartActivity : SettingsEvent()
    data class ShowMessage(val msg: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(private val prefs: AppPreferences) : ViewModel() {

    val settings: StateFlow<SettingsState> = combine(
        prefs.biometricEnabled, prefs.patternEnabled, prefs.pinEnabled,
        prefs.notificationsEnabled, prefs.googleSignedIn, prefs.driveBackup,
        prefs.language, prefs.theme
    ) { arr ->
        SettingsState(
            arr[0] as Boolean, arr[1] as Boolean, arr[2] as Boolean,
            arr[3] as Boolean, arr[4] as Boolean, arr[5] as Boolean,
            arr[6] as String,  arr[7] as String
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    private val _event = MutableSharedFlow<SettingsEvent>()
    val event: SharedFlow<SettingsEvent> = _event.asSharedFlow()

    fun setBiometric(enabled: Boolean) = viewModelScope.launch { prefs.setBiometricEnabled(enabled) }
    fun disablePattern() = viewModelScope.launch { prefs.setPatternEnabled(false); prefs.setPatternHash("") }
    fun disablePin()     = viewModelScope.launch { prefs.setPinEnabled(false); prefs.setPinHash("") }
    fun setNotifications(enabled: Boolean) = viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }

    fun setTheme(theme: String) = viewModelScope.launch {
        prefs.setTheme(theme)
        TelegramVaultApp.instance.applyTheme(theme)
        _event.emit(SettingsEvent.RestartActivity)
    }

    fun setLanguage(lang: String) = viewModelScope.launch {
        prefs.setLanguage(lang)
        _event.emit(SettingsEvent.RestartActivity)
    }

    fun toggleGoogleDrive(activity: Activity) = viewModelScope.launch {
        val isSignedIn = GoogleSignIn.getLastSignedInAccount(activity) != null
        if (isSignedIn) {
            GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            prefs.setGoogleSignedIn(false)
            prefs.setDriveBackup(false)
            _event.emit(SettingsEvent.ShowMessage("Google Drive disconnected"))
        } else {
            _event.emit(SettingsEvent.ShowMessage("Please sign in with Google"))
        }
    }
}
