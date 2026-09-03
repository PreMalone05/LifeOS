package com.example.ai

import com.example.data.ChatMessageEntity

/**
 * Common data structures and interface for Multi-Provider AI Architecture.
 * Decouples LifeOS from specific vendor implementations (Gemini, OpenRouter, OpenAI, Local LLMs).
 */

enum class ProviderType(val id: String, val displayName: String, val defaultEndpoint: String) {
    GEMINI("gemini", "Google Gemini", "https://generativelanguage.googleapis.com"),
    OPENROUTER("openrouter", "OpenRouter", "https://openrouter.ai/api/v1/chat/completions"),
    OPENAI("openai", "OpenAI Compatible", "https://api.openai.com/v1/chat/completions"),
    CUSTOM("custom", "Custom / Local Endpoint", "http://localhost:11434/v1/chat/completions");

    companion object {
        fun fromId(id: String): ProviderType = values().find { it.id.equals(id, ignoreCase = true) } ?: GEMINI
    }
}

data class AiCapabilities(
    val supportsSystemInstruction: Boolean = true,
    val supportsStructuredJson: Boolean = true,
    val supportsStreaming: Boolean = false,
    val contextWindowTokens: Int = 128000,
    val recommendedForFastTasks: Boolean = true,
    val recommendedForDeepReasoning: Boolean = false
)

data class AiModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val providerId: String,
    val isDefault: Boolean = false,
    val capabilities: AiCapabilities = AiCapabilities()
)

data class AiResponse(
    val text: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val providerUsed: String = "",
    val modelUsed: String = "",
    val latencyMs: Long = 0,
    val tokensUsed: Int? = null
) {
    companion object {
        fun success(text: String, providerUsed: String, modelUsed: String, latencyMs: Long = 0) = AiResponse(
            text = text,
            isSuccess = true,
            providerUsed = providerUsed,
            modelUsed = modelUsed,
            latencyMs = latencyMs
        )

        fun failure(errorMessage: String, providerUsed: String = "", modelUsed: String = "", fallbackText: String = "") = AiResponse(
            text = fallbackText,
            isSuccess = false,
            errorMessage = errorMessage,
            providerUsed = providerUsed,
            modelUsed = modelUsed
        )
    }
}

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val latencyMs: Long = 0,
    val message: String,
    val modelTested: String = "",
    val providerTested: String = ""
)

interface AiProvider {
    val providerId: String
    val displayName: String

    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        model: String? = null
    ): AiResponse

    suspend fun generateStructuredJson(
        prompt: String,
        systemInstruction: String? = null,
        schemaHint: String? = null,
        model: String? = null
    ): AiResponse

    suspend fun generateChat(
        messages: List<ChatMessageEntity>,
        systemInstruction: String? = null,
        model: String? = null
    ): AiResponse

    suspend fun testConnection(model: String? = null): ConnectionTestResult

    fun getAvailableModels(): List<AiModelInfo>

    fun getCapabilities(model: String? = null): AiCapabilities
}
