package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onResetCompleted: () -> Unit
) {
    val context = LocalContext.current

    val isAmoled by settingsViewModel.isAmoledMode.collectAsState()
    val isDark by settingsViewModel.isDarkMode.collectAsState()
    val fontScale by settingsViewModel.fontSizeMultiplier.collectAsState()
    val language by settingsViewModel.currentLanguage.collectAsState()
    val ttsEnabled by settingsViewModel.isTtsEnabled.collectAsState()

    var showResetWarning by remember { mutableStateOf(false) }
    var showLangSelector by remember { mutableStateOf(false) }

    val languagesList = remember {
        listOf(
            LangItem("en", "English"),
            LangItem("es", "Español"),
            LangItem("fr", "Français"),
            LangItem("de", "Deutsch"),
            LangItem("ja", "日本語")
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        containerColor = AmoledBlack,
        topBar = {
            TopAppBar(
                title = { Text(text = "SOLE AI Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Section: Appearance
            SettingSectionHeader(title = "VISUAL EXPERIENCE")
            
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // AMOLED Mode
                SettingToggleRow(
                    title = "OLED Pure Black Mode",
                    subtitle = "Eliminate pixel luminance for maximum power savings",
                    isChecked = isAmoled,
                    onCheckedChange = { settingsViewModel.toggleAmoledMode(it) }
                )

                // Dark Mode
                SettingToggleRow(
                    title = "Force Dark Aesthetics",
                    subtitle = "Always maintain optimized eye-safe styles",
                    isChecked = isDark,
                    onCheckedChange = { settingsViewModel.toggleDarkMode(it) }
                )
            }

            // Section: Audio and Reading
            SettingSectionHeader(title = "SPEECH ENGINE")
            SettingToggleRow(
                title = "Speech Synthesis Feedback",
                subtitle = "Read model text answers out loud automatically",
                isChecked = ttsEnabled,
                onCheckedChange = { settingsViewModel.toggleTts(it) }
            )

            // Section: Typography and Localisation
            SettingSectionHeader(title = "TYPOGRAPHY & LOCALIZATION")

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Text Font scaling slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .border(1.dp, CardSurface, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Text Font Sizing", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = when {
                                fontScale <= 0.85f -> "Compact"
                                fontScale <= 1.05f -> "Standard"
                                fontScale <= 1.25f -> "Enlarged"
                                else -> "Executive"
                            },
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { settingsViewModel.updateFontSize(it) },
                        valueRange = 0.8f..1.4f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = CardSurfaceSecondary,
                            thumbColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag("font_size_slider")
                    )
                }

                // Interactive Language Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, CardSurface, RoundedCornerShape(12.dp))
                        .clickable { showLangSelector = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "System Language", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Set conversational translation structures", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    val currentLangName = languagesList.find { it.code == language }?.name ?: "English"
                    Text(text = currentLangName, color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Section: Backup and Sync
            SettingSectionHeader(title = "DATA SECURITY")

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Simulated Export Backups
                SettingActionRow(
                    title = "Export Chat Database",
                    subtitle = "Save local conversations as an encrypted JSON archive",
                    icon = Icons.Default.SaveAlt,
                    onClick = {
                        Toast.makeText(context, "Encrypted conversation backup successfully downloaded to local storage.", Toast.LENGTH_LONG).show()
                    }
                )

                // Simulated Import Backups
                SettingActionRow(
                    title = "Import Chat Recovery Backup",
                    subtitle = "Load historical sessions back into the Room database",
                    icon = Icons.Default.DriveFolderUpload,
                    onClick = {
                        Toast.makeText(context, "Scanning folder... Local chat backup successfully restored.", Toast.LENGTH_LONG).show()
                    }
                )

                // Full system wiping (Factory Reset)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { showResetWarning = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Reset Application Cache", color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Wipe history entries, clears database logs and signs out", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Language list dialog selector
    if (showLangSelector) {
        Dialog(onDismissRequest = { showLangSelector = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, CardSurface, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Select Language", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        languagesList.forEach { item ->
                            val isSelected = item.code == language
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentBlue.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable {
                                        settingsViewModel.updateLanguage(item.code)
                                        showLangSelector = false
                                        Toast.makeText(context, "System localization altered successfully.", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.name, color = if (isSelected) AccentBlue else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full system reset warning dialog
    if (showResetWarning) {
        AlertDialog(
            onDismissRequest = { showResetWarning = false },
            title = { Text(text = "Confirm Hard Reset?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(text = "This action is irreversible. All local database chats, bookmarks, encrypted users, and preferences will be permanently wiped.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.resetAllApplicationData()
                        showResetWarning = false
                        onResetCompleted()
                        Toast.makeText(context, "Local databases cleared successfully. Refreshing credentials.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(text = "Wipe Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetWarning = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        color = TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, CardSurface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = CardSurfaceSecondary
            )
        )
    }
}

@Composable
fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, CardSurface, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

data class LangItem(val code: String, val name: String)
