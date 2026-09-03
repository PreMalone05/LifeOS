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
 * Adapter implementation for Google Gemini models.
 */
class GeminiProvider(
    private val keyStorage: SecureKeyStorage,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    override val providerId: String = ProviderType.GEMINI.id
    override val displayName: String = ProviderType.GEMINI.displayName

    companion object {
        private const val TAG = "GeminiProvider"
        private val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        
        val SUPPORTED_MODELS = listOf(
            AiModelInfo(
                id = "gemini-3.5-flash",
                name = "Gemini 3.5 Flash",
                description = "Google's ultra-fast, intelligent default model for real-time planning and coaching.",
                providerId = "gemini",
                isDefault = true,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = false,
                    contextWindowTokens = 1000000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = false
                )
            ),
            AiModelInfo(
                id = "gemini-3.1-pro-preview",
                name = "Gemini 3.1 Pro",
                description = "Google's premier deep-reasoning model for complex multidimensional analysis.",
                providerId = "gemini",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = false,
                    contextWindowTokens = 2000000,
                    recommendedForFastTasks = false,
                    recommendedForDeepReasoning = true
                )
            ),
            AiModelInfo(
                id = "gemini-3.1-flash-lite-preview",
                name = "Gemini 3.1 Flash-Lite",
                description = "Ultra-low latency model engineered for instant nudges and encouragement.",
                providerId = "gemini",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = false,
                    contextWindowTokens = 1000000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = false
                )
            ),
            AiModelInfo(
                id = "gemini-flash-latest",
                name = "Gemini Flash Latest",
                description = "Latest stable Gemini Flash build for consistent fallback response.",
                providerId = "gemini",
                isDefault = false,
                capabilities = AiCapabilities(
                    supportsSystemInstruction = true,
                    supportsStructuredJson = true,
                    supportsStreaming = false,
                    contextWindowTokens = 1000000,
                    recommendedForFastTasks = true,
                    recommendedForDeepReasoning = false
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
                message = "API Key is missing for Google Gemini. Please configure your key in settings.",
                modelTested = targetModel,
                providerTested = displayName
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val response = generateText(
                prompt = "Respond with 'LifeOS Gemini Online' and nothing else.",
                systemInstruction = "You are a connectivity tester.",
                model = targetModel
            )
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccess) {
                ConnectionTestResult(
                    isSuccess = true,
                    latencyMs = latency,
                    message = "Connected successfully to $targetModel (${latency}ms)",
                    modelTested = targetModel,
                    providerTested = displayName
                )
            } else {
                ConnectionTestResult(
                    isSuccess = false,
                    latencyMs = latency,
                    message = response.errorMessage ?: "Failed to connect",
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
                errorMessage = "Google Gemini API key not configured.",
                providerUsed = providerId,
                fallbackText = "API key missing. Please enter your Gemini API key in settings."
            )
        }

        val selectedModel = model ?: keyStorage.getActiveModel(providerId)
        val candidateModels = listOf(
            selectedModel,
            "gemini-3.5-flash",
            "gemini-flash-latest",
            "gemini-3.1-flash-lite-preview"
        ).distinct()

        var lastError = ""
        val startTime = System.currentTimeMillis()

        for (candidate in candidateModels) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$candidate:generateContent?key=$apiKey"
            try {
                val requestBodyJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val partObj = JSONObject().apply {
                                    put("text", prompt)
                                }
                                put(partObj)
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)

                    if (!systemInstruction.isNullOrBlank()) {
                        val sysInstObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val partObj = JSONObject().apply {
                                    put("text", systemInstruction)
                                }
                                put(partObj)
                            }
                            put("parts", partsArray)
                        }
                        put("systemInstruction", sysInstObj)
                    }
                }

                val requestBody = requestBodyJson.toString().toRequestBody(MEDIA_TYPE)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: return@use
                        val jsonResponse = JSONObject(bodyString)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            if (content != null) {
                                val parts = content.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val text = parts.getJSONObject(0).optString("text", "")
                                    return@withContext AiResponse.success(
                                        text = text,
                                        providerUsed = providerId,
                                        modelUsed = candidate,
                                        latencyMs = latency
                                    )
                                }
                            }
                        }
                    } else {
                        val errBody = response.body?.string() ?: ""
                        Log.w(TAG, "Gemini call failed on $candidate: HTTP ${response.code} - $errBody")
                        lastError = "HTTP ${response.code}: $errBody"
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception contacting Gemini model $candidate: ${e.message}")
                lastError = e.localizedMessage ?: "Unknown network exception"
            }
        }

        AiResponse.failure(
            errorMessage = "All Gemini candidate models failed: $lastError",
            providerUsed = providerId,
            modelUsed = selectedModel
        )
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
            
            IMPORTANT: Output MUST be valid JSON adhering strictly to this schema:
            $schemaHint
            
            Return ONLY the raw JSON. Do NOT wrap with markdown backticks or extra words.
            """.trimIndent()
        } else {
            """
            $prompt
            
            IMPORTANT: Return ONLY raw, valid JSON. Do not include markdown codeblocks or conversational text.
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
                errorMessage = "Google Gemini API key not configured.",
                providerUsed = providerId,
                fallbackText = "Please configure your Gemini API key in settings."
            )
        }

        val selectedModel = model ?: keyStorage.getActiveModel(providerId)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$selectedModel:generateContent?key=$apiKey"
        val startTime = System.currentTimeMillis()

        try {
            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray()

                for (msg in messages) {
                    val role = if (msg.role == "user") "user" else "model"
                    val contentObj = JSONObject().apply {
                        put("role", role)
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", msg.text)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    contentsArray.put(contentObj)
                }
                put("contents", contentsArray)

                if (!systemInstruction.isNullOrBlank()) {
                    val sysInstObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", systemInstruction)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put("systemInstruction", sysInstObj)
                }
            }

            val requestBody = requestBodyJson.toString().toRequestBody(MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(bodyString)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        if (content != null) {
                            val parts = content.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                return@withContext AiResponse.success(
                                    text = text,
                                    providerUsed = providerId,
                                    modelUsed = selectedModel,
                                    latencyMs = latency
                                )
                            }
                        }
                    }
                }
                val err = response.body?.string() ?: "Code ${response.code}"
                return@withContext AiResponse.failure(
                    errorMessage = "Gemini chat failed: $err",
                    providerUsed = providerId,
                    modelUsed = selectedModel
                )
            }
        } catch (e: Exception) {
            return@withContext AiResponse.failure(
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
