package com.akash.nexoraai.di

import android.content.Context
import androidx.room.Room
import com.akash.nexoraai.data.local.NexoraDatabase
import com.akash.nexoraai.data.local.dao.ChatSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNexoraDatabase(@ApplicationContext context: Context): NexoraDatabase {
        return Room.databaseBuilder(
            context,
            NexoraDatabase::class.java,
            "nexora_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideChatSessionDao(db: NexoraDatabase): ChatSessionDao {
        return db.chatSessionDao
    }
}
