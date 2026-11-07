package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.data.local.TokenStats
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraPrimary
import com.example.ui.theme.NexoraPrimaryBright
import com.example.ui.theme.NexoraSecondary
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceHover
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SidebarDrawer(
    sessions: List<ChatSessionEntity>,
    currentSessionId: String?,
    activeModelId: String,
    tokenStats: TokenStats,
    onSelectSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onOpenProviders: () -> Unit,
    onOpenPersonas: () -> Unit,
    onOpenAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val pinnedSessions = remember(filteredSessions) { filteredSessions.filter { it.isPinned } }
    val regularSessions = remember(filteredSessions) { filteredSessions.filter { !it.isPinned } }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = NexoraSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NexoraPrimary, NexoraSecondary))
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
                            contentDescription = null,
                            tint = NexoraSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NEXORA AI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoraTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (activeModelId.isNotBlank()) activeModelId else "Intelligence Core",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NexoraSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // New Chat Button
            Button(
                onClick = onNewChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_chat_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexoraPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Intelligence Thread",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", fontSize = 12.sp, color = NexoraTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexoraPrimary,
                    unfocusedBorderColor = NexoraBorder,
                    focusedTextColor = NexoraTextPrimary,
                    unfocusedTextColor = NexoraTextPrimary,
                    focusedContainerColor = NexoraSurfaceVariant,
                    unfocusedContainerColor = NexoraSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sessions List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (pinnedSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = "PINNED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NexoraSecondary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                        )
                    }
                    items(pinnedSessions, key = { it.id }) { session ->
                        SessionItemRow(
                            session = session,
                            isSelected = session.id == currentSessionId,
                            onSelect = { onSelectSession(session.id) },
                            onTogglePin = { onTogglePin(session.id, session.isPinned) },
                            onDelete = { onDeleteSession(session.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                if (regularSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = "CONVERSATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NexoraTextMuted,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                        )
                    }
                    items(regularSessions, key = { it.id }) { session ->
                        SessionItemRow(
                            session = session,
                            isSelected = session.id == currentSessionId,
                            onSelect = { onSelectSession(session.id) },
                            onTogglePin = { onTogglePin(session.id, session.isPinned) },
                            onDelete = { onDeleteSession(session.id) }
                        )
                    }
                } else if (pinnedSessions.isEmpty()) {
                    item {
                        Text(
                            text = "No conversations found",
                            fontSize = 12.sp,
                            color = NexoraTextMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = NexoraBorder,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Bottom Navigation Tools
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SidebarNavItem(
                    icon = Icons.Default.Settings,
                    label = "AI Providers & Keys",
                    badge = "BYOK",
                    onClick = onOpenProviders,
                    testTag = "sidebar_nav_providers"
                )

                SidebarNavItem(
                    icon = Icons.Default.Face,
                    label = "Persona Studio",
                    badge = "4 Active",
                    onClick = onOpenPersonas,
                    testTag = "sidebar_nav_personas"
                )

                SidebarNavItem(
                    icon = Icons.Default.Analytics,
                    label = "Token Analytics",
                    badge = "${tokenStats.totalPrompt + tokenStats.totalCompletion} tok",
                    onClick = onOpenAnalytics,
                    testTag = "sidebar_nav_analytics"
                )
            }
        }
    }
}

@Composable
fun SessionItemRow(
    session: ChatSessionEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val background = if (isSelected) NexoraSurfaceHover else Color.Transparent
    val border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NexoraPrimary.copy(alpha = 0.5f)) else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() },
        color = background,
        border = border,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = if (isSelected) NexoraPrimaryBright else NexoraTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) NexoraTextPrimary else NexoraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatSessionTime(session.updatedAt),
                    fontSize = 10.sp,
                    color = NexoraTextMuted
                )
            }

            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pin session",
                    tint = if (session.isPinned) NexoraSecondary else NexoraTextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete session",
                    tint = NexoraTextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SidebarNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badge: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = NexoraSurfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NexoraPrimaryBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NexoraTextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NexoraSurfaceHover)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NexoraSecondary
                )
            }
        }
    }
}

fun formatSessionTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
