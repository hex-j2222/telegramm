package org.drinkless.tdlib

/**
 * TdApi stub - replace with real tdlib.jar.
 */
object TdApi {
    abstract class Object
    abstract class Function<R : Object>

    // Authorization States
    class AuthorizationStateWaitTdlibParameters : Object()
    class AuthorizationStateWaitPhoneNumber : Object()
    class AuthorizationStateWaitCode(val codeInfo: AuthenticationCodeInfo = AuthenticationCodeInfo()) : Object()
    class AuthorizationStateWaitPassword(val passwordHint: String? = null) : Object()
    class AuthorizationStateWaitRegistration(val termsOfService: TermsOfService? = null) : Object()
    class AuthorizationStateReady : Object()
    class AuthorizationStateClosed : Object()
    class AuthorizationStateClosing : Object()

    // Functions
    class SetLogVerbosityLevel(val newVerbosityLevel: Int) : Function<Ok>()
    class GetAuthorizationState : Function<Object>()
    class SetAuthenticationPhoneNumber(val phoneNumber: String, val settings: PhoneNumberAuthenticationSettings?) : Function<Ok>()
    class CheckAuthenticationCode(val code: String) : Function<Ok>()
    class CheckAuthenticationPassword(val password: String) : Function<Ok>()
    class SetTdlibParameters(val parameters: TdlibParameters) : Function<Ok>()
    class GetMe : Function<User>()
    class LogOut : Function<Ok>()
    // FIX: offset and limit must be Long to match TDLib API and usage in TdLibClient
    class DownloadFile(val fileId: Int, val priority: Int, val offset: Long, val limit: Long, val synchronous: Boolean) : Function<File>()
    class SetName(val firstName: String, val lastName: String) : Function<Ok>()
    class SetBio(val bio: String) : Function<Ok>()
    class GetUser(val userId: Long) : Function<User>()
    // FIX: Added missing RegisterUser class used in TdLibClient.submitRegistration()
    class RegisterUser(val firstName: String, val lastName: String) : Function<Ok>()

    // Updates
    class UpdateAuthorizationState(val authorizationState: Object) : Object()

    // Data Classes
    class Ok : Object()
    class Error(val code: Int = 0, val message: String = "") : Object()
    class User(
        val id: Long = 0L,
        val firstName: String? = null,
        val lastName: String? = null,
        val username: String? = null,
        val phoneNumber: String? = null,
        val profilePhoto: ProfilePhoto? = null
    ) : Object()
    class ProfilePhoto(val id: Long = 0L, val small: File = File(), val big: File = File())
    class File(val id: Int = 0, val size: Long = 0L, val local: LocalFile = LocalFile(), val remote: RemoteFile = RemoteFile())
    class LocalFile(val path: String = "", val isDownloadingCompleted: Boolean = false)
    class RemoteFile(val id: String = "", val uniqueId: String = "")
    class AuthenticationCodeInfo(val timeout: Int = 60, val type: Object? = null)
    class TermsOfService(val text: FormattedText? = null)
    class FormattedText(val text: String = "")
    class PhoneNumberAuthenticationSettings(
        val allowFlashCall: Boolean = false,
        val allowMissedCall: Boolean = false,
        val isCurrentPhoneNumber: Boolean = false,
        val hasUnknownPhoneNumber: Boolean = false,
        val allowSmsRetrieverApi: Boolean = false
    )
    class TdlibParameters {
        var databaseDirectory: String = ""
        var filesDirectory: String = ""
        var useMessageDatabase: Boolean = false
        var useSecretChats: Boolean = false
        var apiId: Int = 0
        var apiHash: String = ""
        var systemLanguageCode: String = "en"
        var deviceModel: String = ""
        var systemVersion: String = ""
        var applicationVersion: String = "1.0.0"
        var enableStorageOptimizer: Boolean = true
    }
}
