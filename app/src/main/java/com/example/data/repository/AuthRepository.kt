package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
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

    /**
     * Synchronizes the authenticated Firebase user and their real Firestore role
     * from `users/{uid}` into the local Room cache and active session state.
     */
    suspend fun syncAuthenticatedUser(user: UserEntity) {
        val previous = _currentUser.value
        userDao.insertUser(user)
        _currentUser.value = user

        if (previous?.id != user.id || previous.role != user.role) {
            auditDao.insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    userRole = user.role.name,
                    action = "USER_AUTH_ROLE_SYNC",
                    entityType = "USER",
                    entityId = user.id,
                    details = "Synchronized Firebase user ${user.email} with Firestore role ${user.role.name}"
                )
            )
        }
    }

    suspend fun clearSession() {
        _currentUser.value = null
    }

    fun observeAllUsers(): Flow<List<UserEntity>> = userDao.observeAllUsers()

    fun countUsers(): Flow<Int> = userDao.countUsers()
    fun countActiveUsers(): Flow<Int> = userDao.countActiveUsers()

    suspend fun updateUserRole(actor: UserEntity, targetUserId: String, newRole: UserRole) {
        SecurityGuard.assertPermission(actor, Permission.MANAGE_USERS)
        userDao.updateUserRole(targetUserId, newRole)
        if (_currentUser.value?.id == targetUserId) {
            _currentUser.value = _currentUser.value?.copy(role = newRole)
        }
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
        if (_currentUser.value?.id == targetUserId) {
            _currentUser.value = _currentUser.value?.copy(status = newStatus)
        }
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
