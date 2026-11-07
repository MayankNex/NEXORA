package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.NexoraDatabase
import com.example.data.local.PersonaPresetEntity
import com.example.data.local.ProviderConfigEntity
import com.example.data.local.TokenStats
import com.example.data.remote.ChatCompletionRequest
import com.example.data.remote.ConnectionTestResult
import com.example.data.remote.OpenAiMessage
import com.example.data.remote.SseStreamClient
import com.example.data.remote.StreamEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val database: NexoraDatabase,
    private val sseClient: SseStreamClient = SseStreamClient()
) {
    private val chatDao = database.chatDao()
    private val providerDao = database.providerDao()
    private val personaDao = database.personaDao()

    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    fun getAllProviders(): Flow<List<ProviderConfigEntity>> = providerDao.getAllProviders()

    fun getActiveProviderFlow(): Flow<ProviderConfigEntity?> = providerDao.getActiveProviderFlow()

    fun getAllPersonas(): Flow<List<PersonaPresetEntity>> = personaDao.getAllPersonas()

    fun getTotalTokenStats(): Flow<TokenStats> = chatDao.getTotalTokenStats()

    suspend fun createNewSession(
        title: String? = null,
        providerId: String? = null,
        modelId: String? = null,
        personaId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val activeProvider = providerDao.getActiveProvider()
        val defaultPersona = personaDao.getAllPersonas().firstOrNull()?.firstOrNull { it.isDefault }

        val actualProviderId = providerId ?: activeProvider?.id ?: "groq"
        val actualModelId = modelId ?: activeProvider?.defaultModel ?: "llama-3.3-70b-versatile"
        val actualPersonaId = personaId ?: defaultPersona?.id ?: "nexora_prime"

        val sessionId = UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = sessionId,
            title = title ?: "New Intelligence Thread",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            providerId = actualProviderId,
            modelId = actualModelId,
            personaId = actualPersonaId,
            isPinned = false
        )
        chatDao.insertSession(session)
        sessionId
    }

    suspend fun getSession(sessionId: String): ChatSessionEntity? = withContext(Dispatchers.IO) {
        chatDao.getSessionById(sessionId)
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) = withContext(Dispatchers.IO) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    suspend fun togglePinSession(sessionId: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setPinned(sessionId, isPinned)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteSessionWithMessages(sessionId)
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun clearSessionMessages(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun setActiveProvider(providerId: String) = withContext(Dispatchers.IO) {
        providerDao.setActiveProvider(providerId)
    }

    suspend fun saveProvider(provider: ProviderConfigEntity) = withContext(Dispatchers.IO) {
        providerDao.insertProvider(provider)
    }

    suspend fun deleteProvider(providerId: String) = withContext(Dispatchers.IO) {
        providerDao.deleteProvider(providerId)
    }

    suspend fun testProvider(
        baseUrl: String,
        apiKey: String,
        modelId: String
    ): ConnectionTestResult = withContext(Dispatchers.IO) {
        sseClient.testConnection(baseUrl, apiKey, modelId)
    }

    suspend fun savePersona(persona: PersonaPresetEntity) = withContext(Dispatchers.IO) {
        personaDao.insertPersona(persona)
    }

    suspend fun deletePersona(personaId: String) = withContext(Dispatchers.IO) {
        personaDao.deletePersona(personaId)
    }

    suspend fun wipeAllUserData() = withContext(Dispatchers.IO) {
        chatDao.clearAllMessages()
        chatDao.clearAllSessions()
        providerDao.clearAllProviders()
        personaDao.clearAllPersonas()
        NexoraDatabase.seedDefaults(database)
    }

    fun sendChatMessageStream(
        sessionId: String,
        userMessageContent: String,
        personaOverrideId: String? = null,
        modelOverrideId: String? = null
    ): Flow<StreamEvent> = flow {
        // 1. Fetch current session and conversation history
        val session = chatDao.getSessionById(sessionId)
            ?: run {
                emit(StreamEvent.Error(404, "Session not found"))
                return@flow
            }

        val provider = providerDao.getActiveProvider()
            ?: run {
                emit(StreamEvent.Error(400, "No active AI provider configured. Please configure an API provider in Settings."))
                return@flow
            }

        val personaId = personaOverrideId ?: session.personaId
        val persona = personaDao.getPersonaById(personaId)

        // Insert User message
        val userMsgId = UUID.randomUUID().toString()
        val userMessage = ChatMessageEntity(
            id = userMsgId,
            sessionId = sessionId,
            role = "user",
            content = userMessageContent,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMessage)

        // Update session title if first message
        val existingMessages = chatDao.getMessagesListForSession(sessionId)
        if (existingMessages.size <= 1) {
            val autoTitle = if (userMessageContent.length > 30) {
                userMessageContent.take(27) + "..."
            } else {
                userMessageContent
            }
            chatDao.updateSessionTitle(sessionId, autoTitle)
        }

        // Build OpenAI payload
        val openAiMessages = mutableListOf<OpenAiMessage>()

        // System prompt from Persona
        val systemPrompt = persona?.systemPrompt
            ?: "You are NEXORA AI, an unbiased and versatile AI assistant."
        openAiMessages.add(OpenAiMessage(role = "system", content = systemPrompt))

        // Append historical messages (up to last 15 for context)
        val historyToInclude = existingMessages.takeLast(15)
        for (msg in historyToInclude) {
            openAiMessages.add(OpenAiMessage(role = msg.role, content = msg.content))
        }

        val modelToUse = modelOverrideId ?: session.modelId.ifBlank { provider.defaultModel }
        val request = ChatCompletionRequest(
            model = modelToUse,
            messages = openAiMessages,
            temperature = persona?.defaultTemperature ?: provider.temperature,
            maxTokens = provider.maxTokens,
            stream = true
        )

        // Prepare placeholder Assistant message
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantMessage = ChatMessageEntity(
            id = assistantMsgId,
            sessionId = sessionId,
            role = "assistant",
            content = "",
            timestamp = System.currentTimeMillis() + 1,
            modelUsed = modelToUse
        )
        chatDao.insertMessage(assistantMessage)

        val accumulatedContent = StringBuilder()

        try {
            sseClient.streamChatCompletion(
                baseUrl = provider.baseUrl,
                apiKey = provider.apiKey,
                requestPayload = request
            ).collect { event ->
                when (event) {
                    is StreamEvent.Token -> {
                        accumulatedContent.append(event.text)
                        emit(event)
                    }
                    is StreamEvent.Completed -> {
                        // Persist final completion
                        val finalMsg = assistantMessage.copy(
                            content = event.fullContent.ifBlank { accumulatedContent.toString() },
                            promptTokens = event.totalPromptTokens,
                            completionTokens = event.totalCompletionTokens
                        )
                        chatDao.updateMessage(finalMsg)

                        // Update session token counts and timestamp
                        val updatedSession = session.copy(
                            updatedAt = System.currentTimeMillis(),
                            totalPromptTokens = session.totalPromptTokens + event.totalPromptTokens,
                            totalCompletionTokens = session.totalCompletionTokens + event.totalCompletionTokens
                        )
                        chatDao.updateSession(updatedSession)

                        emit(event)
                    }
                    is StreamEvent.Error -> {
                        // Mark message as error
                        val errorMsg = assistantMessage.copy(
                            content = "⚠️ ${event.message}",
                            isError = true
                        )
                        chatDao.updateMessage(errorMsg)
                        emit(event)
                    }
                }
            }
        } catch (e: Exception) {
            val errorMsg = assistantMessage.copy(
                content = "⚠️ Request interrupted: ${e.message}",
                isError = true
            )
            chatDao.updateMessage(errorMsg)
            emit(StreamEvent.Error(null, e.localizedMessage ?: "Stream error"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun exportSessionAsMarkdown(sessionId: String): String = withContext(Dispatchers.IO) {
        val session = chatDao.getSessionById(sessionId) ?: return@withContext "Session not found."
        val messages = chatDao.getMessagesListForSession(sessionId)
        val sb = StringBuilder()
        sb.append("# ${session.title}\n\n")
        sb.append("> Exported from NEXORA AI — Model: ${session.modelId} | Tokens: ${session.totalPromptTokens + session.totalCompletionTokens}\n\n---\n\n")
        for (m in messages) {
            val roleName = if (m.role == "user") "### 👤 You" else "### ⚡ NEXORA AI (${m.modelUsed ?: "Assistant"})"
            sb.append("$roleName\n\n${m.content}\n\n---\n\n")
        }
        sb.toString()
    }
}
