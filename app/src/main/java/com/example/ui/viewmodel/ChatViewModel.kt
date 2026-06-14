package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.network.Content
import com.example.data.network.GeminiClient
import com.example.data.network.Part
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter type: "All", "Pinned", "Favorites", "Archived", "Writing", "Coding", "Study"
    private val _activeFilter = MutableStateFlow("All")
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()

    // Selected model tier
    private val _useProModel = MutableStateFlow(false)
    val useProModel: StateFlow<Boolean> = _useProModel.asStateFlow()

    // Typist streams (Word-by-word simulation for luxury response flow)
    private val _currentStreamingContent = MutableStateFlow("")
    val currentStreamingContent: StateFlow<String> = _currentStreamingContent.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Recommended followup prompts state
    private val _smartFollowUps = MutableStateFlow<List<String>>(emptyList())
    val smartFollowUps: StateFlow<List<String>> = _smartFollowUps.asStateFlow()

    // Observe master chats lists and filter locally in Flow
    val chats: StateFlow<List<ChatEntity>> = combine(
        chatRepository.allChats,
        _searchQuery,
        _activeFilter
    ) { chatsList, query, filter ->
        var list = chatsList
        
        if (query.isNotBlank()) {
            list = list.filter { it.title.contains(query, ignoreCase = true) }
        }

        when (filter) {
            "All" -> list = list.filter { !it.isArchived }
            "Pinned" -> list = list.filter { it.isPinned && !it.isArchived }
            "Favorites" -> list = list.filter { it.isFavorite && !it.isArchived }
            "Archived" -> list = list.filter { it.isArchived }
            else -> list = list.filter { it.category == filter && !it.isArchived }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message lists observed reactively
    val activeMessages: StateFlow<List<MessageEntity>> = _selectedChatId
        .flatMapLatest { id ->
            if (id != null) {
                chatRepository.getMessagesForChat(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedMessages: StateFlow<List<MessageEntity>> = chatRepository.bookmarkedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
        _currentStreamingContent.value = ""
        _isGenerating.value = false
        _smartFollowUps.value = emptyList()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveFilter(filter: String) {
        _activeFilter.value = filter
    }

    fun toggleProModel(enabled: Boolean) {
        _useProModel.value = enabled
    }

    fun createNewChat(title: String = "Untitled Chat", category: String = "General") {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            val newChat = ChatEntity(id = newId, title = title, category = category)
            chatRepository.createChat(newChat)
            _selectedChatId.value = newId
        }
    }

    fun pinChat(chatId: String, isPinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChatById(chatId)?.let { chat ->
                chatRepository.updateChat(chat.copy(isPinned = isPinned))
            }
        }
    }

    fun favoriteChat(chatId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChatById(chatId)?.let { chat ->
                chatRepository.updateChat(chat.copy(isFavorite = isFavorite))
            }
        }
    }

    fun archiveChat(chatId: String, isArchived: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChatById(chatId)?.let { chat ->
                chatRepository.updateChat(chat.copy(isArchived = isArchived))
            }
        }
    }

    fun renameChat(chatId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChatById(chatId)?.let { chat ->
                chatRepository.updateChat(chat.copy(title = newName))
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.deleteChatById(chatId)
            chatRepository.clearMessagesForChat(chatId)
            if (_selectedChatId.value == chatId) {
                _selectedChatId.value = null
            }
        }
    }

    fun toggleBookmarkMessage(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.updateMessage(message.copy(isBookmarked = !message.isBookmarked))
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.deleteMessage(message)
        }
    }

    fun editMessage(message: MessageEntity, newContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.updateMessage(message.copy(content = newContent))
        }
    }

    fun clearActiveChatHistory() {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.clearMessagesForChat(chatId)
        }
    }

    fun sendMessage(content: String, onMessageSentCallback: (Int) -> Unit = {}) {
        val chatId = _selectedChatId.value ?: return
        if (content.isBlank() || _isGenerating.value) return

        viewModelScope.launch(Dispatchers.Main) {
            _isGenerating.value = true
            _currentStreamingContent.value = ""
            _smartFollowUps.value = emptyList()

            // Save user message in DB
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = "user",
                content = content
            )
            chatRepository.insertMessage(userMsg)
            onMessageSentCallback(1) // increment user activity count

            // Create placeholder chat history parts
            val historyMsgs = chatRepository.getMessagesForChatList(chatId)
            val isFirstMessage = historyMsgs.size <= 1

            val networkHistory = historyMsgs.filter { it.id != userMsg.id }.map {
                Content(
                    role = if (it.role == "user") "user" else "model",
                    parts = listOf(Part(text = it.content))
                )
            }

            // Create temporary AI "loading bubble"
            val aiLoadingMsgId = UUID.randomUUID().toString()
            val aiLoadingMsg = MessageEntity(
                id = aiLoadingMsgId,
                chatId = chatId,
                role = "loading",
                content = "SOLE AI is parsing instructions..."
            )
            chatRepository.insertMessage(aiLoadingMsg)

            // Make network call
            val fullResponseText = GeminiClient.fetchAiResponse(
                prompt = content,
                history = networkHistory,
                useProModel = _useProModel.value,
                systemInstruction = "You are SOLE AI, a highly futuristic and ultra-capable premium assistant. Present yourself professionally. Render lists, headings, and code blocks elegantly with appropriate programming tags."
            )

            // Remove loading message
            chatRepository.deleteMessage(aiLoadingMsg)

            // Luxury Streaming Simulated Typist
            simulateWordByWordStreaming(fullResponseText, chatId)

            // Trigger autotitling if it is the first message
            if (isFirstMessage) {
                triggerAutoTitle(content, chatId)
            }

            // Update user with smart follow-ups
            computeSmartFollowups(fullResponseText)
            onMessageSentCallback(1) // increment AI response statistic counter
        }
    }

    private suspend fun simulateWordByWordStreaming(fullText: String, chatId: String) {
        _isGenerating.value = true
        _currentStreamingContent.value = ""

        val words = fullText.split(" ")
        val currentSb = java.lang.StringBuilder()
        
        // Feed into state flow at a highly aesthetic pace (e.g. 15-30ms per word)
        for (i in words.indices) {
            currentSb.append(words[i])
            if (i < words.lastIndex) {
                currentSb.append(" ")
            }
            _currentStreamingContent.value = currentSb.toString()
            delay(20)
        }

        // Write final response directly to Database
        val assistantMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            role = "model",
            content = fullText
        )
        chatRepository.insertMessage(assistantMsg)
        _currentStreamingContent.value = ""
        _isGenerating.value = false
    }

    private fun triggerAutoTitle(userPrompt: String, chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val titleQuery = "Create a brief 2 to 3 word title for a conversation starting with: \"$userPrompt\". Return only the title."
            val shortenedTitle = GeminiClient.fetchAiResponse(
                prompt = titleQuery,
                useProModel = false
            ).removeSurrounding("\"").trim()
            
            val validTitle = if (shortenedTitle.length > 25) {
                shortenedTitle.take(22) + "..."
            } else if (shortenedTitle.startsWith("Error", ignoreCase = true)) {
                userPrompt.take(15) + "..."
            } else {
                shortenedTitle
            }

            chatRepository.getChatById(chatId)?.let { chat ->
                chatRepository.updateChat(chat.copy(title = validTitle))
            }
        }
    }

    private fun computeSmartFollowups(aiResponse: String) {
        viewModelScope.launch {
            val followupQuery = "Given this AI response, provide exactly 3 brief relevant follow-up questions for the user to select. Format your response strictly as 3 bullet points, each on a new line starting with a dot, without any extra text."
            val followupsRaw = GeminiClient.fetchAiResponse(
                prompt = followupQuery,
                useProModel = false
            )

            val parsedList = followupsRaw.lines()
                .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
                .filter { it.isNotEmpty() }
                .take(3)

            _smartFollowUps.value = if (parsedList.size == 3) parsedList else listOf(
                "Tell me more about this",
                "Can you provide a code sample?",
                "Analyze the pros and cons"
            )
        }
    }

    fun injectPrebuiltChat(category: String, greetingText: String, initialSystemText: String) {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            val title = when (category) {
                "Writing" -> "Creative Writer"
                "Coding" -> "Engineering Core"
                "Study" -> "Academic Tutor"
                "Translations" -> "Polyglot Hub"
                "Math" -> "Analytical Math"
                else -> "SOLE Assistant"
            }
            val prebuiltChat = ChatEntity(id = newId, title = title, category = category)
            chatRepository.createChat(prebuiltChat)
            
            val promptMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = newId,
                role = "model",
                content = greetingText
            )
            chatRepository.insertMessage(promptMsg)
            _selectedChatId.value = newId
        }
    }
}
