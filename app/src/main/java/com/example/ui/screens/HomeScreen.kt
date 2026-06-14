package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChatEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    chatViewModel: ChatViewModel,
    currentUser: UserEntity?,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val chats by chatViewModel.chats.collectAsState()
    val searchQuery by chatViewModel.searchQuery.collectAsState()
    val activeFilter by chatViewModel.activeFilter.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var selectedRenameChatId by remember { mutableStateOf("") }
    var renameTextFieldVal by remember { mutableStateOf("") }

    // Chat categories prebuilts matching instructions
    val categoryHelpers = listOf(
        CategoryHelper("Coding", "Engineering AI", Icons.Default.Code, "Write, translate or debug your functions.", "How do I optimize a quicksort function in Kotlin?"),
        CategoryHelper("Writing", "Creative Core", Icons.Default.BorderColor, "Draft letters, summaries or creative stories.", "Write an atmospheric science-fiction synopsis about SOLE AI."),
        CategoryHelper("Study", "Academy Expert", Icons.Default.School, "Clear explanations for historical or scientific events.", "Explain quantum entanglement in extremely simple terms."),
        CategoryHelper("Translations", "Polyglot Hub", Icons.Default.Translate, "Instant accurate translations and language tips.", "Translate this into French, Japanese, and Latin: 'Stay hungry, stay foolish'"),
        CategoryHelper("Math", "Analytical Mind", Icons.Default.Calculate, "Solve algebraic, statistical or logical inputs.", "Prove that the square root of 2 is irrational step by step.")
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        containerColor = AmoledBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand Info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AccentBlue.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, AccentBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S",
                                    color = AccentBlue,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SOLE AI",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Smart. Fast. Reliable.",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Profile and settings headers
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Profile Avatar Core
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CardSurface)
                                    .border(1.dp, CardSurfaceSecondary, CircleShape)
                                    .clickable { onNavigateToProfile() }
                                    .testTag("home_profile_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                val letter = currentUser?.username?.firstOrNull()?.uppercase() ?: "U"
                                Text(
                                    text = letter,
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.testTag("home_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Open Settings",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    chatViewModel.createNewChat("New Chat")
                },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("new_chat_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Chat")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "New Chat", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { chatViewModel.updateSearchQuery(it) },
                placeholder = { Text("Search chats...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("chats_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CardSurfaceSecondary,
                    unfocusedBorderColor = CardSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category presets LazyRow (Premium Quick Actions)
            if (activeFilter == "All" && chats.isEmpty() && searchQuery.isBlank()) {
                Text(
                    text = "AI AGENT CHANNELS",
                    color = AccentBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    items(categoryHelpers) { cat ->
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(130.dp)
                                .border(1.dp, CardSurface, RoundedCornerShape(16.dp))
                                .clickable {
                                    chatViewModel.injectPrebuiltChat(
                                        category = cat.categoryName,
                                        greetingText = "Welcome to your ${cat.categoryDisplayName} assist workspace! How can I help you today?",
                                        initialSystemText = cat.promptSuggestion
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = cat.icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(text = cat.categoryDisplayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = cat.miniBrief, color = TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Standard Filter Horizontal chips
            val filterOptions = listOf("All", "Pinned", "Favorites", "Archived", "Coding", "Writing", "Study", "Translations", "Math")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == activeFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentBlue else CardSurface)
                            .border(1.dp, if (isSelected) AccentBlue else CardSurfaceSecondary, RoundedCornerShape(16.dp))
                            .clickable { chatViewModel.setActiveFilter(filter) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Chats History List Header
            Text(
                text = "CONVERSATIONS",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic List
            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching conversations found" else "No active conversations",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats, key = { it.id }) { chat ->
                        ChatListItem(
                            chat = chat,
                            onItemClick = {
                                chatViewModel.selectChat(chat.id)
                                onNavigateToChat(chat.id)
                            },
                            onPinToggle = { chatViewModel.pinChat(chat.id, !chat.isPinned) },
                            onFavoriteToggle = { chatViewModel.favoriteChat(chat.id, !chat.isFavorite) },
                            onArchiveToggle = { chatViewModel.archiveChat(chat.id, !chat.isArchived) },
                            onRenameClick = {
                                selectedRenameChatId = chat.id
                                renameTextFieldVal = chat.title
                                showRenameDialog = true
                            },
                            onDeleteClick = { chatViewModel.deleteChat(chat.id) }
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, CardSurfaceSecondary, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rename Chat",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = renameTextFieldVal,
                        onValueChange = { renameTextFieldVal = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text(text = "Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (renameTextFieldVal.isNotBlank()) {
                                    chatViewModel.renameChat(selectedRenameChatId, renameTextFieldVal)
                                    showRenameDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text(text = "Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: ChatEntity,
    onItemClick: () -> Unit,
    onPinToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onArchiveToggle: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, if (chat.isPinned) AccentBlue.copy(alpha = 0.5f) else CardSurface, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = { expandedMenu = true }
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Indicators
                val catIcon = when (chat.category) {
                    "Coding" -> Icons.Default.Code
                    "Writing" -> Icons.Default.BorderColor
                    "Study" -> Icons.Default.School
                    "Translations" -> Icons.Default.Translate
                    "Math" -> Icons.Default.Calculate
                    else -> Icons.Default.ChatBubble
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CardSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = null,
                        tint = if (chat.isPinned) AccentBlue else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chat.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chat.isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.PushPin, contentDescription = "Pinned", tint = AccentBlue, modifier = Modifier.size(11.dp))
                        }
                        if (chat.isFavorite) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Fav", tint = Color(0xFFFFD60A), modifier = Modifier.size(11.dp))
                        }
                    }
                    Text(
                        text = "Category: ${chat.category}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Actions dropdown
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Actions", tint = TextTertiary)
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(CardSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (chat.isPinned) "Unpin Chat" else "Pin Chat", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = TextSecondary) },
                        onClick = {
                            onPinToggle()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (chat.isFavorite) "Remove Favorite" else "Favorite Chat", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = TextSecondary) },
                        onClick = {
                            onFavoriteToggle()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (chat.isArchived) "Unarchive Chat" else "Archive Chat", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = TextSecondary) },
                        onClick = {
                            onArchiveToggle()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Chat", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary) },
                        onClick = {
                            onRenameClick()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Chat", color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                        onClick = {
                            onDeleteClick()
                            expandedMenu = false
                        }
                    )
                }
            }
        }
    }
}

data class CategoryHelper(
    val categoryName: String,
    val categoryDisplayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val miniBrief: String,
    val promptSuggestion: String
)
