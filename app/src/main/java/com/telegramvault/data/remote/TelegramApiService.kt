package com.telegramvault.data.remote

import com.telegramvault.data.model.TelegramAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Telegram Bot API for receiving auth codes (supplemental feature)
interface TelegramBotApi {
    @GET("bot{token}/getUpdates")
    suspend fun getUpdates(
        @Path("token") token: String,
        @Query("offset") offset: Int = 0,
        @Query("timeout") timeout: Int = 30
    ): TelegramUpdatesResponse
}

data class TelegramUpdatesResponse(
    val ok: Boolean = false,
    val result: List<TelegramUpdate> = emptyList()
)

data class TelegramUpdate(
    val update_id: Int = 0,
    val message: TelegramMessage? = null
)

data class TelegramMessage(
    val text: String? = null,
    val date: Long = 0
)

@Singleton
class TelegramApiService @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.telegram.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val botApi: TelegramBotApi = retrofit.create(TelegramBotApi::class.java)
}
