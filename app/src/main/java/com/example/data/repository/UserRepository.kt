package com.example.data.repository

import com.example.data.database.UserDao
import com.example.data.model.UserEntity
import com.example.util.CryptoHelper
import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    object EmailAlreadyExists : AuthResult
    object InvalidCredentials : AuthResult
    object UserNotFound : AuthResult
    data class Error(val message: String) : AuthResult
}

class UserRepository(private val userDao: UserDao) {

    suspend fun register(email: String, username: String, password: String): AuthResult {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val existing = userDao.getUserByEmail(normalizedEmail)
            if (existing != null) {
                return AuthResult.EmailAlreadyExists
            }

            val salt = CryptoHelper.generateSalt()
            val passwordHash = CryptoHelper.hashPassword(password, salt)

            val user = UserEntity(
                email = normalizedEmail,
                username = username,
                passwordHash = passwordHash,
                salt = salt
            )
            userDao.insertUser(user)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val user = userDao.getUserByEmail(normalizedEmail)
                ?: return AuthResult.InvalidCredentials

            val isValid = CryptoHelper.verifyPassword(password, user.salt, user.passwordHash)
            if (isValid) {
                AuthResult.Success(user)
            } else {
                AuthResult.InvalidCredentials
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    suspend fun resetPassword(email: String, newPassword: String): AuthResult {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val user = userDao.getUserByEmail(normalizedEmail)
                ?: return AuthResult.UserNotFound

            val newSalt = CryptoHelper.generateSalt()
            val newHash = CryptoHelper.hashPassword(newPassword, newSalt)

            val updatedUser = user.copy(
                passwordHash = newHash,
                salt = newSalt
            )
            userDao.updateUser(updatedUser)
            AuthResult.Success(updatedUser)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Password reset failed")
        }
    }

    fun getUserFlow(email: String): Flow<UserEntity?> {
        return userDao.getUserByEmailFlow(email.trim().lowercase())
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
}
