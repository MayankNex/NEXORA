package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TokenStats(
    val totalPrompt: Int,
    val totalCompletion: Int
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_sessions SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSessionTitle(id: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Transaction
    suspend fun deleteSessionWithMessages(sessionId: String) {
        deleteMessagesForSession(sessionId)
        deleteSession(sessionId)
    }

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesListForSession(sessionId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT COALESCE(SUM(totalPromptTokens), 0) AS totalPrompt, COALESCE(SUM(totalCompletionTokens), 0) AS totalCompletion FROM chat_sessions")
    fun getTotalTokenStats(): Flow<TokenStats>

    @Query("DELETE FROM chat_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY isCustom ASC, name ASC")
    fun getAllProviders(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM providers WHERE isActive = 1 LIMIT 1")
    fun getActiveProviderFlow(): Flow<ProviderConfigEntity?>

    @Query("SELECT * FROM providers WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProvider(): ProviderConfigEntity?

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: String): ProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderConfigEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultProviders(providers: List<ProviderConfigEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderConfigEntity)

    @Query("UPDATE providers SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE providers SET isActive = 1 WHERE id = :id")
    suspend fun activateProvider(id: String)

    @Transaction
    suspend fun setActiveProvider(id: String) {
        deactivateAll()
        activateProvider(id)
    }

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProvider(id: String)

    @Query("DELETE FROM providers")
    suspend fun clearAllProviders()
}

@Dao
interface PersonaDao {
    @Query("SELECT * FROM personas ORDER BY isDefault DESC, name ASC")
    fun getAllPersonas(): Flow<List<PersonaPresetEntity>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaById(id: String): PersonaPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaPresetEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultPersonas(personas: List<PersonaPresetEntity>)

    @Update
    suspend fun updatePersona(persona: PersonaPresetEntity)

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun deletePersona(id: String)

    @Query("DELETE FROM personas")
    suspend fun clearAllPersonas()
}
