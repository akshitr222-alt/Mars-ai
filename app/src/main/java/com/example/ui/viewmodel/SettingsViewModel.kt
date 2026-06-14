package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val isAmoledMode: StateFlow<Boolean> = settingsRepository.isAmoledMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val fontSizeMultiplier: StateFlow<Float> = settingsRepository.fontSizeMultiplier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val currentLanguage: StateFlow<String> = settingsRepository.currentLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val isTtsEnabled: StateFlow<Boolean> = settingsRepository.isTtsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAmoledMode(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun updateFontSize(multiplier: Float) {
        viewModelScope.launch {
            settingsRepository.setFontSizeMultiplier(multiplier)
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(langCode)
        }
    }

    fun toggleTts(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTtsEnabled(enabled)
        }
    }

    fun resetAllApplicationData() {
        viewModelScope.launch {
            chatRepository.clearAllData()
            settingsRepository.setActiveUserEmail(null)
            settingsRepository.setAmoledMode(true)
            settingsRepository.setDarkMode(true)
            settingsRepository.setFontSizeMultiplier(1.0f)
            settingsRepository.setLanguage("en")
        }
    }
}
