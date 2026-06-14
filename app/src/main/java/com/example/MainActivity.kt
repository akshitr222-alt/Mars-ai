package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.ServiceLocator
import com.example.ui.screens.*
import com.example.ui.theme.SoleAiTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Services & Databases
        ServiceLocator.init(applicationContext)

        // Instantiate ViewModels
        val authViewModel = AuthViewModel(
            userRepository = ServiceLocator.userRepository!!,
            settingsRepository = ServiceLocator.settingsRepository!!
        )
        val chatViewModel = ChatViewModel(
            chatRepository = ServiceLocator.chatRepository!!
        )
        val settingsViewModel = SettingsViewModel(
            settingsRepository = ServiceLocator.settingsRepository!!,
            chatRepository = ServiceLocator.chatRepository!!
        )

        setContent {
            val isAmoled by settingsViewModel.isAmoledMode.collectAsState()
            val isDark by settingsViewModel.isDarkMode.collectAsState()
            val fontSizeScale by settingsViewModel.fontSizeMultiplier.collectAsState()

            val activeEmail by authViewModel.currentEmail.collectAsState()
            val currentUser by authViewModel.currentUser.collectAsState()

            SoleAiTheme(
                darkTheme = isDark,
                isAmoled = isAmoled
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    // Splash Screen Navigation Node
                    composable("splash") {
                        SplashScreen(
                            activeEmail = activeEmail,
                            onNavigateToOnboarding = {
                                navController.navigate("onboarding") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Onboarding Screen Navigation Node
                    composable("onboarding") {
                        OnboardingScreen(
                            onFinishOnboarding = {
                                navController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Login Screen Navigation Node
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onNavigateToRegister = {
                                navController.navigate("register")
                            },
                            onNavigateToForgotPassword = {
                                navController.navigate("forgot_password")
                            },
                            onLoginSuccess = {
                                chatViewModel.selectChat(null) // Reset chat state
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Register Screen Navigation Node
                    composable("register") {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onNavigateToLogin = {
                                navController.popBackStack()
                            },
                            onRegisterSuccess = {
                                chatViewModel.selectChat(null)
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Forgot Password Navigation Node
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            authViewModel = authViewModel,
                            onNavigateBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // Home Screen Dashboard Node
                    composable("home") {
                        // Keep track of active chat selections for immediate launching
                        val selectedChatId by chatViewModel.selectedChatId.collectAsState()
                        
                        LaunchedEffect(selectedChatId) {
                            selectedChatId?.let { id ->
                                navController.navigate("chat/$id")
                            }
                        }

                        HomeScreen(
                            chatViewModel = chatViewModel,
                            currentUser = currentUser,
                            onNavigateToChat = { chatId ->
                                navController.navigate("chat/$chatId")
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    // Active Chat Screen View Node
                    composable(
                        route = "chat/{chatId}",
                        arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                        ChatScreen(
                            chatId = chatId,
                            chatViewModel = chatViewModel,
                            fontSizeMultiplier = fontSizeScale,
                            onNavigateBack = {
                                chatViewModel.selectChat(null) // Unselect chat
                                navController.popBackStack()
                            }
                        )
                    }

                    // Profile Details Node
                    composable("profile") {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // System Settings Node
                    composable("settings") {
                        SettingsScreen(
                            settingsViewModel = settingsViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onResetCompleted = {
                                navController.navigate("onboarding") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
