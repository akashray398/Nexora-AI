package com.akash.nexoraai.data.remote

import com.akash.nexoraai.data.remote.dto.OpenRouterRequest
import com.akash.nexoraai.data.remote.dto.OpenRouterResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("api/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}
