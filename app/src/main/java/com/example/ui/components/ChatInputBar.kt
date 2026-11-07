package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isStreaming: Boolean,
    onStopStreaming: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickPrompts by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "🛡️ Audit this for security vulnerabilities:" to "Please audit the following code for security vulnerabilities, CWE issues, and logic flaws, providing defensive fixes:\n\n",
        "⚡ Clean Refactor" to "Please refactor the following code for optimal performance, modularity, and clean architecture:\n\n",
        "🇮🇳 Hinglish Explainer" to "Yeh concept mujhe aasaan Hinglish mein step-by-step samjhao:\n\n",
        "🔬 Threat Model" to "Generate a STRIDE threat model and mitigation checklist for this system architecture:\n\n",
        "🧪 Write Unit Tests" to "Write comprehensive unit test cases and edge cases for the following implementation:\n\n"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = NexoraSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Quick prompt chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { (label, promptPrefix) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexoraSurfaceVariant)
                            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onTextChanged(promptPrefix)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = NexoraTextSecondary
                        )
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Input Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(NexoraSurfaceVariant)
                        .border(1.dp, NexoraBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask NEXORA anything or paste code...",
                            color = NexoraTextMuted,
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            color = NexoraTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(NexoraPrimaryBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 20.dp, max = 140.dp)
                            .testTag("chat_input_text_field")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stop Button (if streaming) OR Send Button
                if (isStreaming) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NexoraTertiary)
                            .clickable { onStopStreaming() }
                            .testTag("stop_streaming_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Generation",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    val canSend = text.isNotBlank()
                    val buttonBrush = if (canSend) {
                        Brush.linearGradient(listOf(NexoraPrimary, NexoraPrimaryBright))
                    } else {
                        Brush.linearGradient(listOf(NexoraSurfaceHover, NexoraSurfaceHover))
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(buttonBrush)
                            .clickable(enabled = canSend) {
                                if (canSend) {
                                    onSendMessage(text)
                                    onTextChanged("")
                                }
                            }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (canSend) Color.White else NexoraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
