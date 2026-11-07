package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PersonaPresetEntity
import com.example.ui.components.getPersonaIcon
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
import com.example.ui.viewmodel.SettingsViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaStudioSheet(
    viewModel: SettingsViewModel,
    activePersonaId: String?,
    onSelectPersona: (PersonaPresetEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val personas by viewModel.personas.collectAsStateWithLifecycle(emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPersona by remember { mutableStateOf<PersonaPresetEntity?>(null) }

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
                        text = "Persona Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoraTextPrimary
                    )
                    Text(
                        text = "Customize system directives, tone & AI behavior",
                        fontSize = 12.sp,
                        color = NexoraTextMuted
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_persona_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Persona", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(personas, key = { it.id }) { persona ->
                    val isSelected = persona.id == activePersonaId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPersona(persona) }
                            .testTag("persona_card_${persona.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NexoraSurfaceHover else NexoraSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NexoraPrimary else NexoraBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(NexoraBackground)
                                            .border(1.dp, NexoraSecondary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getPersonaIcon(persona.id),
                                            contentDescription = null,
                                            tint = NexoraSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = persona.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) NexoraPrimaryBright else NexoraTextPrimary
                                        )
                                        Text(
                                            text = persona.tagline,
                                            fontSize = 11.sp,
                                            color = NexoraTextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NexoraPrimary)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("Active", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    IconButton(
                                        onClick = { editingPersona = persona },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Persona",
                                            tint = NexoraTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (!persona.isDefault) {
                                        IconButton(
                                            onClick = { viewModel.deletePersona(persona.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = NexoraTertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = persona.systemPrompt,
                                fontSize = 12.sp,
                                color = NexoraTextMuted,
                                maxLines = 3,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCreateDialog) {
        PersonaEditorDialog(
            persona = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newPersona ->
                viewModel.savePersona(newPersona)
                showCreateDialog = false
            }
        )
    }

    editingPersona?.let { p ->
        PersonaEditorDialog(
            persona = p,
            onDismiss = { editingPersona = null },
            onSave = { updated ->
                viewModel.savePersona(updated)
                editingPersona = null
            }
        )
    }
}

@Composable
fun PersonaEditorDialog(
    persona: PersonaPresetEntity?,
    onDismiss: () -> Unit,
    onSave: (PersonaPresetEntity) -> Unit
) {
    var name by remember { mutableStateOf(persona?.name ?: "") }
    var tagline by remember { mutableStateOf(persona?.tagline ?: "") }
    var systemPrompt by remember { mutableStateOf(persona?.systemPrompt ?: "") }
    var temperature by remember { mutableFloatStateOf(persona?.defaultTemperature ?: 0.7f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (persona == null) "Create AI Persona" else "Edit Persona",
                color = NexoraTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Short Tagline") },
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("System Directive / Instructions") },
                    minLines = 4,
                    maxLines = 6,
                    colors = defaultTextFieldColors()
                )
                Column {
                    Text("Sampling Temperature: ${String.format("%.2f", temperature)}", fontSize = 11.sp, color = NexoraTextSecondary)
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && systemPrompt.isNotBlank()) {
                        val toSave = persona?.copy(
                            name = name.trim(),
                            tagline = tagline.trim(),
                            systemPrompt = systemPrompt.trim(),
                            defaultTemperature = temperature
                        ) ?: PersonaPresetEntity(
                            id = "custom_persona_" + UUID.randomUUID().toString(),
                            name = name.trim(),
                            tagline = tagline.trim(),
                            systemPrompt = systemPrompt.trim(),
                            defaultTemperature = temperature,
                            isDefault = false
                        )
                        onSave(toSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimary)
            ) {
                Text("Save", color = Color.White)
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
