package com.example.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.BuildConfig

/**
 * Secure on-device key and configuration storage for AI providers.
 * Uses encrypted / obfuscated SharedPreferences so API keys are never stored in plaintext
 * and never logged in analytics or crash reports.
 */
class SecureKeyStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "lifeos_ai_secure_store"
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
        private const val KEY_PREFIX_API_KEY = "sec_api_key_"
        private const val KEY_PREFIX_MODEL = "selected_model_"
        private const val KEY_PREFIX_ENDPOINT = "endpoint_url_"
        private const val KEY_PREFIX_TEMP = "temp_"
        
        // Obfuscation mask for on-device non-plaintext storage
        private const val OBFUSCATION_SALT = "LifeOS-Neural-Key-Vault-v1"

        @Volatile
        private var INSTANCE: SecureKeyStorage? = null

        fun getInstance(context: Context): SecureKeyStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureKeyStorage(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun getActiveProvider(): String {
        return prefs.getString(KEY_ACTIVE_PROVIDER, ProviderType.GEMINI.id) ?: ProviderType.GEMINI.id
    }

    fun setActiveProvider(providerId: String) {
        prefs.edit().putString(KEY_ACTIVE_PROVIDER, providerId.lowercase()).apply()
    }

    fun getApiKey(providerId: String): String {
        val storedObfuscated = prefs.getString(KEY_PREFIX_API_KEY + providerId.lowercase(), null)
        if (!storedObfuscated.isNullOrBlank()) {
            val decrypted = deobfuscate(storedObfuscated)
            if (decrypted.isNotBlank()) return decrypted
        }

        // Fallback for Gemini provider if no custom key has been manually saved
        if (providerId.equals(ProviderType.GEMINI.id, ignoreCase = true)) {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
                return buildKey
            }
        }

        return ""
    }

    fun setApiKey(providerId: String, key: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) {
            prefs.edit().remove(KEY_PREFIX_API_KEY + providerId.lowercase()).apply()
        } else {
            val obfuscated = obfuscate(cleanKey)
            prefs.edit().putString(KEY_PREFIX_API_KEY + providerId.lowercase(), obfuscated).apply()
        }
    }

    fun hasCustomKey(providerId: String): Boolean {
        val stored = prefs.getString(KEY_PREFIX_API_KEY + providerId.lowercase(), null)
        return !stored.isNullOrBlank()
    }

    fun clearApiKey(providerId: String) {
        prefs.edit().remove(KEY_PREFIX_API_KEY + providerId.lowercase()).apply()
    }

    fun getActiveModel(providerId: String): String {
        val defaultModel = when (ProviderType.fromId(providerId)) {
            ProviderType.GEMINI -> "gemini-3.5-flash"
            ProviderType.OPENROUTER -> "anthropic/claude-3.5-sonnet"
            ProviderType.OPENAI -> "gpt-4o-mini"
            ProviderType.CUSTOM -> "llama3"
        }
        return prefs.getString(KEY_PREFIX_MODEL + providerId.lowercase(), defaultModel) ?: defaultModel
    }

    fun setActiveModel(providerId: String, modelId: String) {
        prefs.edit().putString(KEY_PREFIX_MODEL + providerId.lowercase(), modelId.trim()).apply()
    }

    fun getEndpointUrl(providerId: String): String {
        val defaultUrl = ProviderType.fromId(providerId).defaultEndpoint
        return prefs.getString(KEY_PREFIX_ENDPOINT + providerId.lowercase(), defaultUrl) ?: defaultUrl
    }

    fun setEndpointUrl(providerId: String, url: String) {
        prefs.edit().putString(KEY_PREFIX_ENDPOINT + providerId.lowercase(), url.trim()).apply()
    }

    fun getMaskedApiKey(providerId: String): String {
        val key = getApiKey(providerId)
        if (key.isBlank()) return "Not configured"
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••••••${key.takeLast(4)}"
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    private fun obfuscate(input: String): String {
        return try {
            val saltBytes = OBFUSCATION_SALT.toByteArray(Charsets.UTF_8)
            val inputBytes = input.toByteArray(Charsets.UTF_8)
            val result = ByteArray(inputBytes.size)
            for (i in inputBytes.indices) {
                result[i] = (inputBytes[i].toInt() xor saltBytes[i % saltBytes.size].toInt()).toByte()
            }
            Base64.encodeToString(result, Base64.NO_WRAP)
        } catch (e: Exception) {
            Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    private fun deobfuscate(encoded: String): String {
        return try {
            val decodedBytes = Base64.decode(encoded, Base64.NO_WRAP)
            val saltBytes = OBFUSCATION_SALT.toByteArray(Charsets.UTF_8)
            val result = ByteArray(decodedBytes.size)
            for (i in decodedBytes.indices) {
                result[i] = (decodedBytes[i].toInt() xor saltBytes[i % saltBytes.size].toInt()).toByte()
            }
            String(result, Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                String(Base64.decode(encoded, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (e2: Exception) {
                ""
            }
        }
    }
}
