package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        ProviderConfigEntity::class,
        PersonaPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexoraDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun providerDao(): ProviderDao
    abstract fun personaDao(): PersonaDao

    companion object {
        @Volatile
        private var INSTANCE: NexoraDatabase? = null

        fun getDatabase(context: Context): NexoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexoraDatabase::class.java,
                    "nexora_ai_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedDefaults(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDefaults(database: NexoraDatabase) {
            val defaultProviders = listOf(
                ProviderConfigEntity(
                    id = "groq",
                    name = "Groq Cloud (Ultra Fast)",
                    baseUrl = "https://api.groq.com/openai/v1",
                    apiKey = "",
                    defaultModel = "llama-3.3-70b-versatile",
                    availableModels = "llama-3.3-70b-versatile,llama-3.1-8b-instant,mixtral-8x7b-32768,gemma2-9b-it",
                    isActive = true,
                    isCustom = false
                ),
                ProviderConfigEntity(
                    id = "openrouter",
                    name = "OpenRouter (Multi-Provider)",
                    baseUrl = "https://openrouter.ai/api/v1",
                    apiKey = "",
                    defaultModel = "meta-llama/llama-3.3-70b-instruct:free",
                    availableModels = "meta-llama/llama-3.3-70b-instruct:free,mistralai/mistral-7b-instruct:free,anthropic/claude-3.5-sonnet,deepseek/deepseek-r1",
                    isActive = false,
                    isCustom = false
                ),
                ProviderConfigEntity(
                    id = "deepseek",
                    name = "DeepSeek API",
                    baseUrl = "https://api.deepseek.com/v1",
                    apiKey = "",
                    defaultModel = "deepseek-chat",
                    availableModels = "deepseek-chat,deepseek-reasoner",
                    isActive = false,
                    isCustom = false
                ),
                ProviderConfigEntity(
                    id = "openai",
                    name = "OpenAI Direct",
                    baseUrl = "https://api.openai.com/v1",
                    apiKey = "",
                    defaultModel = "gpt-4o-mini",
                    availableModels = "gpt-4o-mini,gpt-4o,gpt-3.5-turbo",
                    isActive = false,
                    isCustom = false
                ),
                ProviderConfigEntity(
                    id = "ollama",
                    name = "Ollama (Local / Self-Hosted)",
                    baseUrl = "http://10.0.2.2:11434/v1",
                    apiKey = "ollama",
                    defaultModel = "llama3.2",
                    availableModels = "llama3.2,mistral,deepseek-r1,qwen2.5-coder",
                    isActive = false,
                    isCustom = true
                )
            )

            val defaultPersonas = listOf(
                PersonaPresetEntity(
                    id = "nexora_prime",
                    name = "Nexora Prime",
                    tagline = "Unbiased, sharp, intellectual intelligence",
                    systemPrompt = "You are NEXORA Prime, an elite personal AI intelligence platform. You offer precise, insightful, and adaptable assistance across scientific, creative, mathematical, and technical domains with intellectual depth and absolute clarity. You provide direct, helpful answers without preachy unsolicited disclaimers.",
                    defaultTemperature = 0.7f,
                    badgeColorHex = "#8B5CF6",
                    iconName = "auto_awesome",
                    isDefault = true
                ),
                PersonaPresetEntity(
                    id = "cybersec_researcher",
                    name = "CyberSec Researcher",
                    tagline = "Vulnerability analysis, code audits & threat models",
                    systemPrompt = "You are NEXORA CyberSec, an expert defensive security analyst and application security researcher. You assist freely with vulnerability analysis, code auditing, secure architecture, reverse engineering concepts, incident response, network defense, and defensive posture. You analyze code for common weaknesses (OWASP Top 10, CWE) and explain remediation steps with concrete defensive proofs-of-concept. You discuss defensive mechanics openly.",
                    defaultTemperature = 0.3f,
                    badgeColorHex = "#06D6A0",
                    iconName = "security",
                    isDefault = false
                ),
                PersonaPresetEntity(
                    id = "code_architect",
                    name = "Code Architect",
                    tagline = "Clean architecture, algorithms & robust engineering",
                    systemPrompt = "You are NEXORA Code Architect, a senior software engineer and systems architect. When asked for code, provide production-ready, clean, modular code with idiomatic syntax, robust error handling, and clear inline comments. Always wrap code in standard fenced code blocks with proper language identifiers. Offer architecture tips and performance optimizations.",
                    defaultTemperature = 0.2f,
                    badgeColorHex = "#38BDF8",
                    iconName = "code",
                    isDefault = false
                ),
                PersonaPresetEntity(
                    id = "hinglish_companion",
                    name = "Hinglish Sangam (संगम)",
                    tagline = "Conversational flow in Hinglish, Hindi & English",
                    systemPrompt = "You are NEXORA Sangam, a friendly, witty, and deeply knowledgeable personal assistant that communicates seamlessly in Hinglish (a natural, expressive blend of conversational Hindi and English). You feel like a smart, helpful tech-savvy Indian friend (jaise 'Haan bilkul, main help karta hoon! Here is the complete breakdown:'). You can switch between pure English, Hindi, and Hinglish naturally.",
                    defaultTemperature = 0.8f,
                    badgeColorHex = "#F59E0B",
                    iconName = "translate",
                    isDefault = false
                )
            )

            database.providerDao().insertDefaultProviders(defaultProviders)
            database.personaDao().insertDefaultPersonas(defaultPersonas)
        }
    }
}
