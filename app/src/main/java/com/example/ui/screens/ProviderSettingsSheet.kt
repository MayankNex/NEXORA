package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProviderConfigEntity
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraPrimary
import com.example.ui.theme.NexoraPrimaryBright
import com.example.ui.theme.NexoraSecondary
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceHover
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTertiary
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.ui.viewmodel.SettingsUiState
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsSheet(
    viewModel: SettingsViewModel,
    uiState: SettingsUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val providers by viewModel.providers.collectAsStateWithLifecycle(emptyList())

    var selectedProviderForEdit by remember(providers) {
        mutableStateOf(providers.firstOrNull { it.isActive } ?: providers.firstOrNull())
    }

    var showAddCustomDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NexoraSurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI Providers & API Keys",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoraTextPrimary
                    )
                    Text(
                        text = "Bring Your Own Key (BYOK) • Encrypted On-Device",
                        fontSize = 12.sp,
                        color = NexoraTextMuted
                    )
                }

                Button(
                    onClick = { showAddCustomDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_custom_provider_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = NexoraSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Custom", fontSize = 12.sp, color = NexoraTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Provider Selector Chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                providers.forEach { provider ->
                    val isSelected = selectedProviderForEdit?.id == provider.id
                    val isActive = provider.isActive

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedProviderForEdit = provider
                                viewModel.clearTestResult()
                            }
                            .testTag("provider_chip_${provider.id}"),
                        color = if (isSelected) NexoraSurfaceHover else NexoraSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NexoraPrimary else NexoraBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NexoraSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = provider.name.split(" ").firstOrNull() ?: provider.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NexoraPrimaryBright else NexoraTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Provider Card
            selectedProviderForEdit?.let { provider ->
                ProviderEditCard(
                    provider = provider,
                    isTesting = uiState.isTesting && uiState.testingProviderId == provider.id,
                    testResult = if (uiState.testingProviderId == provider.id) uiState.testResult else null,
                    onSave = { updated ->
                        viewModel.saveProvider(updated)
                        selectedProviderForEdit = updated
                    },
                    onActivate = {
                        viewModel.setActiveProvider(provider.id)
                    },
                    onDelete = {
                        viewModel.deleteProvider(provider.id)
                        selectedProviderForEdit = providers.firstOrNull { it.id != provider.id }
                    },
                    onTestConnection = { baseUrl, apiKey, model ->
                        viewModel.testConnection(provider.id, baseUrl, apiKey, model)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddCustomDialog) {
        AddCustomProviderDialog(
            onDismiss = { showAddCustomDialog = false },
            onAdd = { newProvider ->
                viewModel.saveProvider(newProvider)
                selectedProviderForEdit = newProvider
                showAddCustomDialog = false
            }
        )
    }
}

@Composable
fun ProviderEditCard(
    provider: ProviderConfigEntity,
    isTesting: Boolean,
    testResult: com.example.data.remote.ConnectionTestResult?,
    onSave: (ProviderConfigEntity) -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onTestConnection: (baseUrl: String, apiKey: String, model: String) -> Unit
) {
    var name by remember(provider) { mutableStateOf(provider.name) }
    var baseUrl by remember(provider) { mutableStateOf(provider.baseUrl) }
    var apiKey by remember(provider) { mutableStateOf(provider.apiKey) }
    var defaultModel by remember(provider) { mutableStateOf(provider.defaultModel) }
    var temperature by remember(provider) { mutableFloatStateOf(provider.temperature) }
    var maxTokens by remember(provider) { mutableIntStateOf(provider.maxTokens) }
    var showApiKey by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onActivate() }
                ) {
                    Icon(
                        imageVector = if (provider.isActive) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (provider.isActive) NexoraSecondary else NexoraTextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (provider.isActive) "Active Engine" else "Tap to Set as Active Engine",
                        fontWeight = if (provider.isActive) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        color = if (provider.isActive) NexoraSecondary else NexoraTextSecondary
                    )
                }

                if (provider.isCustom) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Provider",
                            tint = NexoraTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Provider Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
        }

        item {
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL (OpenAI-Compatible)") },
                placeholder = { Text("https://api.groq.com/openai/v1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
        }

        item {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                placeholder = { Text(if (provider.id == "ollama") "Optional for local Ollama" else "sk-...") },
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle API Key visibility",
                            tint = NexoraTextMuted
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
        }

        item {
            OutlinedTextField(
                value = defaultModel,
                onValueChange = { defaultModel = it },
                label = { Text("Model ID") },
                placeholder = { Text("e.g. llama-3.3-70b-versatile, gpt-4o") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Temperature: ${String.format("%.2f", temperature)}", fontSize = 12.sp, color = NexoraTextSecondary)
                    Text(
                        if (temperature < 0.4f) "Deterministic / Code" else if (temperature < 0.9f) "Balanced" else "Creative",
                        fontSize = 11.sp,
                        color = NexoraPrimaryBright
                    )
                }
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0.0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = NexoraPrimary,
                        activeTrackColor = NexoraPrimaryBright,
                        inactiveTrackColor = NexoraBorder
                    )
                )
            }
        }

        item {
            // Test Connection Result Banner
            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.isSuccess) Color(0xFF063A29) else Color(0xFF3B121A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (res.isSuccess) NexoraSecondary else NexoraTertiary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (res.isSuccess) NexoraSecondary else NexoraTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = res.message,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Test Connection Button
                Button(
                    onClick = {
                        onTestConnection(baseUrl, apiKey, defaultModel)
                    },
                    enabled = !isTesting && baseUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("test_connection_button")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = NexoraSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testing...", fontSize = 12.sp, color = NexoraSecondary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NexoraSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test API Ping", fontSize = 12.sp, color = NexoraSecondary)
                    }
                }

                // Save Config Button
                Button(
                    onClick = {
                        val updated = provider.copy(
                            name = name.trim(),
                            baseUrl = baseUrl.trim(),
                            apiKey = apiKey.trim(),
                            defaultModel = defaultModel.trim(),
                            temperature = temperature,
                            maxTokens = maxTokens
                        )
                        onSave(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("save_provider_button")
                ) {
                    Text("Save Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddCustomProviderDialog(
    onDismiss: () -> Unit,
    onAdd: (ProviderConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf("Custom Self-Hosted") }
    var baseUrl by remember { mutableStateOf("http://10.0.2.2:11434/v1") }
    var apiKey by remember { mutableStateOf("") }
    var defaultModel by remember { mutableStateOf("llama3.2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Provider", color = NexoraTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Optional)") },
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = defaultModel,
                    onValueChange = { defaultModel = it },
                    label = { Text("Model ID") },
                    colors = defaultTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank() && defaultModel.isNotBlank()) {
                        val newEntity = ProviderConfigEntity(
                            id = "custom_" + System.currentTimeMillis(),
                            name = name.trim(),
                            baseUrl = baseUrl.trim(),
                            apiKey = apiKey.trim(),
                            defaultModel = defaultModel.trim(),
                            availableModels = defaultModel.trim(),
                            isActive = false,
                            isCustom = true
                        )
                        onAdd(newEntity)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimary)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NexoraTextSecondary)
            }
        },
        containerColor = NexoraSurface
    )
}

@Composable
fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NexoraPrimary,
    unfocusedBorderColor = NexoraBorder,
    focusedTextColor = NexoraTextPrimary,
    unfocusedTextColor = NexoraTextPrimary,
    focusedContainerColor = NexoraSurfaceVariant,
    unfocusedContainerColor = NexoraSurfaceVariant,
    focusedLabelColor = NexoraPrimaryBright,
    unfocusedLabelColor = NexoraTextMuted
)
