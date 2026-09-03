package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiModelInfo
import com.example.ai.ProviderType
import com.example.viewmodel.LifeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    viewModel: LifeViewModel,
    onBack: () -> Unit
) {
    val activeProviderId by viewModel.activeAiProviderId.collectAsStateWithLifecycle()
    val activeModelId by viewModel.activeAiModelId.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingAiConnection.collectAsStateWithLifecycle()
    val testResult by viewModel.aiConnectionResult.collectAsStateWithLifecycle()
    val customEndpoint by viewModel.customAiEndpoint.collectAsStateWithLifecycle()

    val aiManager = viewModel.aiManager
    val currentProvider = aiManager.getProvider(activeProviderId)
    val availableModels = currentProvider.getAvailableModels()

    var keyInput by remember(activeProviderId) {
        mutableStateOf(aiManager.keyStorage.getApiKey(activeProviderId))
    }
    var endpointInput by remember(activeProviderId, customEndpoint) {
        mutableStateOf(aiManager.keyStorage.getEndpointUrl(activeProviderId))
    }
    var isKeyVisible by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Brain & Multi-Provider",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Provider Agnostic Neural Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("ai_settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.resetAiSettingsToDefaults()
                            keyInput = ""
                            saveSuccessMessage = "Reset to default Gemini engine"
                        },
                        modifier = Modifier.testTag("ai_reset_defaults_button")
                    ) {
                        Text("Reset", style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // Section 1: Overview Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = "AI Engine",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Provider-Independent AI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Switch freely between Google Gemini, OpenRouter, OpenAI, and local LLMs (Ollama) without vendor lock-in.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Section 2: Choose Active Provider
            item {
                Text(
                    text = "SELECT AI PROVIDER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProviderType.values().forEach { provider ->
                        val isSelected = provider.id.equals(activeProviderId, ignoreCase = true)
                        ProviderCard(
                            provider = provider,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.selectAiProvider(provider.id)
                                keyInput = aiManager.keyStorage.getApiKey(provider.id)
                                endpointInput = aiManager.keyStorage.getEndpointUrl(provider.id)
                                saveSuccessMessage = null
                            }
                        )
                    }
                }
            }

            // Section 3: Model Selection for Active Provider
            item {
                Text(
                    text = "ACTIVE MODEL (${currentProvider.displayName.uppercase()})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableModels.forEach { modelInfo ->
                        val isSelected = modelInfo.id.equals(activeModelId, ignoreCase = true)
                        ModelSelectionCard(
                            modelInfo = modelInfo,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.selectAiModel(activeProviderId, modelInfo.id)
                            }
                        )
                    }
                }
            }

            // Section 4: API Key & Endpoint Configuration
            item {
                Text(
                    text = "AUTHENTICATION & ENDPOINT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Key Status indicator
                        val hasCustomKey = aiManager.keyStorage.hasCustomKey(activeProviderId)
                        val isGemini = activeProviderId.equals(ProviderType.GEMINI.id, ignoreCase = true)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "API Key",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            val statusBadge = when {
                                hasCustomKey -> "Custom Key Configured ✅"
                                isGemini -> "Using Built-in Gemini Key ⚡"
                                else -> "Key Required ⚠️"
                            }
                            val badgeColor = when {
                                hasCustomKey -> MaterialTheme.colorScheme.primary
                                isGemini -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }

                            Text(
                                text = statusBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("Enter ${currentProvider.displayName} API Key") },
                            placeholder = {
                                Text(
                                    when (activeProviderId) {
                                        ProviderType.OPENROUTER.id -> "sk-or-v1-..."
                                        ProviderType.OPENAI.id -> "sk-..."
                                        else -> "AIzaSy..."
                                    }
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (isKeyVisible) "Hide Key" else "Show Key"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_api_key_input")
                        )

                        // If Custom Endpoint or OpenAI, show Base URL
                        if (activeProviderId.equals(ProviderType.CUSTOM.id, ignoreCase = true) ||
                            activeProviderId.equals(ProviderType.OPENAI.id, ignoreCase = true) ||
                            activeProviderId.equals(ProviderType.OPENROUTER.id, ignoreCase = true)
                        ) {
                            OutlinedTextField(
                                value = endpointInput,
                                onValueChange = { endpointInput = it },
                                label = { Text("Custom Base URL") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_custom_endpoint_input")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.saveAiApiKey(activeProviderId, keyInput)
                                    if (activeProviderId.equals(ProviderType.CUSTOM.id, ignoreCase = true) ||
                                        activeProviderId.equals(ProviderType.OPENAI.id, ignoreCase = true)
                                    ) {
                                        viewModel.saveCustomAiEndpoint(endpointInput)
                                    }
                                    saveSuccessMessage = "Saved settings for ${currentProvider.displayName}"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_save_key_button")
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Key")
                            }

                            OutlinedButton(
                                onClick = {
                                    keyInput = ""
                                    viewModel.saveAiApiKey(activeProviderId, "")
                                    saveSuccessMessage = "Cleared API Key"
                                },
                                modifier = Modifier.testTag("ai_clear_key_button")
                            ) {
                                Text("Clear")
                            }
                        }

                        if (saveSuccessMessage != null) {
                            Text(
                                text = saveSuccessMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Section 5: Connection Diagnostics & Test Ping
            item {
                Text(
                    text = "CONNECTION DIAGNOSTICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Test Provider Ping",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Validates key authorization, network latency & output format",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.testAiConnection(activeProviderId, activeModelId)
                            },
                            enabled = !isTesting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_test_connection_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pinging $activeModelId...")
                            } else {
                                Icon(Icons.Filled.NetworkPing, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Connection")
                            }
                        }

                        // Test Result Banner
                        if (testResult != null) {
                            val res = testResult!!
                            val bannerColor = if (res.isSuccess) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                            val textColor = if (res.isSuccess) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = bannerColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (res.isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                                contentDescription = null,
                                                tint = textColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (res.isSuccess) "Connection Succeeded" else "Connection Failed",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }

                                        if (res.isSuccess && res.latencyMs > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                            ) {
                                                Text(
                                                    text = "⚡ ${res.latencyMs} ms",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = res.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: Security & Privacy Guarantee
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Zero Telemetry & Direct API Routing",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "API keys are encrypted and stored solely on your local device. Network requests route directly to the provider endpoints without third-party middleware or tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: ProviderType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .testTag("ai_provider_card_${provider.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (provider) {
                ProviderType.GEMINI -> Icons.Filled.AutoAwesome
                ProviderType.OPENROUTER -> Icons.Filled.Hub
                ProviderType.OPENAI -> Icons.Filled.Terminal
                ProviderType.CUSTOM -> Icons.Filled.Dns
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (provider) {
                        ProviderType.GEMINI -> "Official Google Gemini models (Flash & Pro)"
                        ProviderType.OPENROUTER -> "Unified access to Claude, Llama, DeepSeek & Mistral"
                        ProviderType.OPENAI -> "Standard OpenAI & ChatGPT-compatible models"
                        ProviderType.CUSTOM -> "Self-hosted Ollama, LocalAI, vLLM, or private servers"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@Composable
fun ModelSelectionCard(
    modelInfo: AiModelInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .testTag("ai_model_card_${modelInfo.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = modelInfo.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (modelInfo.isDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "DEFAULT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                RadioButton(selected = isSelected, onClick = onSelect)
            }

            Text(
                text = modelInfo.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Capabilities chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (modelInfo.capabilities.recommendedForFastTasks) {
                    CapabilityBadge("⚡ Fast Response")
                }
                if (modelInfo.capabilities.recommendedForDeepReasoning) {
                    CapabilityBadge("🧠 Deep Reasoning")
                }
                if (modelInfo.capabilities.supportsStructuredJson) {
                    CapabilityBadge("📋 Structured JSON")
                }
            }
        }
    }
}

@Composable
fun CapabilityBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
