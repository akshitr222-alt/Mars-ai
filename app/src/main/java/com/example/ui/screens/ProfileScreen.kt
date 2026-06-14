package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel

data class AchievementItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    var editingUsername by remember { mutableStateOf(false) }
    var usernameFieldVal by remember { mutableStateOf("") }

    // Preset list of avatars (indices 0 to 5)
    val avatars = listOf("Ω", "Ψ", "Φ", "Σ", "Δ", "Λ")

    val achievementsList = remember {
        listOf(
            AchievementItem("AI Pioneer", "Configured and initialized the SOLE AI client framework.", Icons.Default.Rocket, Color(0xFF007BFF)),
            AchievementItem("Code Wizard", "Created or modified complex software algorithms.", Icons.Default.Code, Color(0xFFFFCC00)),
            AchievementItem("Deep Thinker", "Exceeded 5 messages in a single continuous conversation.", Icons.Default.Psychology, Color(0xFF38EF7D)),
            AchievementItem("Academic Scholar", "Interacted with SOLE AI about study and core science.", Icons.Default.School, Color(0xFFFF9500)),
            AchievementItem("Polyglot", "Sought translation advice from the linguistic core.", Icons.Default.Translate, Color(0xFFE100FF)),
            AchievementItem("Analytical Mathematician", "Calculated or verified numerical formulae.", Icons.Default.Calculate, Color(0xFF00D2FF))
        )
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            usernameFieldVal = it.username
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        containerColor = AmoledBlack,
        topBar = {
            TopAppBar(
                title = { Text(text = "SOLE AI Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }, modifier = Modifier.testTag("logout_button")) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Sign Out", tint = ErrorRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Core Panel
            Spacer(modifier = Modifier.height(16.dp))

            // Large avatar selector
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentBlue, CardSurfaceSecondary)
                        )
                    )
                    .border(2.dp, AccentBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val index = currentUser?.avatarId ?: 0
                val activeAvatar = avatars.getOrElse(index) { "Ω" }
                Text(
                    text = activeAvatar,
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Avatar Selector Quick Toggles
            Text(text = "SWAP CORE SYMBOL", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                avatars.forEachIndexed { index, symbol ->
                    val isSelected = currentUser?.avatarId == index
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AccentBlue else CardSurface)
                            .border(1.dp, if (isSelected) AccentBlue else CardSurfaceSecondary, CircleShape)
                            .clickable {
                                currentUser?.let { user ->
                                    authViewModel.updateUserProfile(user.username, index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = symbol, color = if (isSelected) Color.White else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Editable Display Name
            if (editingUsername) {
                OutlinedTextField(
                    value = usernameFieldVal,
                    onValueChange = { usernameFieldVal = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = CardSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (usernameFieldVal.isNotBlank()) {
                                currentUser?.let {
                                    authViewModel.updateUserProfile(usernameFieldVal, it.avatarId)
                                }
                                editingUsername = false
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save username", tint = SuccessGreen)
                        }
                    }
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentUser?.username ?: "SOLE User",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { editingUsername = true }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Display Name", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text(
                text = currentUser?.email ?: "",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Metrics Statistics Cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardSurface, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "TOTAL CHATS", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = (currentUser?.totalChats ?: 0).toString(),
                            color = AccentBlue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardSurface, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "TOTAL MESSAGES", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = (currentUser?.totalMessages ?: 0).toString(),
                            color = AccentBlue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Unlocked Achievements List
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LOCKED & UNLOCKED ACHIEVEMENTS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                achievementsList.forEach { ach ->
                    val isUnlocked = currentUser?.achievements?.contains(ach.title, ignoreCase = true) == true
                    AchievementRowCard(achievement = ach, isUnlocked = isUnlocked)
                }
            }
        }
    }
}

@Composable
fun AchievementRowCard(
    achievement: AchievementItem,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isUnlocked) achievement.color.copy(alpha = 0.4f) else CardSurface,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) DarkSurface else DarkSurface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (isUnlocked) achievement.color.copy(alpha = 0.1f) else CardSurfaceSecondary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = null,
                    tint = if (isUnlocked) achievement.color else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = achievement.title,
                        color = if (isUnlocked) TextPrimary else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isUnlocked) {
                        Text(
                            text = "UNLOCKED",
                            color = SuccessGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = "LOCKED",
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .background(CardSurfaceSecondary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = achievement.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
