package com.akash.nexoraai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akash.nexoraai.data.local.dao.ChatSessionDao
import com.akash.nexoraai.data.local.entity.ChatSessionEntity

@Database(entities = [ChatSessionEntity::class], version = 1, exportSchema = false)
abstract class NexoraDatabase : RoomDatabase() {
    abstract val chatSessionDao: ChatSessionDao
}
