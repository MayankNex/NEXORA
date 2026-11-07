package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatTopBar
import com.example.ui.components.MarkdownMessageView
import com.example.ui.theme.NexoraAssistantBubble
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraPrimary
import com.example.ui.theme.NexoraPrimaryBright
import com.example.ui.theme.NexoraSecondary
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTertiary
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.ui.theme.NexoraUserBubble
import com.example.ui.viewmodel.ChatUiState
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll on new message or streaming token
    LaunchedEffect(uiState.messages.size, uiState.streamingBuffer) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NexoraBackground,
        topBar = {
            ChatTopBar(
                session = uiState.currentSession,
                activeProvider = uiState.activeProvider,
                activeModelId = uiState.activeModelId,
                activePersona = uiState.activePersona,
                allPersonas = viewModel.allPersonas.value,
                onOpenDrawer = onOpenDrawer,
                onOpenSettings = onOpenSettings,
                onSelectPersona = { viewModel.selectPersona(it) },
                onSelectModel = { viewModel.selectModel(it) },
                onRenameSession = { viewModel.renameSession(uiState.currentSessionId.orEmpty(), it) },
                onClearChat = { viewModel.clearCurrentChat() },
                onExportChat = { viewModel.exportCurrentSession(context) }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChanged = { inputText = it },
                onSendMessage = { prompt -> viewModel.sendMessage(prompt) },
                isStreaming = uiState.isStreaming,
                onStopStreaming = { viewModel.stopStreaming() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Error banner if any
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { error ->
                    ErrorBanner(
                        message = error,
                        onDismiss = { viewModel.dismissError() },
                        onOpenSettings = onOpenSettings
                    )
                }
            }

            if (uiState.messages.isEmpty()) {
                EmptyStateView(
                    activeModel = uiState.activeModelId,
                    onPromptSelected = { prompt ->
                        inputText = prompt
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("chat_messages_list"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            isLastMessage = message.id == uiState.messages.lastOrNull()?.id,
                            isStreaming = uiState.isStreaming && message.id == uiState.messages.lastOrNull()?.id,
                            streamingBuffer = uiState.streamingBuffer,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Message", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                inputText = message.content
                            },
                            onDelete = {
                                viewModel.deleteMessage(message.id)
                            },
                            onRegenerate = {
                                // Find previous user message
                                val idx = uiState.messages.indexOfFirst { it.id == message.id }
                                if (idx > 0) {
                                    val previousUserMsg = uiState.messages[idx - 1]
                                    viewModel.deleteMessage(message.id)
                                    viewModel.sendMessage(previousUserMsg.content)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isLastMessage: Boolean,
    isStreaming: Boolean,
    streamingBuffer: String,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRegenerate: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_item" else "assistant_message_item"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                // AI Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NexoraPrimary, NexoraSecondary)
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(NexoraBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "NEXORA AI",
                            tint = NexoraSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Message Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) NexoraUserBubble else NexoraAssistantBubble,
                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, NexoraBorder) else null,
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isUser && message.modelUsed != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = message.modelUsed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NexoraSecondary
                            )
                            if (message.promptTokens > 0 || message.completionTokens > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${message.promptTokens + message.completionTokens} tok",
                                    fontSize = 10.sp,
                                    color = NexoraTextMuted
                                )
                            }
                        }
                    }

                    val displayContent = if (!isUser && isLastMessage && isStreaming) {
                        if (streamingBuffer.isNotBlank()) streamingBuffer else message.content
                    } else {
                        message.content
                    }

                    if (isUser) {
                        Text(
                            text = displayContent,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    } else {
                        MarkdownMessageView(
                            content = displayContent,
                            isStreaming = isStreaming && isLastMessage
                        )
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(10.dp))
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NexoraSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = NexoraPrimaryBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Action Toolbar below message
        Row(
            modifier = Modifier
                .padding(
                    start = if (isUser) 0.dp else 42.dp,
                    end = if (isUser) 42.dp else 0.dp,
                    top = 4.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Copy Action
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy message",
                tint = NexoraTextMuted,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onCopy() }
            )

            if (isUser) {
                // Edit Prompt
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit message",
                    tint = NexoraTextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onEdit() }
                )
            } else {
                // Regenerate
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Regenerate response",
                    tint = NexoraTextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onRegenerate() }
                )
            }

            // Delete Action
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete message",
                tint = NexoraTextMuted,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun EmptyStateView(
    activeModel: String,
    onPromptSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(NexoraPrimary.copy(alpha = 0.4f), NexoraBackground)
                    )
                )
                .border(1.5.dp, NexoraPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = NexoraPrimaryBright,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NEXORA AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = NexoraTextPrimary,
            letterSpacing = 2.sp
        )

        Text(
            text = "Personal Intelligence & Multi-Provider Platform",
            fontSize = 13.sp,
            color = NexoraTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (activeModel.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NexoraSurfaceVariant)
                    .border(1.dp, NexoraBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Engine: $activeModel",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NexoraSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Suggested Cards
        val starterPrompts = listOf(
            "🛡️ Security Audit" to "Analyze this code for common security vulnerabilities (OWASP, SQLi, XSS, memory leaks) and demonstrate defensive fixes.",
            "⚡ Architecture Review" to "Design a scalable event-driven backend architecture with clean separation of concerns and caching.",
            "🇮🇳 Hinglish Explanation" to "Neural networks kaise kaam karte hain? Mujhe real life examples ke saath Hinglish mein samjhao.",
            "💻 High-Performance Code" to "Write a thread-safe caching system in Kotlin with TTL eviction and coroutine locks."
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            starterPrompts.forEach { (title, prompt) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPromptSelected(prompt) },
                    colors = CardDefaults.cardColors(containerColor = NexoraSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = NexoraTextPrimary
                        )
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            color = NexoraTextSecondary,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1217)),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraTertiary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = NexoraTertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Engine Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NexoraTertiary
                )
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = NexoraTertiary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Settings", fontSize = 11.sp, color = Color.White)
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = NexoraTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
