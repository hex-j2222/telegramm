package com.telegramvault.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.telegramvault.data.model.TelegramAccount
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities  = [TelegramAccount::class],
    version   = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    companion object {
        private const val DB_NAME = "telegramvault.db"

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, passphrase: ByteArray): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context, passphrase).also { INSTANCE = it }
            }

        private fun build(context: Context, passphrase: ByteArray): AppDatabase {
            SQLiteDatabase.loadLibs(context)
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }

        fun destroy() { INSTANCE = null }
    }
}
