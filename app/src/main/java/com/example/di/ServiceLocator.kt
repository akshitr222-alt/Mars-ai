package com.example.di

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.repository.ChatRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserRepository

object ServiceLocator {
    private var database: AppDatabase? = null
    
    var userRepository: UserRepository? = null
        private set
        
    var chatRepository: ChatRepository? = null
        private set
        
    var settingsRepository: SettingsRepository? = null
        private set

    fun init(context: Context) {
        if (database == null) {
            val db = AppDatabase.getDatabase(context)
            database = db
            userRepository = UserRepository(db.userDao())
            chatRepository = ChatRepository(db.chatDao())
            settingsRepository = SettingsRepository(context)
        }
    }
}
