package com.akash.nexoraai.domain.repository

import com.akash.nexoraai.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(message: String): Flow<Result<String>>
    fun getRecentConversations(): Flow<List<ChatSessionEntity>>
    suspend fun saveConversation(title: String, iconName: String, lastMessage: String)
}
