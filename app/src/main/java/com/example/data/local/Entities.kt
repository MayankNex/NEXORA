package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class ProviderConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val defaultModel: String,
    val availableModels: String,
    val isActive: Boolean = false,
    val isCustom: Boolean = false,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val lastTestedLatencyMs: Long? = null,
    val lastTestedStatus: String? = null
)

@Entity(tableName = "personas")
data class PersonaPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagline: String,
    val systemPrompt: String,
    val defaultTemperature: Float = 0.7f,
    val badgeColorHex: String = "#8B5CF6",
    val iconName: String = "auto_awesome",
    val isDefault: Boolean = false
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val providerId: String,
    val modelId: String,
    val personaId: String,
    val isPinned: Boolean = false,
    val totalPromptTokens: Int = 0,
    val totalCompletionTokens: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val modelUsed: String? = null,
    val isError: Boolean = false
)
