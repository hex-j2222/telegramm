package com.telegramvault.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "telegram_accounts")
data class TelegramAccount(
    @PrimaryKey val id: Long = 0L,
    val phoneNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val bio: String = "",
    val profilePhotoPath: String = "",
    val apiId: Int = 0,
    val apiHash: String = "",
    val encryptedSession: String = "",
    val isActive: Boolean = true,
    val hasCloudPassword: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val displayOrder: Int = 0,
    val phoneCodeHash: String = ""
) : Parcelable {
    val displayName: String get() = "$firstName $lastName".trim().ifEmpty { phoneNumber }
    val initials: String get() = buildString {
        firstName.firstOrNull()?.let { append(it.uppercaseChar()) }
        lastName.firstOrNull()?.let { append(it.uppercaseChar()) }
        if (isEmpty()) append(phoneNumber.lastOrNull() ?: '?')
    }
}
