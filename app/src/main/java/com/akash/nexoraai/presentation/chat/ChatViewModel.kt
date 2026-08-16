package com.akash.nexoraai.presentation.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akash.nexoraai.ChatMessage
import com.akash.nexoraai.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>(
        ChatMessage("Hi! I'm Nexora.\nHow can I help you today?", false)
    )
    val messages: List<ChatMessage> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val recentConversations = _searchQuery
        .combine(repository.getRecentConversations()) { query, sessions ->
            if (query.isBlank()) sessions
            else sessions.filter { it.title.contains(query, ignoreCase = true) || it.lastMessage.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendMessage(text: String, systemPrompt: String? = null) {
        if (text.isBlank() && systemPrompt == null) return

        val isFirstMessage = _messages.size <= 1
        
        if (text.isNotBlank()) {
            _messages.add(ChatMessage(text, true))
        }
        _isLoading.value = true

        viewModelScope.launch {
            // Updated Instruction: Professional, organized, but concise by default.
            val structuralInstruction = """
                Default Persona: Conversational, helpful, and concise. 
                Formatting Rules:
                - If the user greeting (e.g. Hi, Hello), respond briefly (max 1 sentence).
                - Use clear HEADINGS and BULLET POINTS for technical/complex answers.
                - Use **BOLD** for keywords.
                - Keep it short and satisfying unless the user explicitly asks for a "detailed" or "long" explanation.
            """.trimIndent()

            val fullPrompt = when {
                systemPrompt != null && text.isNotBlank() -> "$structuralInstruction\n\n$systemPrompt\n\n$text"
                systemPrompt != null -> "$structuralInstruction\n\n$systemPrompt"
                else -> "$structuralInstruction\n\n$text"
            }

            repository.sendMessage(fullPrompt).collect { result ->
                _isLoading.value = false
                result.onSuccess { response ->
                    _messages.add(ChatMessage(response, false))
                    
                    // Save to history if it's the first exchange
                    if (isFirstMessage) {
                        repository.saveConversation(
                            title = if (text.isNotBlank()) text else "Special Task",
                            iconName = "Chat",
                            lastMessage = response.take(50)
                        )
                    }
                }
                result.onFailure { error ->
                    _messages.add(ChatMessage("Error: ${error.message}", false))
                }
            }
        }
    }
    
    fun clearChat() {
        _messages.clear()
        _messages.add(ChatMessage("Hi! I'm Nexora.\nHow can I help you today?", false))
    }
}
