package com.telegramvault.data.remote

import android.content.Context
import android.util.Log
import com.telegramvault.BuildConfig
import com.telegramvault.data.model.AuthState
import com.telegramvault.data.model.TelegramAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TdLibClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "TdLibClient"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var client: Client? = null
    private var currentPhone: String = ""
    private var currentSessionDir: String = ""

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        init {
            try {
                System.loadLibrary("tdjni")
            } catch (e: UnsatisfiedLinkError) {
                Log.w("TdLibClient", "libtdjni not found — stub mode active")
            }
        }

        // Credentials come from BuildConfig (injected from local.properties at build time)
        val API_ID: Int    get() = BuildConfig.TG_API_ID
        val API_HASH: String get() = BuildConfig.TG_API_HASH
    }

    fun initialize(phone: String, sessionDir: String) {
        currentPhone    = phone
        currentSessionDir = sessionDir

        client?.close()
        client = Client.create(
            { update -> handleUpdate(update) },
            { e    -> Log.e(TAG, "Update exception: $e") },
            { Log.d(TAG, "Client closed") }
        )

        scope.launch {
            client?.send(TdApi.SetLogVerbosityLevel(0)) {}
            client?.send(TdApi.SetTdlibParameters(buildParameters(sessionDir))) {}
        }
    }

    private fun buildParameters(sessionDir: String): TdApi.TdlibParameters {
        val dir = File(context.filesDir, sessionDir).absolutePath
        return TdApi.TdlibParameters().apply {
            databaseDirectory      = dir
            filesDirectory         = dir
            useMessageDatabase     = false
            useSecretChats         = false
            apiId                  = API_ID
            apiHash                = API_HASH
            systemLanguageCode     = "en"
            deviceModel            = android.os.Build.MODEL
            systemVersion          = android.os.Build.VERSION.RELEASE
            applicationVersion     = "1.0.0"
            enableStorageOptimizer = true
        }
    }

    private fun handleUpdate(update: TdApi.Object) {
        scope.launch {
            when (update) {
                is TdApi.UpdateAuthorizationState ->
                    handleAuthState(update.authorizationState)
                else -> {}
            }
        }
    }

    private fun handleAuthState(state: TdApi.Object) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> { /* params already sent */ }

            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _authState.value = AuthState.Loading
                val settings = TdApi.PhoneNumberAuthenticationSettings().apply {
                    allowFlashCall        = false
                    allowMissedCall       = false
                    isCurrentPhoneNumber  = false
                    hasUnknownPhoneNumber = false
                    allowSmsRetrieverApi  = false
                }
                client?.send(TdApi.SetAuthenticationPhoneNumber(currentPhone, settings)) { res ->
                    if (res is TdApi.Error)
                        scope.launch { _authState.value = AuthState.Error(res.message, res.code) }
                }
            }

            is TdApi.AuthorizationStateWaitCode -> {
                _authState.value = AuthState.WaitingCode(
                    phoneCodeHash = "",
                    timeout       = state.codeInfo.timeout,
                    phone         = currentPhone
                )
            }

            is TdApi.AuthorizationStateWaitPassword -> {
                _authState.value = AuthState.WaitingPassword(
                    hint = state.passwordHint ?: ""
                )
            }

            is TdApi.AuthorizationStateWaitRegistration -> {
                _authState.value = AuthState.WaitingRegistration(
                    termsOfService = state.termsOfService?.text?.text ?: ""
                )
            }

            is TdApi.AuthorizationStateReady -> fetchUserInfo()

            is TdApi.AuthorizationStateClosed -> {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun submitCode(code: String) {
        _authState.value = AuthState.Loading
        client?.send(TdApi.CheckAuthenticationCode(code)) { res ->
            if (res is TdApi.Error)
                scope.launch { _authState.value = AuthState.Error(res.message, res.code) }
        }
    }

    fun submitPassword(password: String) {
        _authState.value = AuthState.Loading
        client?.send(TdApi.CheckAuthenticationPassword(password)) { res ->
            if (res is TdApi.Error)
                scope.launch { _authState.value = AuthState.Error(res.message, res.code) }
        }
    }

    fun submitRegistration(firstName: String, lastName: String) {
        client?.send(TdApi.RegisterUser(firstName, lastName)) { res ->
            if (res is TdApi.Error)
                scope.launch { _authState.value = AuthState.Error(res.message, res.code) }
        }
    }

    private fun fetchUserInfo() {
        client?.send(TdApi.GetMe()) { res ->
            scope.launch {
                if (res is TdApi.User) {
                    val account = TelegramAccount(
                        id           = res.id,
                        phoneNumber  = res.phoneNumber ?: currentPhone,
                        firstName    = res.firstName  ?: "",
                        lastName     = res.lastName   ?: "",
                        username     = res.username   ?: "",
                        apiId        = API_ID,
                        apiHash      = API_HASH,
                        encryptedSession = currentSessionDir
                    )
                    _authState.value = AuthState.Success(account)
                    // Download profile photo async
                    downloadProfilePhoto(res)
                } else if (res is TdApi.Error) {
                    _authState.value = AuthState.Error(res.message, res.code)
                }
            }
        }
    }

    private fun downloadProfilePhoto(user: TdApi.User) {
        val photoId = user.profilePhoto?.small?.id ?: return
        client?.send(TdApi.DownloadFile(photoId, 1, 0L, 0L, true)) { res ->
            if (res is TdApi.File && res.local.isDownloadingCompleted) {
                Log.d(TAG, "Profile photo saved: ${res.local.path}")
            }
        }
    }

    fun refreshUserInfo(onResult: (TelegramAccount?) -> Unit) {
        client?.send(TdApi.GetMe()) { res ->
            if (res is TdApi.User) {
                val account = TelegramAccount(
                    id          = res.id,
                    phoneNumber = res.phoneNumber ?: currentPhone,
                    firstName   = res.firstName   ?: "",
                    lastName    = res.lastName    ?: "",
                    username    = res.username    ?: "",
                    apiId       = API_ID,
                    apiHash     = API_HASH
                )
                onResult(account)
            } else {
                onResult(null)
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, bio: String, onResult: (Boolean) -> Unit) {
        client?.send(TdApi.SetName(firstName, lastName)) { res ->
            if (res is TdApi.Error) { onResult(false); return@send }
            client?.send(TdApi.SetBio(bio)) { res2 ->
                onResult(res2 !is TdApi.Error)
            }
        }
    }

    fun resumeSession(sessionDir: String, phone: String) {
        currentPhone      = phone
        currentSessionDir = sessionDir
        initialize(phone, sessionDir)
    }

    fun logOut(onComplete: () -> Unit) {
        client?.send(TdApi.LogOut()) { onComplete() }
    }

    fun close() {
        client?.close()
        client = null
    }

    fun isReady(): Boolean =
        _authState.value is AuthState.Success || _authState.value is AuthState.Idle
}
