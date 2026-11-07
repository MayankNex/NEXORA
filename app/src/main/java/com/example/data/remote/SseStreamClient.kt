package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

sealed interface StreamEvent {
    data class Token(val text: String) : StreamEvent
    data class Completed(
        val totalPromptTokens: Int,
        val totalCompletionTokens: Int,
        val fullContent: String
    ) : StreamEvent
    data class Error(val code: Int?, val message: String) : StreamEvent
}

class SseStreamClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(ChatCompletionRequest::class.java)
    private val chunkAdapter = moshi.adapter(ChatCompletionChunk::class.java)

    fun streamChatCompletion(
        baseUrl: String,
        apiKey: String,
        requestPayload: ChatCompletionRequest
    ): Flow<StreamEvent> = callbackFlow {
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
        val endpoint = if (cleanBaseUrl.endsWith("/chat/completions")) {
            cleanBaseUrl
        } else {
            "$cleanBaseUrl/chat/completions"
        }

        val jsonBody = try {
            requestAdapter.toJson(requestPayload.copy(stream = true))
        } catch (e: Exception) {
            trySend(StreamEvent.Error(null, "Failed to serialize request: ${e.message}"))
            close()
            return@callbackFlow
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Accept", "text/event-stream")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://nexora.ai")
            .addHeader("X-Title", "NEXORA AI Platform")

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
        }

        val call = okHttpClient.newCall(requestBuilder.build())

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (call.isCanceled()) {
                    close()
                    return
                }
                val msg = when {
                    e.message?.contains("Failed to connect") == true ->
                        "Connection refused. If using local Ollama, ensure it is running and accessible (e.g. 10.0.2.2:11434)."
                    e.message?.contains("timeout") == true ->
                        "Request timed out. Provider server took too long to respond."
                    else -> "Network failure: ${e.localizedMessage ?: "Unknown network error"}"
                }
                trySend(StreamEvent.Error(null, msg))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        val errorBody = res.body?.string().orEmpty()
                        val parsedMessage = parseErrorMessage(res.code, errorBody)
                        trySend(StreamEvent.Error(res.code, parsedMessage))
                        close()
                        return
                    }

                    val body = res.body
                    if (body == null) {
                        trySend(StreamEvent.Error(res.code, "Empty response body from provider"))
                        close()
                        return
                    }

                    var fullContent = StringBuilder()
                    var promptTokens = requestPayload.messages.sumOf { it.content.length / 4 }
                    var completionTokens = 0

                    try {
                        val reader = BufferedReader(InputStreamReader(body.byteStream()))
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            val trimmed = line?.trim().orEmpty()
                            if (trimmed.isEmpty() || trimmed.startsWith(":")) {
                                continue // keep-alive comment
                            }

                            if (trimmed.startsWith("data:")) {
                                val dataContent = trimmed.substring(5).trim()
                                if (dataContent == "[DONE]") {
                                    break
                                }

                                try {
                                    val chunk = chunkAdapter.fromJson(dataContent)
                                    val delta = chunk?.choices?.firstOrNull()?.delta?.content
                                    if (!delta.isNullOrEmpty()) {
                                        fullContent.append(delta)
                                        completionTokens += (delta.length / 4).coerceAtLeast(1)
                                        trySend(StreamEvent.Token(delta))
                                    }

                                    // If usage was returned in the chunk
                                    chunk?.usage?.let { u ->
                                        u.promptTokens?.let { promptTokens = it }
                                        u.completionTokens?.let { completionTokens = it }
                                    }
                                } catch (e: Exception) {
                                    // Non-fatal chunk parse fallback
                                }
                            }
                        }

                        trySend(
                            StreamEvent.Completed(
                                totalPromptTokens = promptTokens.coerceAtLeast(1),
                                totalCompletionTokens = completionTokens.coerceAtLeast(1),
                                fullContent = fullContent.toString()
                            )
                        )
                    } catch (e: Exception) {
                        if (call.isCanceled()) {
                            // User stopped streaming deliberately
                            trySend(
                                StreamEvent.Completed(
                                    totalPromptTokens = promptTokens,
                                    totalCompletionTokens = completionTokens,
                                    fullContent = fullContent.toString()
                                )
                            )
                        } else {
                            trySend(StreamEvent.Error(null, "Stream interrupted: ${e.message}"))
                        }
                    } finally {
                        close()
                    }
                }
            }
        })

        awaitClose {
            if (!call.isCanceled()) {
                call.cancel()
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun testConnection(
        baseUrl: String,
        apiKey: String,
        modelToTest: String
    ): ConnectionTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")

        // Step 1: Attempt /models check or lightweight completion
        val endpoint = if (cleanBaseUrl.endsWith("/chat/completions")) {
            cleanBaseUrl
        } else {
            "$cleanBaseUrl/chat/completions"
        }

        val testPayload = ChatCompletionRequest(
            model = modelToTest.ifBlank { "default" },
            messages = listOf(OpenAiMessage(role = "user", content = "ping")),
            maxTokens = 1,
            stream = false
        )

        val jsonBody = try {
            requestAdapter.toJson(testPayload)
        } catch (e: Exception) {
            return@withContext ConnectionTestResult(
                isSuccess = false,
                latencyMs = 0,
                message = "Failed to create test request: ${e.message}"
            )
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://nexora.ai")

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
        }

        try {
            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            response.use { res ->
                if (res.isSuccessful) {
                    ConnectionTestResult(
                        isSuccess = true,
                        latencyMs = latency,
                        message = "Connected successfully (${latency}ms)! Ready to generate."
                    )
                } else {
                    val errorBody = res.body?.string().orEmpty()
                    val errorMsg = parseErrorMessage(res.code, errorBody)
                    ConnectionTestResult(
                        isSuccess = false,
                        latencyMs = latency,
                        message = "Connection test failed [HTTP ${res.code}]: $errorMsg"
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                isSuccess = false,
                latencyMs = latency,
                message = "Host unreachable or error: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    private fun parseErrorMessage(code: Int, rawBody: String): String {
        return try {
            val json = JSONObject(rawBody)
            if (json.has("error")) {
                val errorObj = json.optJSONObject("error")
                val msg = errorObj?.optString("message") ?: json.optString("error")
                if (msg.isNotBlank()) return msg
            }
            if (json.has("message")) {
                return json.getString("message")
            }
            when (code) {
                401 -> "Unauthorized: API Key is invalid or missing."
                429 -> "Rate Limit Exceeded: Quota exhausted or too many requests."
                404 -> "Not Found: Model or API endpoint does not exist."
                500, 502, 503 -> "Server error from provider. Try again in a moment."
                else -> "HTTP $code: $rawBody"
            }
        } catch (_: Exception) {
            when (code) {
                401 -> "Unauthorized: API Key is invalid or missing."
                429 -> "Rate Limit Exceeded: Quota exhausted or too many requests."
                404 -> "Not Found: Model or API endpoint does not exist."
                else -> if (rawBody.isNotBlank()) rawBody else "HTTP Error $code"
            }
        }
    }
}
