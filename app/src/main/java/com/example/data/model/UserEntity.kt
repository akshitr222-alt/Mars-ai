package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val avatarId: Int = 0,
    val totalChats: Int = 0,
    val totalMessages: Int = 0,
    val achievements: String = "AI Pioneer" // Initial default achievement
)
