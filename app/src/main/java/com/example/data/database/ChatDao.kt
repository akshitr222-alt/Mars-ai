package com.example.data.database

import androidx.room.*
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, createdAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    suspend fun getChatById(id: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChatList(chatId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearMessagesForChat(chatId: String)

    @Query("SELECT * FROM messages WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM chats WHERE title LIKE '%' || :query || '%'")
    fun searchChats(query: String): Flow<List<ChatEntity>>

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
