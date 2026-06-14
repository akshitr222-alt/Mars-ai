package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    fontSizeMultiplier: Float = 1.0f
) {
    val lines = text.lines()
    val contentBlocks = remember(text) { parseMarkdown(lines) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        contentBlocks.forEach { block ->
            when (block) {
                is Block.Heading -> {
                    Text(
                        text = parseInlineStyles(block.text),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineLarge.copy(
                                fontSize = (24 * fontSizeMultiplier).sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            2 -> MaterialTheme.typography.headlineMedium.copy(
                                fontSize = (20 * fontSizeMultiplier).sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            else -> MaterialTheme.typography.headlineSmall.copy(
                                fontSize = (18 * fontSizeMultiplier).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    )
                }
                is Block.CodeBlock -> {
                    CodeViewer(
                        language = block.language,
                        code = block.code,
                        fontSizeMultiplier = fontSizeMultiplier
                    )
                }
                is Block.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = AccentBlue,
                            fontSize = (16 * fontSizeMultiplier).sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineStyles(block.text),
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = (15 * fontSizeMultiplier).sp,
                                lineHeight = 21.sp
                            )
                        )
                    }
                }
                is Block.NumberedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = block.number,
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * fontSizeMultiplier).sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineStyles(block.text),
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = (15 * fontSizeMultiplier).sp,
                                lineHeight = 21.sp
                            )
                        )
                    }
                }
                is Block.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(CardSurface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .border(width = 2.dp, color = AccentBlue, shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = parseInlineStyles(block.text),
                            style = TextStyle(
                                color = TextSecondary,
                                fontStyle = FontStyle.Italic,
                                fontSize = (15 * fontSizeMultiplier).sp,
                                lineHeight = 21.sp
                            )
                        )
                    }
                }
                is Block.Paragraph -> {
                    SelectionContainer {
                        Text(
                            text = parseInlineStyles(block.text),
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = (15 * fontSizeMultiplier).sp,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodeViewer(
    language: String,
    code: String,
    fontSizeMultiplier: Float = 1.0f
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }

    val formattedLang = remember(language) {
        if (language.isBlank()) "code" else language.lowercase()
    }

    val highlightedCode = remember(code, formattedLang) {
        highlightSyntax(code, formattedLang)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardSurfaceSecondary, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language.uppercase(),
                    color = AccentSilver,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy Button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))

                    // Share Button
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, code)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Code via"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share code",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Expand/Collapse Button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code Area
            AnimatedVisibility(visible = isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = highlightedCode,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = (13 * fontSizeMultiplier).sp,
                                color = TextPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

// Simple Parser Models
sealed class Block {
    data class Heading(val text: String, val level: Int) : Block()
    data class CodeBlock(val language: String, val code: String) : Block()
    data class BulletItem(val text: String) : Block()
    data class NumberedItem(val number: String, val text: String) : Block()
    data class Quote(val text: String) : Block()
    data class Paragraph(val text: String) : Block()
}

fun parseMarkdown(lines: List<String>): List<Block> {
    val blocks = mutableListOf<Block>()
    var inCodeBlock = false
    var currentLanguage = ""
    val codeBuilder = StringBuilder()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(Block.CodeBlock(currentLanguage, codeBuilder.toString()))
                codeBuilder.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                currentLanguage = line.trim().removePrefix("```").trim()
            }
            i++
            continue
        }

        if (inCodeBlock) {
            codeBuilder.append(line).append("\n")
            i++
            continue
        }

        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> {
                blocks.add(Block.Heading(trimmed.removePrefix("# ").trim(), 1))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(Block.Heading(trimmed.removePrefix("## ").trim(), 2))
            }
            trimmed.startsWith("### ") -> {
                blocks.add(Block.Heading(trimmed.removePrefix("### ").trim(), 3))
            }
            trimmed.startsWith("> ") -> {
                blocks.add(Block.Quote(trimmed.removePrefix("> ").trim()))
            }
            trimmed.startsWith("- ") -> {
                blocks.add(Block.BulletItem(trimmed.removePrefix("- ").trim()))
            }
            trimmed.startsWith("* ") -> {
                blocks.add(Block.BulletItem(trimmed.removePrefix("* ").trim()))
            }
            // Simple numbered items check (e.g. "1. ")
            trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                val index = trimmed.indexOf(".")
                val number = trimmed.substring(0, index + 1)
                val text = trimmed.substring(index + 1).trim()
                blocks.add(Block.NumberedItem(number, text))
            }
            trimmed.isEmpty() -> {
                // Add minor content separator spacer if desired or skip
            }
            else -> {
                blocks.add(Block.Paragraph(line))
            }
        }
        i++
    }

    if (inCodeBlock) {
        blocks.add(Block.CodeBlock(currentLanguage, codeBuilder.toString()))
    }

    return blocks
}

