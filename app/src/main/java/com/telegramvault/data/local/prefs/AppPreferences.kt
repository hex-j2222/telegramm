package com.telegramvault.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val LANGUAGE        = stringPreferencesKey("language")
        val THEME           = stringPreferencesKey("theme")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val PATTERN_ENABLED = booleanPreferencesKey("pattern_enabled")
        val PIN_ENABLED     = booleanPreferencesKey("pin_enabled")
        val PATTERN_HASH    = stringPreferencesKey("pattern_hash")
        val PIN_HASH        = stringPreferencesKey("pin_hash")
        val AUTO_LOCK_TIME  = intPreferencesKey("auto_lock_time")
        val FIRST_LAUNCH    = booleanPreferencesKey("first_launch")
        val GOOGLE_SIGNED_IN = booleanPreferencesKey("google_signed_in")
        val DRIVE_BACKUP    = booleanPreferencesKey("drive_backup")
        val NOTIFICATIONS   = booleanPreferencesKey("notifications_enabled")
        val LOCK_ENABLED    = booleanPreferencesKey("lock_enabled")
    }

    val language: Flow<String> = context.dataStore.data.catchIO().map { it[Keys.LANGUAGE] ?: "system" }
    val theme: Flow<String>    = context.dataStore.data.catchIO().map { it[Keys.THEME] ?: "dark" }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.catchIO().map { it[Keys.BIOMETRIC_ENABLED] ?: false }
    val patternEnabled: Flow<Boolean>   = context.dataStore.data.catchIO().map { it[Keys.PATTERN_ENABLED] ?: false }
    val pinEnabled: Flow<Boolean>       = context.dataStore.data.catchIO().map { it[Keys.PIN_ENABLED] ?: false }
    val lockEnabled: Flow<Boolean>      = context.dataStore.data.catchIO().map { it[Keys.LOCK_ENABLED] ?: false }
    val patternHash: Flow<String>       = context.dataStore.data.catchIO().map { it[Keys.PATTERN_HASH] ?: "" }
    val pinHash: Flow<String>           = context.dataStore.data.catchIO().map { it[Keys.PIN_HASH] ?: "" }
    val autoLockTime: Flow<Int>         = context.dataStore.data.catchIO().map { it[Keys.AUTO_LOCK_TIME] ?: 60 }
    val firstLaunch: Flow<Boolean>      = context.dataStore.data.catchIO().map { it[Keys.FIRST_LAUNCH] ?: true }
    val googleSignedIn: Flow<Boolean>   = context.dataStore.data.catchIO().map { it[Keys.GOOGLE_SIGNED_IN] ?: false }
    val driveBackup: Flow<Boolean>      = context.dataStore.data.catchIO().map { it[Keys.DRIVE_BACKUP] ?: false }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.catchIO().map { it[Keys.NOTIFICATIONS] ?: true }

    private fun Flow<Preferences>.catchIO() = catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    suspend fun setLanguage(lang: String)           = context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    suspend fun setTheme(theme: String)             = context.dataStore.edit { it[Keys.THEME] = theme }
    suspend fun setBiometricEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    suspend fun setPatternEnabled(enabled: Boolean)  = context.dataStore.edit { it[Keys.PATTERN_ENABLED] = enabled }
    suspend fun setPinEnabled(enabled: Boolean)      = context.dataStore.edit { it[Keys.PIN_ENABLED] = enabled }
    suspend fun setLockEnabled(enabled: Boolean)     = context.dataStore.edit { it[Keys.LOCK_ENABLED] = enabled }
    suspend fun setPatternHash(hash: String)         = context.dataStore.edit { it[Keys.PATTERN_HASH] = hash }
    suspend fun setPinHash(hash: String)             = context.dataStore.edit { it[Keys.PIN_HASH] = hash }
    suspend fun setAutoLockTime(seconds: Int)        = context.dataStore.edit { it[Keys.AUTO_LOCK_TIME] = seconds }
    suspend fun setFirstLaunch(first: Boolean)       = context.dataStore.edit { it[Keys.FIRST_LAUNCH] = first }
    suspend fun setGoogleSignedIn(signed: Boolean)   = context.dataStore.edit { it[Keys.GOOGLE_SIGNED_IN] = signed }
    suspend fun setDriveBackup(enabled: Boolean)     = context.dataStore.edit { it[Keys.DRIVE_BACKUP] = enabled }
    suspend fun setNotificationsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
}
