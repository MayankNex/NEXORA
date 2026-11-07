package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.PersonaPresetEntity
import com.example.data.local.ProviderConfigEntity
import com.example.data.local.TokenStats
import com.example.data.repository.ChatRepository
import com.example.data.remote.StreamEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val currentSessionId: String? = null,
    val currentSession: ChatSessionEntity? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isStreaming: Boolean = false,
    val streamingBuffer: String = "",
    val activePersona: PersonaPresetEntity? = null,
    val activeProvider: ProviderConfigEntity? = null,
    val activeModelId: String = "",
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProviders: StateFlow<List<ProviderConfigEntity>> = repository.getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPersonas: StateFlow<List<PersonaPresetEntity>> = repository.getAllPersonas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTokenStats: StateFlow<TokenStats> = repository.getTotalTokenStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TokenStats(0, 0))

    private var activeStreamJob: Job? = null

    init {
        // Observe sessions and automatically select or create the first one
        viewModelScope.launch {
            repository.getAllSessions().collect { sessions ->
                if (_uiState.value.currentSessionId == null) {
                    if (sessions.isNotEmpty()) {
                        selectSession(sessions.first().id)
                    } else {
                        // Create initial session
                        val newId = repository.createNewSession(
                            title = "Welcome to NEXORA AI"
                        )
                        selectSession(newId)
                    }
                }
            }
        }

        // Observe active provider
        viewModelScope.launch {
            repository.getActiveProviderFlow().collect { provider ->
                _uiState.update { current ->
                    current.copy(
                        activeProvider = provider,
                        activeModelId = if (current.activeModelId.isBlank()) provider?.defaultModel.orEmpty() else current.activeModelId
                    )
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        activeStreamJob?.cancel()
        _uiState.update { it.copy(currentSessionId = sessionId, isStreaming = false, streamingBuffer = "", errorMessage = null) }

        viewModelScope.launch {
            val session = repository.getSession(sessionId)
            val persona = session?.let { s ->
                allPersonas.value.firstOrNull { it.id == s.personaId }
            }
            _uiState.update {
                it.copy(
                    currentSession = session,
                    activePersona = persona,
                    activeModelId = session?.modelId.orEmpty().ifBlank { it.activeProvider?.defaultModel.orEmpty() }
                )
            }
        }

        // Collect messages for this session
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun startNewSession(personaId: String? = null, modelId: String? = null) {
        viewModelScope.launch {
            val id = repository.createNewSession(
                title = "New Intelligence Thread",
                providerId = _uiState.value.activeProvider?.id,
                modelId = modelId ?: _uiState.value.activeModelId,
                personaId = personaId ?: _uiState.value.activePersona?.id
            )
            selectSession(id)
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val sessionId = _uiState.value.currentSessionId ?: return
        val activeProvider = _uiState.value.activeProvider

        if (activeProvider == null) {
            _uiState.update { it.copy(errorMessage = "Please configure an active AI provider in Settings first.") }
            return
        }

        if (activeProvider.apiKey.isBlank() && !activeProvider.baseUrl.contains("10.0.2.2") && !activeProvider.baseUrl.contains("localhost") && !activeProvider.baseUrl.contains("127.0.0.1")) {
            _uiState.update { it.copy(errorMessage = "API key is required for ${activeProvider.name}. Tap Settings to enter your key.") }
            return
        }

        _uiState.update { it.copy(isStreaming = true, streamingBuffer = "", errorMessage = null) }

        activeStreamJob?.cancel()
        activeStreamJob = viewModelScope.launch {
            repository.sendChatMessageStream(
                sessionId = sessionId,
                userMessageContent = trimmed,
                personaOverrideId = _uiState.value.activePersona?.id,
                modelOverrideId = _uiState.value.activeModelId
            ).collect { event ->
                when (event) {
                    is StreamEvent.Token -> {
                        _uiState.update { it.copy(streamingBuffer = it.streamingBuffer + event.text) }
                    }
                    is StreamEvent.Completed -> {
                        _uiState.update { it.copy(isStreaming = false, streamingBuffer = "") }
                    }
                    is StreamEvent.Error -> {
                        _uiState.update {
                            it.copy(
                                isStreaming = false,
                                streamingBuffer = "",
                                errorMessage = event.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun stopStreaming() {
        activeStreamJob?.cancel()
        activeStreamJob = null
        _uiState.update { it.copy(isStreaming = false, streamingBuffer = "") }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun clearCurrentChat() {
        val sid = _uiState.value.currentSessionId ?: return
        viewModelScope.launch {
            repository.clearSessionMessages(sid)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun togglePinSession(sessionId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId, !isPinned)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                val remaining = allSessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().id)
                } else {
                    startNewSession()
                }
            }
        }
    }

    fun selectPersona(persona: PersonaPresetEntity) {
        _uiState.update { it.copy(activePersona = persona) }
    }

    fun selectModel(modelId: String) {
        _uiState.update { it.copy(activeModelId = modelId) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun exportCurrentSession(context: Context) {
        val sid = _uiState.value.currentSessionId ?: return
        viewModelScope.launch {
            val mdContent = repository.exportSessionAsMarkdown(sid)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, mdContent)
                putExtra(Intent.EXTRA_TITLE, "NEXORA AI Conversation Export")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Export NEXORA Conversation")
            context.startActivity(shareIntent)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
