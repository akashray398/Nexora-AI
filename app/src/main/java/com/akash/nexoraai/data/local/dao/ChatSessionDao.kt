package com.akash.nexoraai.data.local.dao

import androidx.room.*
import com.akash.nexoraai.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: ChatSessionEntity): Long

    @Delete
    fun deleteSession(session: ChatSessionEntity): Int
}
