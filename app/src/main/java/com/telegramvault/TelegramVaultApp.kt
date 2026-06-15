package com.telegramvault

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.telegramvault.data.local.prefs.AppPreferences
import com.telegramvault.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TelegramVaultApp : Application() {

    @Inject lateinit var prefs: AppPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    fun applyTheme(theme: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                "dark"  -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else    -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    companion object {
        lateinit var instance: TelegramVaultApp
            private set
    }
}
