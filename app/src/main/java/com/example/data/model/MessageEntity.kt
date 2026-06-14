package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String, // "user" or "model" or "loading"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val mediaPath: String? = null
)
