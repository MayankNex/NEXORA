package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float? = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int? = null,
    val stream: Boolean = true
)

@JsonClass(generateAdapter = true)
data class DeltaMessage(
    val role: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class StreamChoice(
    val index: Int? = null,
    val delta: DeltaMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class TokenUsage(
    @Json(name = "prompt_tokens") val promptTokens: Int? = null,
    @Json(name = "completion_tokens") val completionTokens: Int? = null,
    @Json(name = "total_tokens") val totalTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class ChatCompletionChunk(
    val id: String? = null,
    val choices: List<StreamChoice>? = null,
    val usage: TokenUsage? = null
)

@JsonClass(generateAdapter = true)
data class ModelItem(
    val id: String
)

@JsonClass(generateAdapter = true)
data class ModelListResponse(
    val data: List<ModelItem>? = null
)

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val latencyMs: Long,
    val message: String,
    val modelsFound: List<String> = emptyList()
)
