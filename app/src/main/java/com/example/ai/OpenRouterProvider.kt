package com.example.ai

import android.util.Log
import com.example.data.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Adapter implementation for OpenRouter (https://openrouter.ai).
 * Provides unified access to hundreds of open & proprietary models (Claude 3.5, Llama 3.3, DeepSeek, Mistral, GPT-4o).
 */
class OpenRouterProvider(
    private val keyStorage: SecureKeyStorage,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    override val providerId: String = ProviderType.OPENROUTER.id
    override val displayName: String = ProviderType.OPENROUTER.displayName

    companion object {
        private const val TAG = "OpenRouterProvider"
        private val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val SUPPORTED_MODELS = listOf(
            AiModelInfo(
                id = "anthropic/claude-3.5-sonnet",
                name = "Claude 3.5 Sonnet",
                description = "Anthropic's state-of-the-art model for nuanced reasoning, executive planning, and high-craft writing.",
                providerId = "openrouter",
                isDefault = true,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 200000,
                    recommendedForFastTasks = false,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "meta-llama/llama-3.3-70b-instruct",
                name = "Llama 3.3 70B Instruct",
                description = "Meta's flagship open-weights model with exceptional planning accuracy and high throughput.",
                providerId = "openrouter",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 128000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "deepseek/deepseek-chat",
                name = "DeepSeek V3",
                description = "High-efficiency, cost-effective reasoning engine optimized for structured logic and coding.",
                providerId = "openrouter",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 64000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "google/gemini-2.0-flash-001",
                name = "Gemini 2.0 Flash (OpenRouter)",
                description = "Google's ultra-fast model routed via OpenRouter high-availability infrastructure.",
                providerId = "openrouter",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 1000000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = false
                )
            ),
            AiModelInfo(
                id = "openai/gpt-4o-mini",
                name = "GPT-4o Mini (OpenRouter)",
                description = "OpenAI's compact, fast model for daily micro-tasks and schedule adjustments.",
                providerId = "openrouter",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 128000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = false
                )
            ),
            AiModelInfo(
                id = "mistralai/mistral-large-2411",
                name = "Mistral Large 2411",
                description = "Mistral's flagship European multilingual model with sharp reasoning and concise style.",
                providerId = "openrouter",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 128000,
                    recommendedForFastTasks = false,
                    recommendedForDeepReasoning = true
                )
            )
        )
    }

    override fun getAvailableModels(): List<AiModelInfo> = SUPPORTED_MODELS

    override fun getCapabilities(model: String?): AiCapabilities {
        val targetId = model ?: keyStorage.getActiveModel(providerId)
        return SUPPORTED_MODELS.find { it.id == targetId }?.capabilities ?: AiCapabilities()
    }

    override suspend fun testConnection(model: String?): ConnectionTestResult = withContext(Dispatchers.IO) {
        val targetModel = model ?: keyStorage.getActiveModel(providerId)
        val apiKey = keyStorage.getApiKey(providerId)

        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(
                isSuccess = false,
                message = "OpenRouter API Key is missing. Please set your key starting with 'sk-or-...' in settings.",
                modelTested = targetModel,
                providerTested = displayName
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val response = generateText(
                prompt = "Reply with 'LifeOS OpenRouter Online' and nothing else.",
                systemInstruction = "You are a fast ping test assistant.",
                model = targetModel
            )
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccess) {
                ConnectionTestResult(
                    isSuccess = true,
                    latencyMs = latency,
                    message = "Connected to $targetModel (${latency}ms)",
                    modelTested = targetModel,
                    providerTested = displayName
                )
            } else {
                ConnectionTestResult(
                    isSuccess = false,
                    latencyMs = latency,
                    message = response.errorMessage ?: "OpenRouter connection failed",
                    modelTested = targetModel,
                    providerTested = displayName
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                isSuccess = false,
                latencyMs = latency,
                message = "Connection exception: ${e.localizedMessage}",
                modelTested = targetModel,
                providerTested = displayName
            )
        }
    }

    override suspend fun generateText(
        prompt: String,
        systemInstruction: String?,
        model: String?
    ): AiResponse = withContext(Dispatchers.IO) {
        val apiKey = keyStorage.getApiKey(providerId)
        if (apiKey.isBlank()) {
            return@withContext AiResponse.failure(
                errorMessage = "OpenRouter API key is not configured.",
                providerUsed = providerId,
                fallbackText = "Please configure your OpenRouter API key in settings."
            )
        }

        val selectedModel = model ?: keyStorage.getActiveModel(providerId)
        val endpoint = keyStorage.getEndpointUrl(providerId)
        val startTime = System.currentTimeMillis()

        try {
            val messagesArray = JSONArray()
            if (!systemInstruction.isNullOrBlank()) {
                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
            }
            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })

            val requestBodyJson = JSONObject().apply {
                put("model", selectedModel)
                put("messages", messagesArray)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://lifeos.app")
                .addHeader("X-Title", "LifeOS Android")
                .post(requestBodyJson.toString().toRequestBody(MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val bodyStr = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(bodyStr)
                    val choices = jsonObj.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val messageObj = firstChoice.optJSONObject("message")
                        val content = messageObj?.optString("content", "") ?: ""
                        val usage = jsonObj.optJSONObject("usage")
                        val totalTokens = usage?.optInt("total_tokens")

                        return@withContext AiResponse(
                            text = content,
                            isSuccess = true,
                            providerUsed = providerId,
                            modelUsed = selectedModel,
                            latencyMs = latency,
                            tokensUsed = totalTokens
                        )
                    }
                }

                Log.w(TAG, "OpenRouter call failed: code=${response.code}, body=$bodyStr")
                val errorDetails = try {
                    val errJson = JSONObject(bodyStr).optJSONObject("error")
                    errJson?.optString("message", bodyStr) ?: bodyStr
                } catch (e: Exception) {
                    bodyStr
                }

                return@withContext AiResponse.failure(
                    errorMessage = "HTTP ${response.code}: $errorDetails",
                    providerUsed = providerId,
                    modelUsed = selectedModel
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "OpenRouter exception", e)
            return@withContext AiResponse.failure(
                errorMessage = "Network error: ${e.localizedMessage}",
                providerUsed = providerId,
                modelUsed = selectedModel
            )
        }
    }

    override suspend fun generateStructuredJson(
        prompt: String,
        systemInstruction: String?,
        schemaHint: String?,
        model: String?
    ): AiResponse = withContext(Dispatchers.IO) {
        val enhancedPrompt = if (!schemaHint.isNullOrBlank()) {
            """
            $prompt
            
            OUTPUT INSTRUCTIONS:
            Adhere strictly to this JSON schema:
            $schemaHint
            
            Return ONLY pure valid JSON without markdown fences (no ```json or ```).
            """.trimIndent()
        } else {
            """
            $prompt
            
            OUTPUT INSTRUCTIONS:
            Return ONLY pure valid JSON without markdown fences or additional explanation.
            """.trimIndent()
        }

        val rawResponse = generateText(enhancedPrompt, systemInstruction, model)
        if (!rawResponse.isSuccess) return@withContext rawResponse

        val cleaned = cleanJsonFences(rawResponse.text)
        rawResponse.copy(text = cleaned)
    }

    override suspend fun generateChat(
        messages: List<ChatMessageEntity>,
        systemInstruction: String?,
        model: String?
    ): AiResponse = withContext(Dispatchers.IO) {
        val apiKey = keyStorage.getApiKey(providerId)
        if (apiKey.isBlank()) {
            return@withContext AiResponse.failure(
                errorMessage = "OpenRouter API key is not configured.",
                providerUsed = providerId,
                fallbackText = "Please enter your OpenRouter API key in settings."
            )
        }

        val selectedModel = model ?: keyStorage.getActiveModel(providerId)
        val endpoint = keyStorage.getEndpointUrl(providerId)
        val startTime = System.currentTimeMillis()

        try {
            val messagesArray = JSONArray()
            if (!systemInstruction.isNullOrBlank()) {
                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
            }

            for (msg in messages) {
                val role = if (msg.role == "user") "user" else "assistant"
                messagesArray.put(JSONObject().apply {
                    put("role", role)
                    put("content", msg.text)
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("model", selectedModel)
                put("messages", messagesArray)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://lifeos.app")
                .addHeader("X-Title", "LifeOS Android")
                .post(requestBodyJson.toString().toRequestBody(MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val bodyStr = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(bodyStr)
                    val choices = jsonObj.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val messageObj = firstChoice.optJSONObject("message")
                        val content = messageObj?.optString("content", "") ?: ""

                        return@withContext AiResponse.success(
                            text = content,
                            providerUsed = providerId,
                            modelUsed = selectedModel,
                            latencyMs = latency
                        )
                    }
                }

                return@withContext AiResponse.failure(
                    errorMessage = "HTTP ${response.code}: $bodyStr",
                    providerUsed = providerId,
                    modelUsed = selectedModel
                )
            }
        } catch (e: Exception) {
            AiResponse.failure(
                errorMessage = "Chat exception: ${e.localizedMessage}",
                providerUsed = providerId,
                modelUsed = selectedModel
            )
        }
    }

    private fun cleanJsonFences(raw: String): String {
        var result = raw.trim()
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json").trim()
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```").trim()
        }
        if (result.endsWith("```")) {
            result = result.removeSuffix("```").trim()
        }
        return result
    }
}
