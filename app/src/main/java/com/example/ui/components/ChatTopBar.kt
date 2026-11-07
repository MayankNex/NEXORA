package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.data.local.PersonaPresetEntity
import com.example.data.local.ProviderConfigEntity
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraPrimary
import com.example.ui.theme.NexoraPrimaryBright
import com.example.ui.theme.NexoraSecondary
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    session: ChatSessionEntity?,
    activeProvider: ProviderConfigEntity?,
    activeModelId: String,
    activePersona: PersonaPresetEntity?,
    allPersonas: List<PersonaPresetEntity>,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectPersona: (PersonaPresetEntity) -> Unit,
    onSelectModel: (String) -> Unit,
    onRenameSession: (String) -> Unit,
    onClearChat: () -> Unit,
    onExportChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showPersonaMenu by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Thread", color = NexoraTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Thread Title") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameSession(renameText.trim())
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save", color = NexoraPrimaryBright)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = NexoraTextSecondary)
                }
            },
            containerColor = NexoraSurface
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = NexoraSurface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drawer Hamburger
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("drawer_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = NexoraTextPrimary
                    )
                }

                // Title & Model Chip
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            renameText = session?.title.orEmpty()
                            showRenameDialog = true
                        }
                    ) {
                        Text(
                            text = session?.title ?: "NEXORA Intelligence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NexoraTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = NexoraTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Model Selector Pill
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showModelMenu = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NexoraSecondary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeModelId.isNotBlank()) activeModelId else activeProvider?.name ?: "Configure Provider",
                                fontSize = 11.sp,
                                color = NexoraPrimaryBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Model picker dropdown
                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false },
                            modifier = Modifier.background(NexoraSurfaceVariant)
                        ) {
                            val availableModels = activeProvider?.availableModels
                                ?.split(",")
                                ?.map { it.trim() }
                                ?.filter { it.isNotEmpty() }
                                ?: listOf(activeModelId)

                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = model,
                                            color = if (model == activeModelId) NexoraPrimaryBright else NexoraTextPrimary,
                                            fontWeight = if (model == activeModelId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectModel(model)
                                        showModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Persona Pill Selector
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NexoraSurfaceVariant)
                            .border(1.dp, NexoraBorder, RoundedCornerShape(16.dp))
                            .clickable { showPersonaMenu = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("persona_selector_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = getPersonaIcon(activePersona?.id.orEmpty())
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NexoraSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activePersona?.name?.split(" ")?.firstOrNull() ?: "Prime",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NexoraTextPrimary
                        )
                    }

                    // Persona Dropdown
                    DropdownMenu(
                        expanded = showPersonaMenu,
                        onDismissRequest = { showPersonaMenu = false },
                        modifier = Modifier.background(NexoraSurfaceVariant)
                    ) {
                        allPersonas.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = getPersonaIcon(p.id),
                                            contentDescription = null,
                                            tint = if (p.id == activePersona?.id) NexoraSecondary else NexoraTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = p.name,
                                                color = if (p.id == activePersona?.id) NexoraPrimaryBright else NexoraTextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = p.tagline,
                                                color = NexoraTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectPersona(p)
                                    showPersonaMenu = false
                                }
                            )
                        }
                    }
                }

                // Overflow Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("top_bar_overflow_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = NexoraTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(NexoraSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Thread (Markdown)", color = NexoraTextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = NexoraPrimary) },
                            onClick = {
                                showMenu = false
                                onExportChat()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat", color = NexoraTextPrimary) },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = NexoraTextSecondary) },
                            onClick = {
                                showMenu = false
                                onClearChat()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("API & Providers", color = NexoraTextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Settings, null, tint = NexoraSecondary) },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )
                    }
                }
            }
        }
    }
}

fun getPersonaIcon(id: String): ImageVector {
    return when {
        id.contains("cybersec") -> Icons.Default.Security
        id.contains("code") -> Icons.Default.Code
        id.contains("hinglish") -> Icons.Default.Translate
        else -> Icons.Default.AutoAwesome
    }
}
