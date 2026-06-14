package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    activeEmail: String?,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Alpha animation
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "Alpha"
    )

    // Scale animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2600) // Beautiful cinematic delay

        if (activeEmail != null && activeEmail.isNotBlank()) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Cyber Logo Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
                    .background(Color.Transparent)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Futuristic Glowing Orb visual
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(AccentBlue.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape)
                        .border(width = 1.5.dp, color = AccentBlue, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Text(
                    text = "S",
                    color = AccentBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Title
            Text(
                text = "SOLE AI",
                color = TextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Smart. Fast. Reliable.",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}
