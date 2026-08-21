package com.gramflow.app.data.repository

import com.gramflow.app.data.local.dao.UserDao
import com.gramflow.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class AuthRepository(private val userDao: UserDao) {

    private val _sessionUser = MutableStateFlow<UserEntity?>(null)
    val activeUserFlow: StateFlow<UserEntity?> = _sessionUser.asStateFlow()

    private var lastActiveTimestamp: Long = 0L
    val INACTIVITY_TIMEOUT_MS = 30 * 60 * 1000L // 30 Minutes

    fun recordActivity() {
        lastActiveTimestamp = System.currentTimeMillis()
    }

    fun isSessionValid(): Boolean {
        val user = _sessionUser.value ?: return false
        val isExpired = System.currentTimeMillis() - lastActiveTimestamp > INACTIVITY_TIMEOUT_MS
        if (isExpired) {
            logout()
            return false
        }
        return true
    }

    fun logout() {
        _sessionUser.value = null
        lastActiveTimestamp = 0L
    }

    suspend fun getActiveUser(): UserEntity? {
        if (isSessionValid()) {
            return _sessionUser.value
        }
        return null
    }

    suspend fun login(email: String, passwordRaw: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (!trimmedEmail.endsWith("@hemp.com")) {
            return Result.failure(Exception("Unauthorized domain. Please use your @hemp.com address."))
        }
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("Invalid email or password."))

        // Check password
        val inputHash = hashPassword(passwordRaw)
        if (user.passwordHash != inputHash && user.passwordHash != passwordRaw) {
            return Result.failure(Exception("Invalid email or password."))
        }

        _sessionUser.value = user
        recordActivity()
        return Result.success(user)
    }

    suspend fun signup(email: String, name: String, passwordRaw: String, registrationCode: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (!trimmedEmail.endsWith("@hemp.com")) {
            return Result.failure(Exception("Unauthorized domain. Please use your @hemp.com address."))
        }
        if (registrationCode != "HEMP2026" && registrationCode != "ADMIN2026") {
            return Result.failure(Exception("Invalid registration security code. Use HEMP2026."))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val newUser = UserEntity(
            email = trimmedEmail,
            name = name.trim(),
            passwordHash = hashPassword(passwordRaw)
        )
        val id = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = id)

        _sessionUser.value = createdUser
        recordActivity()
        return Result.success(createdUser)
    }

    suspend fun updateProfile(email: String, currentPassword: String, newPassword: String?, newName: String?): Result<Unit> {
        val user = userDao.getUserByEmail(email)
            ?: return Result.failure(Exception("User not found."))

        val currentHash = hashPassword(currentPassword)
        if (user.passwordHash != currentHash && user.passwordHash != currentPassword) {
            return Result.failure(Exception("Incorrect current password."))
        }

        val updated = user.copy(
            name = if (!newName.isNullOrBlank()) newName.trim() else user.name,
            passwordHash = if (!newPassword.isNullOrBlank()) hashPassword(newPassword) else user.passwordHash
        )
        userDao.updateUser(updated)
        _sessionUser.value = updated
        recordActivity()
        return Result.success(Unit)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
