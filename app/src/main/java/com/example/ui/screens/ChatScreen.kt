package com.example.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.ui.components.MarkdownText
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    chatViewModel: ChatViewModel,
    fontSizeMultiplier: Float,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val messages by chatViewModel.activeMessages.collectAsState()
    val isGenerating by chatViewModel.isGenerating.collectAsState()
    val currentStreaming by chatViewModel.currentStreamingContent.collectAsState()
    val proModelMode by chatViewModel.useProModel.collectAsState()
    val smartFollowUps by chatViewModel.smartFollowUps.collectAsState()

    var activeChat by remember { mutableStateOf<ChatEntity?>(null) }
    var inputTextFieldVal by remember { mutableStateOf("") }

    // TTS Synthesis engine local state
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeakingMsgId by remember { mutableStateOf("") }

    // List scrolling state
    val listState = rememberLazyListState()

    // Initialize TTS
    DisposableEffect(Unit) {
        val initializedTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = initializedTts
        onDispose {
            initializedTts.stop()
            initializedTts.shutdown()
        }
    }

    // Load active chat info
    LaunchedEffect(chatId) {
        activeChat = chatViewModel.chats.value.find { it.id == chatId }
    }

    // Scroll to latest message on size change or text streaming
    LaunchedEffect(messages.size, currentStreaming) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        containerColor = AmoledBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeChat?.title ?: "Chatting with SOLE",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGenerating) "Typing response..." else "Active Mode: ${if (proModelMode) "Gemini Pro" else "Gemini Flash"}",
                            color = if (isGenerating) AccentBlue else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    // Chat memory clear
                    IconButton(onClick = { chatViewModel.clearActiveChatHistory() }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Session History", tint = TextSecondary)
                    }

                    // Pro mode toggle switch
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRO",
                            color = if (proModelMode) AccentBlue else TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = proModelMode,
                            onCheckedChange = { chatViewModel.toggleProModel(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = CardSurface
                            ),
                            modifier = Modifier.scale(0.8f).testTag("pro_model_toggle")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        bottomBar = {
            // Layout Input and suggestions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmoledBlack)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Smart recommendations suggestions row
                AnimatedVisibility(visible = smartFollowUps.isNotEmpty() && !isGenerating) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(text = "SUGGESTED ACTIONS", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(smartFollowUps) { fup ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CardSurface)
                                        .border(1.dp, CardSurfaceSecondary, RoundedCornerShape(8.dp))
                                        .clickable {
                                            inputTextFieldVal = fup
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = fup, color = AccentSilver, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Icon(imageVector = Icons.Default.ArrowOutward, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary inputs text bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputTextFieldVal,
                        onValueChange = { inputTextFieldVal = it },
                        placeholder = { Text("Ask SOLE AI Anything...", color = TextSecondary) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CardSurfaceSecondary,
                            unfocusedBorderColor = CardSurface,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        trailingIcon = {
                            // Voice Microphone trigger simulation
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        inputTextFieldVal = "Processing spoken instructions..."
                                        delay(1500)
                                        inputTextFieldVal = "How does artificial neural networking solve natural language computations?"
                                    }
                                },
                                modifier = Modifier.testTag("voice_input_trigger")
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = TextSecondary)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Floating action submit bubble
                    IconButton(
                        onClick = {
                            if (inputTextFieldVal.isNotBlank() && !isGenerating) {
                                val text = inputTextFieldVal
                                inputTextFieldVal = ""
                                chatViewModel.sendMessage(text)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (inputTextFieldVal.isNotBlank()) AccentBlue else CardSurface, CircleShape)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send prompt",
                            tint = if (inputTextFieldVal.isNotBlank()) Color.White else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty() && currentStreaming.isBlank()) {
                // Empty view guidance helper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = AccentBlue.copy(alpha = 0.5f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SOLE AI INTELLIGENCE INTERFACE",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Begin asking queries. Your active history is encrypted, stored offline on this device, and secure.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        MessageBubbleItem(
                            message = message,
                            fontSizeMultiplier = fontSizeMultiplier,
                            isSpeaking = isTtsSpeakingMsgId == message.id,
                            onCopy = { clipboardManager.setText(AnnotatedString(message.content)) },
                            onDelete = { chatViewModel.deleteMessage(message) },
                            onBookmarkToggle = { chatViewModel.toggleBookmarkMessage(message) },
                            onSpeechToggle = {
                                if (isTtsSpeakingMsgId == message.id) {
                                    tts?.stop()
                                    isTtsSpeakingMsgId = ""
                                } else {
                                    isTtsSpeakingMsgId = message.id
                                    tts?.speak(message.content, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            }
                        )
                    }

                    // Current typing simulated streaming assistant bubble
                    if (currentStreaming.isNotBlank()) {
                        item {
                            StreamingBubbleItem(
                                content = currentStreaming,
                                fontSizeMultiplier = fontSizeMultiplier
                            )
                        }
                    }

                    // Custom loading dynamic anim
                    if (isGenerating && currentStreaming.isBlank()) {
                        item {
                            LoadingBubbleItem()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    fontSizeMultiplier: Float,
    isSpeaking: Boolean,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onSpeechToggle: () -> Unit
) {
    val isUser = message.role == "user"
    val isModel = message.role == "model"

    var actionSheetVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile symbol leading for AI model
            if (isModel) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(AccentBlue.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, AccentBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "S", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Body Card Bubble
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) CardSurfaceSecondary else DarkSurface)
                    .border(
                        1.dp,
                        if (isUser) CardSurfaceSecondary else CardSurface,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                if (isUser) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = (15 * fontSizeMultiplier).sp,
                        lineHeight = 21.sp
                    )
                } else if (isModel) {
                    MarkdownText(
                        text = message.content,
                        fontSizeMultiplier = fontSizeMultiplier
                    )
                } else {
                    // Loading placeholder
                    Text(
                        text = message.content,
                        color = TextSecondary,
                        fontSize = (13 * fontSizeMultiplier).sp
                    )
                }

                // Bubbles quick bar
                if (isModel || isUser) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy message", tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(24.dp)) {
                            val activeIcon = if (message.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                            Icon(imageVector = activeIcon, contentDescription = "Bookmark prompt", tint = if (message.isBookmarked) AccentBlue else TextTertiary, modifier = Modifier.size(14.dp))
                        }
                        if (isModel) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onSpeechToggle, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
                                    contentDescription = "Speak Text",
                                    tint = if (isSpeaking) AccentBlue else TextTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // User leading profile symbol
            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(CardSurface, CircleShape)
                        .border(1.dp, CardSurfaceSecondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StreamingBubbleItem(
    content: String,
    fontSizeMultiplier: Float
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(AccentBlue.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "S", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(DarkSurface)
                .border(1.dp, CardSurface, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .padding(14.dp)
        ) {
            MarkdownText(
                text = content,
                fontSizeMultiplier = fontSizeMultiplier
            )
        }
    }
}

@Composable
fun LoadingBubbleItem() {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(CardSurface, CircleShape)
                .border(1.dp, CardSurfaceSecondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "S", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(DarkSurface)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Spinning pulse visual placeholders
                CircularProgressIndicator(
                    color = AccentBlue,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "SOLE AI is formulating solutions...", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
