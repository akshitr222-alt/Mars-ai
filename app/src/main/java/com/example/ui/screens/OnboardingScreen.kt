package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val symbolLetter: String,
    val description: String,
    val gradientColors: List<Color>
)

@Composable
fun OnboardingScreen(onFinishOnboarding: () -> Unit) {
    var currentPageIndex by remember { mutableStateOf(0) }

    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Welcome to SOLE AI",
                subtitle = "The Future of Assistance",
                symbolLetter = "Ω",
                description = "Experience a quantum leap in daily productivity. SOLE AI integrates cutting-edge generative thinking to serve as your ultimate intellectual resource.",
                gradientColors = listOf(Color(0xFF007BFF), Color(0xFF00C6FF))
            ),
            OnboardingPageData(
                title = "Ask Anything",
                subtitle = "Infinite Knowledge System",
                symbolLetter = "Ψ",
                description = "Get instant, comprehensive answers to any question. Complex science, history, calculations or casual advice, available in milliseconds.",
                gradientColors = listOf(Color(0xFFFF5E62), Color(0xFFFF9966))
            ),
            OnboardingPageData(
                title = "Get Intelligent Responses",
                subtitle = "Context-Aware Memory",
                symbolLetter = "Φ",
                description = "Engage in natural conversations. SOLE AI remembers historic context and follows up intelligently, maintaining a secure, logical memory stream.",
                gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            ),
            OnboardingPageData(
                title = "Create Content & Solutions",
                subtitle = "The Expert Companion",
                symbolLetter = "Σ",
                description = "Draft books, solve mathematical models, format custom structured articles, and program complex functions with dynamic syntax highlighting.",
                gradientColors = listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
            ),
            OnboardingPageData(
                title = "Start Your Journey",
                subtitle = "Smart. Fast. Reliable.",
                symbolLetter = "Δ",
                description = "Your flagship AI dashboard is optimized and waiting. Unlock your personal and private digital assistant today.",
                gradientColors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
            )
        )
    }

    val page = pages[currentPageIndex]

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        containerColor = AmoledBlack,
        bottomBar = {
            // Action Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                if (currentPageIndex > 0) {
                    IconButton(
                        onClick = { currentPageIndex-- },
                        modifier = Modifier
                            .size(52.dp)
                            .border(1.dp, CardSurface, CircleShape)
                            .background(AmoledBlack, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Onboarding Page",
                            tint = TextPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(52.dp))
                }

                // Smooth Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.forEachIndexed { idx, _ ->
                        val isSelected = idx == currentPageIndex
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "Indicator"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentBlue else CardSurfaceSecondary)
                        )
                    }
                }

                // Next or Launch Button
                if (currentPageIndex == pages.lastIndex) {
                    Button(
                        onClick = onFinishOnboarding,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("onboarding_finish_button")
                    ) {
                        Text(
                            text = "Launch",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { currentPageIndex++ },
                        modifier = Modifier
                            .size(52.dp)
                            .background(AccentBlue, CircleShape)
                            .testTag("onboarding_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Onboarding Page",
                            tint = Color.White
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
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Animation Panel
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                page.gradientColors[0].copy(alpha = 0.15f),
                                AmoledBlack
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(page.gradientColors[0], Color.Transparent)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Vector glyph
                Text(
                    text = page.symbolLetter,
                    color = page.gradientColors[0],
                    fontSize = 72.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(42.dp))

            // Text Block (Smooth animated crossfade)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = page.subtitle.uppercase(),
                    color = AccentBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.title,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
