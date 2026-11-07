package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.data.local.TokenStats
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

@Composable
fun TokenAnalyticsDialog(
    currentSession: ChatSessionEntity?,
    tokenStats: TokenStats,
    viewModel: SettingsViewModel,
    onExportMarkdown: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showConfirmWipeDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = NexoraSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("API & Token Telemetry", color = NexoraTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Lifetime stats
                Text(
                    text = "LIFETIME CONSUMPTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NexoraTextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Prompt Tok",
                        value = "${tokenStats.totalPrompt}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completion Tok",
                        value = "${tokenStats.totalCompletion}",
                        modifier = Modifier.weight(1f)
                    )
                }

                StatCard(
                    title = "Total Tokens Processed",
                    value = "${tokenStats.totalPrompt + tokenStats.totalCompletion} tokens",
                    highlight = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Current Session stats
                if (currentSession != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ACTIVE THREAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoraTextMuted
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = NexoraSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = currentSession.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = NexoraTextPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Model: ${currentSession.modelId}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NexoraSecondary
                                )
                                Text(
                                    text = "${currentSession.totalPromptTokens + currentSession.totalCompletionTokens} tokens",
                                    fontSize = 11.sp,
                                    color = NexoraTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Export Button
                Button(
                    onClick = onExportMarkdown,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceHover),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("export_markdown_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = NexoraPrimaryBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Thread as Markdown", fontSize = 12.sp, color = NexoraTextPrimary)
                }

                // Danger zone: Wipe data
                Button(
                    onClick = { showConfirmWipeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B121A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraTertiary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("purge_all_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = NexoraTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Purge All Stored Keys & Chats", fontSize = 12.sp, color = NexoraTertiary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NexoraPrimaryBright)
            }
        },
        containerColor = NexoraSurface
    )

    if (showConfirmWipeDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmWipeDialog = false },
            title = { Text("Purge All Data & Credentials?", color = NexoraTertiary) },
            text = {
                Text(
                    text = "This will permanently delete all chat history, stored API keys, and custom personas from this device. Factory default presets will be reloaded.",
                    color = NexoraTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllUserData()
                        showConfirmWipeDialog = false
                        onDismiss()
                        Toast.makeText(context, "All credentials and chats purged", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraTertiary)
                ) {
                    Text("Confirm Purge", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmWipeDialog = false }) {
                    Text("Cancel", color = NexoraTextSecondary)
                }
            },
            containerColor = NexoraSurface
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = NexoraSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (highlight) NexoraSecondary.copy(alpha = 0.6f) else NexoraBorder
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = NexoraTextMuted)
            Text(
                text = value,
                fontSize = if (highlight) 16.sp else 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (highlight) NexoraSecondary else NexoraTextPrimary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
