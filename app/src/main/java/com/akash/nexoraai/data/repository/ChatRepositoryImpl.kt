package com.akash.nexoraai.data.repository

import com.akash.nexoraai.data.local.dao.ChatSessionDao
import com.akash.nexoraai.data.local.entity.ChatSessionEntity
import com.akash.nexoraai.data.remote.GeminiApi
import com.akash.nexoraai.data.remote.GroqApi
import com.akash.nexoraai.data.remote.OpenRouterApi
import com.akash.nexoraai.data.remote.dto.*
import com.akash.nexoraai.domain.repository.ChatRepository
import com.akash.nexoraai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val geminiApi: GeminiApi,
    private val groqApi: GroqApi,
    private val openRouterApi: OpenRouterApi,
    private val chatSessionDao: ChatSessionDao
) : ChatRepository {

    private val geminiKey = BuildConfig.GEMINI_KEY
    private val groqKey = BuildConfig.GROQ_KEY
    private val openRouterKey = BuildConfig.OPENROUTER_KEY

    override fun sendMessage(message: String): Flow<Result<String>> = flow {
        // AI Router Logic: Groq -> Gemini -> OpenRouter

        // 1. Try Groq (Primary - Fast)
        try {
            val response = groqApi.getChatCompletion(
                authHeader = "Bearer $groqKey",
                request = GroqRequest(
                    messages = listOf(GroqMessage(role = "user", content = message))
                )
            )
            val result = response.choices.firstOrNull()?.message?.content
            if (result != null) {
                emit(Result.success(result))
                return@flow
            }
        } catch (e: Exception) {
            // Fail silently to try next provider
        }

        // 2. Try Gemini (Secondary - Powerful)
        try {
            val response = geminiApi.generateContent(
                apiKey = geminiKey,
                request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = message))))
                )
            )
            val result = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (result != null) {
                emit(Result.success(result))
                return@flow
            }
        } catch (e: Exception) {
            // Fail silently to try final fallback
        }

        // 3. Try OpenRouter (Final Fallback)
        try {
            val response = openRouterApi.getChatCompletion(
                authHeader = "Bearer $openRouterKey",
                request = OpenRouterRequest(
                    messages = listOf(OpenRouterMessage(role = "user", content = message))
                )
            )
            val result = response.choices.firstOrNull()?.message?.content
            if (result != null) {
                emit(Result.success(result))
            } else {
                emit(Result.failure(Exception("All models returned empty response")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Critical failure: All 3 AI providers (Groq, Gemini, OpenRouter) are currently unavailable.")))
        }
    }

    override fun getRecentConversations(): Flow<List<ChatSessionEntity>> {
        return chatSessionDao.getAllSessions()
    }

    override suspend fun saveConversation(title: String, iconName: String, lastMessage: String) {
        withContext(Dispatchers.IO) {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            chatSessionDao.insertSession(
                ChatSessionEntity(
                    title = title,
                    date = date,
                    iconName = iconName,
                    lastMessage = lastMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
