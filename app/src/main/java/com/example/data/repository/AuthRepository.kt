package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.security.PasswordHasher
import com.example.security.Permission
import com.example.security.SecurityGuard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val auditDao = database.auditDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: Flow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun autoLoginDefaultUser(): UserEntity {
        // If not logged in, default to the Admin account for complete showcase
        val current = _currentUser.value
        if (current != null) return current

        val admin = userDao.getUserById("usr_admin") ?: run {
            // If DB not ready yet, seed or query
            userDao.getAllUsers().firstOrNull() ?: UserEntity(
                id = "usr_admin",
                username = "admin",
                email = "admin@edubank.org",
                passwordHash = "",
                salt = "",
                role = UserRole.ADMIN,
                displayName = "Dr. Arthur Pendelton"
            )
        }
        _currentUser.value = admin
        return admin
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val user = if (identifier.contains("@")) {
            userDao.getUserByEmail(identifier.trim().lowercase())
        } else {
            userDao.getUserByUsername(identifier.trim().lowercase())
        } ?: return Result.failure(IllegalArgumentException("User not found."))

        if (user.status == UserStatus.SUSPENDED) {
            return Result.failure(SecurityException("Your account has been suspended by an administrator."))
        }

        val isValid = PasswordHasher.verifyPassword(password, user.salt, user.passwordHash)
        if (!isValid) {
            return Result.failure(IllegalArgumentException("Invalid password."))
        }

        val now = System.currentTimeMillis()
        userDao.updateLastLogin(user.id, now)
        val updated = user.copy(lastLoginAt = now)
        _currentUser.value = updated

        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                userRole = user.role.name,
                action = "USER_LOGIN",
                entityType = "USER",
                entityId = user.id,
                details = "User logged in successfully"
            )
        )
        return Result.success(updated)
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String,
        role: UserRole = UserRole.MEMBER
    ): Result<UserEntity> {
        if (username.isBlank() || email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Please provide valid username, email, and at least 6-character password."))
        }

        if (userDao.getUserByUsername(username.trim().lowercase()) != null) {
            return Result.failure(IllegalArgumentException("Username already in use."))
        }

        if (userDao.getUserByEmail(email.trim().lowercase()) != null) {
            return Result.failure(IllegalArgumentException("Email already registered."))
        }

        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(password, salt)
        val newUser = UserEntity(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            username = username.trim().lowercase(),
            email = email.trim().lowercase(),
            passwordHash = hash,
            salt = salt,
            role = role,
            displayName = displayName.ifBlank { username }
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser

        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = newUser.id,
                userRole = newUser.role.name,
                action = "USER_REGISTER",
                entityType = "USER",
                entityId = newUser.id,
                details = "New user registered with role ${role.name}"
            )
        )
        return Result.success(newUser)
    }

    suspend fun switchUserPersona(targetUserId: String): UserEntity? {
        val target = userDao.getUserById(targetUserId) ?: return null
        _currentUser.value = target
        return target
    }

    suspend fun logout() {
        _currentUser.value = null
    }

    fun observeAllUsers(): Flow<List<UserEntity>> = userDao.observeAllUsers()

    fun countUsers(): Flow<Int> = userDao.countUsers()
    fun countActiveUsers(): Flow<Int> = userDao.countActiveUsers()

    suspend fun updateUserRole(actor: UserEntity, targetUserId: String, newRole: UserRole) {
        SecurityGuard.assertPermission(actor, Permission.MANAGE_USERS)
        userDao.updateUserRole(targetUserId, newRole)
        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = "ROLE_MODIFIED",
                entityType = "USER",
                entityId = targetUserId,
                details = "Changed role of $targetUserId to ${newRole.name}"
            )
        )
    }

    suspend fun updateUserStatus(actor: UserEntity, targetUserId: String, newStatus: UserStatus) {
        SecurityGuard.assertPermission(actor, Permission.MANAGE_USERS)
        userDao.updateUserStatus(targetUserId, newStatus)
        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = "STATUS_MODIFIED",
                entityType = "USER",
                entityId = targetUserId,
                details = "Changed status of $targetUserId to ${newStatus.name}"
            )
        )
    }
}
