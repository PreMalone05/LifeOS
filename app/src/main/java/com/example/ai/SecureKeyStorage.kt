package com.example.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.BuildConfig

/**
 * Secure on-device key and configuration storage for AI providers.
 * Uses AndroidX Security Crypto's EncryptedSharedPreferences (Keystore-backed AES-256)
 * so API keys are securely encrypted at rest and never logged in analytics or crash reports.
 */
class SecureKeyStorage(private val context: Context) {

    private val legacyPrefs: SharedPreferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to private SharedPreferences", e)
            context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    init {
        migrateLegacyKeys()
    }

    companion object {
        private const val TAG = "SecureKeyStorage"
        private const val LEGACY_PREFS_NAME = "lifeos_ai_secure_store"
        private const val ENCRYPTED_PREFS_NAME = "lifeos_ai_keystore_secure_store"
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
        private const val KEY_PREFIX_API_KEY = "sec_api_key_"
        private const val KEY_PREFIX_MODEL = "selected_model_"
        private const val KEY_PREFIX_ENDPOINT = "endpoint_url_"
        private const val KEY_PREFIX_TEMP = "temp_"
        
        // Legacy salt kept strictly for migrating older obfuscated keys on first read
        private const val LEGACY_OBFUSCATION_SALT = "LifeOS-Neural-Key-Vault-v1"

        @Volatile
        private var INSTANCE: SecureKeyStorage? = null

        fun getInstance(context: Context): SecureKeyStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureKeyStorage(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private fun migrateLegacyKeys() {
        try {
            val legacyAll = legacyPrefs.all
            if (legacyAll.isNotEmpty()) {
                val editor = prefs.edit()
                for ((key, value) in legacyAll) {
                    if (key.startsWith(KEY_PREFIX_API_KEY) && value is String) {
                        val decrypted = legacyDeobfuscate(value)
                        if (decrypted.isNotBlank()) {
                            editor.putString(key, decrypted)
                        }
                    } else if (value is String) {
                        editor.putString(key, value)
                    }
                }
                editor.apply()
                legacyPrefs.edit().clear().apply()
                Log.i(TAG, "Successfully migrated legacy keys to EncryptedSharedPreferences and cleared legacy store.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception during legacy key migration", e)
        }
    }

    fun getActiveProvider(): String {
        return prefs.getString(KEY_ACTIVE_PROVIDER, ProviderType.GEMINI.id) ?: ProviderType.GEMINI.id
    }

    fun setActiveProvider(providerId: String) {
        prefs.edit().putString(KEY_ACTIVE_PROVIDER, providerId.lowercase()).apply()
    }

    fun getApiKey(providerId: String): String {
        val keyName = KEY_PREFIX_API_KEY + providerId.lowercase()
        
        // 1. Check keystore-backed encrypted prefs
        val storedKey = prefs.getString(keyName, null)
        if (!storedKey.isNullOrBlank()) {
            return storedKey
        }

        // 2. Migration fallback: check legacy obfuscated prefs on first read
        val legacyStored = legacyPrefs.getString(keyName, null)
        if (!legacyStored.isNullOrBlank()) {
            val decrypted = legacyDeobfuscate(legacyStored)
            if (decrypted.isNotBlank()) {
                prefs.edit().putString(keyName, decrypted).apply()
                legacyPrefs.edit().remove(keyName).apply()
                return decrypted
            }
        }

        // 3. Fallback for Gemini provider if no custom key has been manually saved
        if (providerId.equals(ProviderType.GEMINI.id, ignoreCase = true)) {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
                return buildKey
            }
        }

        return ""
    }

    fun setApiKey(providerId: String, key: String) {
        val keyName = KEY_PREFIX_API_KEY + providerId.lowercase()
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) {
            prefs.edit().remove(keyName).apply()
        } else {
            prefs.edit().putString(keyName, cleanKey).apply()
        }
        // Ensure any legacy obfuscated entry is purged
        if (legacyPrefs.contains(keyName)) {
            legacyPrefs.edit().remove(keyName).apply()
        }
    }

    fun hasCustomKey(providerId: String): Boolean {
        val keyName = KEY_PREFIX_API_KEY + providerId.lowercase()
        if (!prefs.getString(keyName, null).isNullOrBlank()) return true
        val legacy = legacyPrefs.getString(keyName, null)
        return !legacy.isNullOrBlank()
    }

    fun clearApiKey(providerId: String) {
        val keyName = KEY_PREFIX_API_KEY + providerId.lowercase()
        prefs.edit().remove(keyName).apply()
        legacyPrefs.edit().remove(keyName).apply()
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
        legacyPrefs.edit().clear().apply()
    }

    private fun legacyDeobfuscate(encoded: String): String {
        return try {
            val decodedBytes = Base64.decode(encoded, Base64.NO_WRAP)
            val saltBytes = LEGACY_OBFUSCATION_SALT.toByteArray(Charsets.UTF_8)
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
