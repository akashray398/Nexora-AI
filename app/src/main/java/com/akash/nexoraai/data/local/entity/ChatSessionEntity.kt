package com.akash.nexoraai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: String,
    val iconName: String, // e.g., "Chat", "Mic", "Code"
    val lastMessage: String,
    val timestamp: Long
)
