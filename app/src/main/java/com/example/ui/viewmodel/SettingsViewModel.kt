package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PersonaPresetEntity
import com.example.data.local.ProviderConfigEntity
import com.example.data.local.TokenStats
import com.example.data.remote.ConnectionTestResult
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isTesting: Boolean = false,
    val testResult: ConnectionTestResult? = null,
    val testingProviderId: String? = null,
    val isWiping: Boolean = false,
    val wipeCompleted: Boolean = false
)

class SettingsViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val providers: StateFlow<List<ProviderConfigEntity>> = repository.getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personas: StateFlow<List<PersonaPresetEntity>> = repository.getAllPersonas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTokenStats: StateFlow<TokenStats> = repository.getTotalTokenStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TokenStats(0, 0))

    fun setActiveProvider(providerId: String) {
        viewModelScope.launch {
            repository.setActiveProvider(providerId)
        }
    }

    fun saveProvider(provider: ProviderConfigEntity) {
        viewModelScope.launch {
            repository.saveProvider(provider)
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            repository.deleteProvider(providerId)
        }
    }

    fun testConnection(
        providerId: String,
        baseUrl: String,
        apiKey: String,
        modelId: String
    ) {
        _uiState.update { it.copy(isTesting = true, testResult = null, testingProviderId = providerId) }
        viewModelScope.launch {
            val result = repository.testProvider(baseUrl, apiKey, modelId)
            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = result,
                    testingProviderId = providerId
                )
            }
            // Update provider entity with last test result
            val existing = providers.value.firstOrNull { it.id == providerId }
            if (existing != null) {
                val updated = existing.copy(
                    lastTestedLatencyMs = result.latencyMs,
                    lastTestedStatus = if (result.isSuccess) "SUCCESS (${result.latencyMs}ms)" else "FAILED"
                )
                repository.saveProvider(updated)
            }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null, testingProviderId = null) }
    }

    fun savePersona(persona: PersonaPresetEntity) {
        viewModelScope.launch {
            repository.savePersona(persona)
        }
    }

    fun deletePersona(personaId: String) {
        viewModelScope.launch {
            repository.deletePersona(personaId)
        }
    }

    fun wipeAllUserData() {
        _uiState.update { it.copy(isWiping = true) }
        viewModelScope.launch {
            repository.wipeAllUserData()
            _uiState.update { it.copy(isWiping = false, wipeCompleted = true) }
        }
    }

    fun resetWipeCompleted() {
        _uiState.update { it.copy(wipeCompleted = false) }
    }
}
