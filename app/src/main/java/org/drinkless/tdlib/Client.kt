package org.drinkless.tdlib

/**
 * TDLib Java Client - Stub interface matching official TDLib Java API.
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * SETUP INSTRUCTIONS (must do before building):
 *   1. Download official TDLib prebuilt for Android from:
 *      https://core.telegram.org/tdlib/getting-started
 *   2. Place tdlib.jar -> app/libs/tdlib.jar
 *   3. Place libtdjni.so for each ABI -> app/src/main/jniLibs/<abi>/
 *   4. Delete this stub file (it will conflict with the real jar)
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
class Client private constructor() {
    fun interface ResultHandler { fun onResult(obj: TdApi.Object) }
    fun interface ExceptionHandler { fun onException(e: Throwable) }

    fun send(query: TdApi.Function<*>, handler: (TdApi.Object) -> Unit) {}
    fun send(query: TdApi.Function<*>, resultHandler: ResultHandler?) {}
    fun close() {}

    companion object {
        @JvmStatic
        fun create(
            updateHandler: ResultHandler?,
            updateExceptionHandler: ExceptionHandler?,
            defaultExceptionHandler: ExceptionHandler?
        ): Client = Client()

        @JvmStatic
        fun execute(query: TdApi.Function<*>): TdApi.Object? = null
    }
}
