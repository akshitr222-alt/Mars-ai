package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val message: String) : AuthState
    data class Error(val error: String) : AuthState
}

class AuthViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentEmail: StateFlow<String?> = settingsRepository.activeUserEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUser: StateFlow<UserEntity?> = currentEmail
        .flatMapLatest { email ->
            if (email != null) {
                userRepository.getUserFlow(email)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun register(email: String, username: String, password: String) {
        if (email.isBlank() || username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val result = userRepository.register(email, username, password)) {
                is AuthResult.Success -> {
                    settingsRepository.setActiveUserEmail(result.user.email)
                    _authState.value = AuthState.Success("Account successfully created")
                }
                is AuthResult.EmailAlreadyExists -> {
                    _authState.value = AuthState.Error("This email address is already registered")
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {
                    _authState.value = AuthState.Error("An error occurred during registration")
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val result = userRepository.login(email, password)) {
                is AuthResult.Success -> {
                    settingsRepository.setActiveUserEmail(result.user.email)
                    _authState.value = AuthState.Success("Access Granted")
                }
                is AuthResult.InvalidCredentials -> {
                    _authState.value = AuthState.Error("Invalid login credentials")
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {
                    _authState.value = AuthState.Error("Auth verification failed")
                }
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        if (email.isBlank() || newPassword.isBlank()) {
            _authState.value = AuthState.Error("Email and new password are required")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val result = userRepository.resetPassword(email, newPassword)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Success("Password was successfully updated. You can login now.")
                }
                is AuthResult.UserNotFound -> {
                    _authState.value = AuthState.Error("No account matches this email address")
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {
                    _authState.value = AuthState.Error("Reset password transition failed")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            settingsRepository.setActiveUserEmail(null)
            _authState.value = AuthState.Idle
        }
    }

    fun clearAuthState() {
        _authState.value = AuthState.Idle
    }

    fun updateUserProfile(username: String, avatarId: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            userRepository.updateUser(user.copy(username = username, avatarId = avatarId))
        }
    }

    fun rewardAchievement(achievementName: String) {
        val user = currentUser.value ?: return
        if (user.achievements.contains(achievementName)) return
        val updatedAchievements = "${user.achievements}, $achievementName"
        viewModelScope.launch {
            userRepository.updateUser(user.copy(achievements = updatedAchievements))
        }
    }

    fun incrementUserStats(chatsDiff: Int = 0, messagesDiff: Int = 0) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            userRepository.updateUser(user.copy(
                totalChats = user.totalChats + chatsDiff,
                totalMessages = user.totalMessages + messagesDiff
            ))
        }
    }
}
