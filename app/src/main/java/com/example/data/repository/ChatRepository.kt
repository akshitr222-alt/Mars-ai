package com.example.data.repository

import com.example.data.database.ChatDao
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()
    val bookmarkedMessages: Flow<List<MessageEntity>> = chatDao.getBookmarkedMessages()

    suspend fun getChatById(id: String): ChatEntity? = chatDao.getChatById(id)

    suspend fun createChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }

    suspend fun updateChat(chat: ChatEntity) {
        chatDao.updateChat(chat)
    }

    suspend fun deleteChat(chat: ChatEntity) {
        chatDao.deleteChat(chat)
    }

    suspend fun deleteChatById(chatId: String) {
        chatDao.deleteChatById(chatId)
    }

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    suspend fun getMessagesForChatList(chatId: String): List<MessageEntity> {
        return chatDao.getMessagesForChatList(chatId)
    }

    suspend fun insertMessage(message: MessageEntity) {
        chatDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) {
        chatDao.updateMessage(message)
    }

    suspend fun deleteMessage(message: MessageEntity) {
        chatDao.deleteMessage(message)
    }

    suspend fun clearMessagesForChat(chatId: String) {
        chatDao.clearMessagesForChat(chatId)
    }

    fun searchChats(query: String): Flow<List<ChatEntity>> {
        return chatDao.searchChats(query)
    }

    suspend fun clearAllData() {
        chatDao.deleteAllMessages()
        chatDao.deleteAllChats()
    }
}
