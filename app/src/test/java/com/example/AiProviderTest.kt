package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AiProviderTest {

    private lateinit var context: Context
    private lateinit var keyStorage: SecureKeyStorage
    private lateinit var aiManager: AiManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        keyStorage = SecureKeyStorage(context)
        keyStorage.resetToDefaults()
        aiManager = AiManager.initialize(context)
    }

    @Test
    fun `SecureKeyStorage stores and retrieves obfuscated API keys correctly`() {
        val testApiKey = "sk-ant-api03-test-secret-key-123456"
        keyStorage.setApiKey(ProviderType.OPENROUTER.id, testApiKey)

        val retrievedKey = keyStorage.getApiKey(ProviderType.OPENROUTER.id)
        assertEquals(testApiKey, retrievedKey)
        assertTrue(keyStorage.hasCustomKey(ProviderType.OPENROUTER.id))

        // Check masked key
        val masked = keyStorage.getMaskedApiKey(ProviderType.OPENROUTER.id)
        assertTrue(masked.contains("••••"))
        assertTrue(masked.startsWith("sk-a"))
    }

    @Test
    fun `SecureKeyStorage persists active provider and model choices`() {
        keyStorage.setActiveProvider(ProviderType.OPENROUTER.id)
        assertEquals(ProviderType.OPENROUTER.id, keyStorage.getActiveProvider())

        val chosenModel = "anthropic/claude-3.5-sonnet"
        keyStorage.setActiveModel(ProviderType.OPENROUTER.id, chosenModel)
        assertEquals(chosenModel, keyStorage.getActiveModel(ProviderType.OPENROUTER.id))

        keyStorage.setActiveProvider(ProviderType.GEMINI.id)
        assertEquals(ProviderType.GEMINI.id, keyStorage.getActiveProvider())
    }

    @Test
    fun `SecureKeyStorage persists custom endpoint URLs for Local AI`() {
        val ollamaUrl = "http://192.168.1.100:11434/v1/chat/completions"
        keyStorage.setEndpointUrl(ProviderType.CUSTOM.id, ollamaUrl)

        val storedUrl = keyStorage.getEndpointUrl(ProviderType.CUSTOM.id)
        assertEquals(ollamaUrl, storedUrl)
    }

    @Test
    fun `AiManager correctly registers all four core providers`() {
        val providers = aiManager.getAllProviders()
        assertEquals(4, providers.size)

        val gemini = aiManager.getProvider(ProviderType.GEMINI.id)
        assertEquals("Google Gemini", gemini.displayName)

        val openRouter = aiManager.getProvider(ProviderType.OPENROUTER.id)
        assertEquals("OpenRouter", openRouter.displayName)

        val openAi = aiManager.getProvider(ProviderType.OPENAI.id)
        assertEquals("OpenAI Compatible", openAi.displayName)

        val custom = aiManager.getProvider(ProviderType.CUSTOM.id)
        assertEquals(ProviderType.CUSTOM.displayName, custom.displayName)
    }

    @Test
    fun `AiProvider model lists provide rich capability descriptors`() {
        val openRouterProvider = aiManager.getProvider(ProviderType.OPENROUTER.id)
        val models = openRouterProvider.getAvailableModels()
        assertTrue(models.isNotEmpty())

        val claude = models.firstOrNull { it.id.contains("claude-3.5-sonnet") }
        assertNotNull(claude)
        assertTrue(claude!!.capabilities.recommendedForDeepReasoning)
        assertTrue(claude.capabilities.supportsStructuredJson)

        val geminiProvider = aiManager.getProvider(ProviderType.GEMINI.id)
        val geminiModels = geminiProvider.getAvailableModels()
        assertTrue(geminiModels.any { it.capabilities.recommendedForFastTasks })
    }

    @Test
    fun `Reset to defaults clears custom keys and restores Gemini default`() {
        keyStorage.setApiKey(ProviderType.OPENAI.id, "sk-test-key")
        keyStorage.setActiveProvider(ProviderType.OPENAI.id)

        keyStorage.resetToDefaults()

        assertEquals(ProviderType.GEMINI.id, keyStorage.getActiveProvider())
        assertEquals("gemini-3.5-flash", keyStorage.getActiveModel(ProviderType.GEMINI.id))
        assertFalse(keyStorage.hasCustomKey(ProviderType.OPENAI.id))
    }
}
