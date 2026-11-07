package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexoraCodeBackground
import com.example.ui.theme.NexoraCodeBorder
import com.example.ui.theme.NexoraPrimary
import com.example.ui.theme.NexoraPrimaryBright
import com.example.ui.theme.NexoraSecondary
import com.example.ui.theme.NexoraSurfaceHover
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import kotlinx.coroutines.delay

sealed interface ContentSegment {
    data class Text(val content: String) : ContentSegment
    data class Code(val language: String, val code: String) : ContentSegment
}

fun parseMarkdownSegments(raw: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val codeFenceRegex = Regex("```([a-zA-Z0-9_-]*)\\s*\\n?([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = codeFenceRegex.findAll(raw)

    for (match in matches) {
        if (match.range.first > lastIndex) {
            val textBefore = raw.substring(lastIndex, match.range.first)
            if (textBefore.isNotEmpty()) {
                segments.add(ContentSegment.Text(textBefore))
            }
        }

        val language = match.groupValues[1].ifBlank { "code" }
        val codeBody = match.groupValues[2].trimEnd()
        segments.add(ContentSegment.Code(language, codeBody))
        lastIndex = match.range.last + 1
    }

    if (lastIndex < raw.length) {
        val remaining = raw.substring(lastIndex)
        if (remaining.isNotEmpty()) {
            segments.add(ContentSegment.Text(remaining))
        }
    }

    if (segments.isEmpty() && raw.isNotEmpty()) {
        segments.add(ContentSegment.Text(raw))
    }

    return segments
}

@Composable
fun MarkdownMessageView(
    content: String,
    isStreaming: Boolean = false,
    textColor: Color = NexoraTextPrimary,
    modifier: Modifier = Modifier
) {
    val segments = remember(content) { parseMarkdownSegments(content) }

    Column(modifier = modifier) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is ContentSegment.Text -> {
                    RenderFormattedText(
                        raw = segment.content,
                        textColor = textColor,
                        showCursor = isStreaming && index == segments.lastIndex
                    )
                }
                is ContentSegment.Code -> {
                    CodeBlockCard(
                        language = segment.language,
                        code = segment.code
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (isStreaming && (segments.isEmpty() || segments.last() is ContentSegment.Code)) {
            PulsingStreamingCursor()
        }
    }
}

@Composable
fun RenderFormattedText(
    raw: String,
    textColor: Color,
    showCursor: Boolean = false
) {
    val annotated = remember(raw, textColor) {
        buildAnnotatedString {
            var i = 0
            while (i < raw.length) {
                // Bold check **text**
                if (raw.startsWith("**", i)) {
                    val endBold = raw.indexOf("**", i + 2)
                    if (endBold != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                            append(raw.substring(i + 2, endBold))
                        }
                        i = endBold + 2
                        continue
                    }
                }

                // Inline code `code`
                if (raw[i] == '`') {
                    val endCode = raw.indexOf('`', i + 1)
                    if (endCode != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = NexoraCodeBackground,
                                color = NexoraPrimaryBright,
                                fontSize = 13.sp
                            )
                        ) {
                            append(" " + raw.substring(i + 1, endCode) + " ")
                        }
                        i = endCode + 1
                        continue
                    }
                }

                // Header #
                if (raw.startsWith("### ", i)) {
                    val endLine = raw.indexOf('\n', i)
                    val line = if (endLine != -1) raw.substring(i + 4, endLine) else raw.substring(i + 4)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NexoraPrimaryBright)) {
                        append(line)
                    }
                    i = if (endLine != -1) endLine else raw.length
                    continue
                }

                append(raw[i])
                i++
            }
        }
    }

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = annotated,
            color = textColor,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
        if (showCursor) {
            PulsingStreamingCursor()
        }
    }
}

@Composable
fun PulsingStreamingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .padding(start = 4.dp, bottom = 3.dp)
            .size(width = 8.dp, height = 16.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(2.dp))
            .background(NexoraPrimaryBright)
    )
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, NexoraCodeBorder, RoundedCornerShape(10.dp))
            .testTag("code_block_${language}"),
        color = NexoraCodeBackground
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurfaceHover)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NexoraSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoraTextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code", code)
                            clipboard.setPrimaryClip(clip)
                            copied = true
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (copied) NexoraSecondary else NexoraTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (copied) "Copied" else "Copy",
                        fontSize = 12.sp,
                        color = if (copied) NexoraSecondary else NexoraTextSecondary
                    )
                }
            }

            // Code Body
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = NexoraTextPrimary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
