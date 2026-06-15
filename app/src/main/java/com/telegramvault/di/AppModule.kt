package com.telegramvault.di

import android.content.Context
import com.telegramvault.data.local.db.AccountDao
import com.telegramvault.data.local.db.AppDatabase
import com.telegramvault.security.CryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        crypto: CryptoManager
    ): AppDatabase = AppDatabase.getInstance(context, crypto.getDatabaseKey())

    @Provides
    @Singleton
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
}
