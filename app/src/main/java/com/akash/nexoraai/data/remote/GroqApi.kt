package com.akash.nexoraai.data.remote

import com.akash.nexoraai.data.remote.dto.GroqRequest
import com.akash.nexoraai.data.remote.dto.GroqResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequest
    ): GroqResponse
}
