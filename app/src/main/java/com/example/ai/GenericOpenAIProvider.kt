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
 * Adapter implementation for standard OpenAI API and any OpenAI-compatible custom/local endpoints
 * (e.g. Ollama, LM Studio, vLLM, LocalAI).
 */
class GenericOpenAIProvider(
    private val keyStorage: SecureKeyStorage,
    override val providerId: String = ProviderType.OPENAI.id,
    override val displayName: String = ProviderType.OPENAI.displayName,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    companion object {
        private const val TAG = "GenericOpenAIProvider"
        private val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val OPENAI_MODELS = listOf(
            AiModelInfo(
                id = "gpt-4o-mini",
                name = "GPT-4o Mini",
                description = "Fast, lightweight model for day-to-day productivity and micro-nudges.",
                providerId = "openai",
                isDefault = true,
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
                id = "gpt-4o",
                name = "GPT-4o",
                description = "Flagship high-intelligence multimodal model for strategic life planning.",
                providerId = "openai",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 128000,
                    recommendedForFastTasks = false,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "o3-mini",
                name = "o3-mini",
                description = "High-reasoning STEM and logical decomposition engine.",
                providerId = "openai",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = false,
                    contextWindowTokens = 128000,
                    recommendedForFastTasks = false,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "gpt-4-turbo",
                name = "GPT-4 Turbo",
                description = "Proven high-capacity production reasoning model.",
                providerId = "openai",
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

        val CUSTOM_ENDPOINT_MODELS = listOf(
            AiModelInfo(
                id = "llama3",
                name = "Llama 3 (Local)",
                description = "Self-hosted local Llama model via Ollama / LM Studio.",
                providerId = "custom",
                isDefault = true,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 32000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "mistral",
                name = "Mistral (Local)",
                description = "Local lightweight Mistral instance.",
                providerId = "custom",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 32000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = false
                )
            ),
            AiModelInfo(
                id = "qwen2.5-coder",
                name = "Qwen 2.5 (Local)",
                description = "Fast local reasoning & structured logic model.",
                providerId = "custom",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 32000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "custom-model",
                name = "Custom Model ID",
                description = "Custom model identifier configured on your local or remote server.",
                providerId = "custom",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = true,
                    contextWindowTokens = 64000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = true
                )
            )
        )
    }

    override fun getAvailableModels(): List<AiModelInfo> {
        return if (providerId == ProviderType.CUSTOM.id) CUSTOM_ENDPOINT_MODELS else OPENAI_MODELS
    }

    override fun getCapabilities(model: String?): AiCapabilities {
        val targetId = model ?: keyStorage.getActiveModel(providerId)
        return getAvailableModels().find { it.id == targetId }?.capabilities ?: AiCapabilities()
    }

    override suspend fun testConnection(model: String?): ConnectionTestResult = withContext(Dispatchers.IO) {
        val targetModel = model ?: keyStorage.getActiveModel(providerId)
        val apiKey = keyStorage.getApiKey(providerId)
        val isCustom = providerId == ProviderType.CUSTOM.id

        if (apiKey.isBlank() && !isCustom) {
            return@withContext ConnectionTestResult(
                isSuccess = false,
                message = "API Key is required for OpenAI. Please enter your API key in settings.",
                modelTested = targetModel,
                providerTested = displayName
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val response = generateText(
                prompt = "Reply with 'LifeOS OpenAI Online' and nothing else.",
                systemInstruction = "You are a connectivity check agent.",
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
                    message = response.errorMessage ?: "Connection failed",
                    modelTested = targetModel,
                    providerTested = displayName
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                isSuccess = false,
                latencyMs = latency,
                message = "Exception: ${e.localizedMessage}",
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
        val isCustom = providerId == ProviderType.CUSTOM.id

        if (apiKey.isBlank() && !isCustom) {
            return@withContext AiResponse.failure(
                errorMessage = "OpenAI API key is missing.",
                providerUsed = providerId,
                fallbackText = "Please enter your OpenAI API key in settings."
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

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody(MEDIA_TYPE))

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val request = requestBuilder.build()

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

                Log.w(TAG, "OpenAI call failed: code=${response.code}, body=$bodyStr")
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
            Log.e(TAG, "OpenAI exception", e)
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
            
            Return ONLY raw valid JSON without markdown fences (no ```json or ```).
            """.trimIndent()
        } else {
            """
            $prompt
            
            OUTPUT INSTRUCTIONS:
            Return ONLY raw valid JSON without markdown codeblocks or conversational preamble.
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
        val isCustom = providerId == ProviderType.CUSTOM.id

        if (apiKey.isBlank() && !isCustom) {
            return@withContext AiResponse.failure(
                errorMessage = "OpenAI API key missing.",
                providerUsed = providerId,
                fallbackText = "Please enter your OpenAI API key in settings."
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

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody(MEDIA_TYPE))

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val request = requestBuilder.build()

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
