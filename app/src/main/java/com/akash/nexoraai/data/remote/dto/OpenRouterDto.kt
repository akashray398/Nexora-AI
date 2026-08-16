package com.akash.nexoraai.data.remote.dto

data class OpenRouterRequest(
    val model: String = "google/gemini-2.0-flash-001",
    val messages: List<OpenRouterMessage>
)

data class OpenRouterMessage(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>
)

data class OpenRouterChoice(
    val message: OpenRouterMessage
)