// Inline Styling (Bold **text** and Italic *text*)
fun parseInlineStyles(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldIndex = text.indexOf("**", cursor)
            val italicIndex = text.indexOf("*", cursor)

            if (boldIndex != -1 && (italicIndex == -1 || boldIndex < italicIndex)) {
                // Add text before bold
                append(text.substring(cursor, boldIndex))
                val endBold = text.indexOf("**", boldIndex + 2)
                if (endBold != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(text.substring(boldIndex + 2, endBold))
                    }
                    cursor = endBold + 2
                } else {
                    append("**")
                    cursor = boldIndex + 2
                }
            } else if (italicIndex != -1) {
                // Add text before italic
                append(text.substring(cursor, italicIndex))
                val endItalic = text.indexOf("*", italicIndex + 1)
                if (endItalic != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = TextPrimary)) {
                        append(text.substring(italicIndex + 1, endItalic))
                    }
                    cursor = endItalic + 1
                } else {
                    append("*")
                    cursor = italicIndex + 1
                }
            } else {
                append(text.substring(cursor))
                break
            }
        }
    }
}

// Syntax Highlighting Engine
fun highlightSyntax(code: String, language: String): AnnotatedString {
    val keywords = setOf(
        "val", "var", "fun", "class", "interface", "object", "return", "import", "package", 
        "def", "import", "from", "as", "if", "else", "elif", "while", "for", "in", "try", "except", 
        "finally", "let", "const", "function", "public", "private", "protected", "void", "static", 
        "int", "double", "float", "String", "boolean", "bool", "select", "from", "where", "insert", "update", "delete"
    )
    val types = setOf(
        "Int", "Long", "Double", "Float", "String", "Boolean", "Any", "Unit", "List", "Map", "Set", 
        "Dict", "str", "float", "int", "list", "dict", "tuple"
    )

    return buildAnnotatedString {
        val tokens = code.split(Regex("""(?<=\W)|(?=\W)"""))
        var inComment = false
        var inString = false
        var stringChar = '"'

        tokens.forEach { token ->
            when {
                inComment -> {
                    withStyle(SpanStyle(color = Color(0xFF6A9955), fontStyle = FontStyle.Italic)) {
                        append(token)
                    }
                    if (token == "\n") inComment = false
                }
                inString -> {
                    withStyle(SpanStyle(color = Color(0xFFCE9178))) {
                        append(token)
                    }
                    if (token.contains(stringChar.toString())) {
                        inString = false
                    }
                }
                token == "//" || token == "#" -> {
                    inComment = true
                    withStyle(SpanStyle(color = Color(0xFF6A9955), fontStyle = FontStyle.Italic)) {
                        append(token)
                    }
                }
                token == "\"" || token == "'" -> {
                    inString = true
                    stringChar = token[0]
                    withStyle(SpanStyle(color = Color(0xFFCE9178))) {
                        append(token)
                    }
                }
                token in keywords -> {
                    withStyle(SpanStyle(color = Color(0xFF569CD6), fontWeight = FontWeight.Bold)) {
                        append(token)
                    }
                }
                token in types -> {
                    withStyle(SpanStyle(color = Color(0xFF4EC9B0))) {
                        append(token)
                    }
                }
                token.matches(Regex("""^\d+$""")) -> {
                    withStyle(SpanStyle(color = Color(0xFFB5CEA8))) {
                        append(token)
                    }
                }
                else -> {
                    append(token)
                }
            }
        }
    }
}
